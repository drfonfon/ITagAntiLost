package com.fonfon.itagantilost.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fonfon.itagantilost.App;
import com.fonfon.itagantilost.R;
import com.fonfon.itagantilost.lib.BleConstants;
import com.fonfon.itagantilost.lib.BleService;

import java.util.List;

public class MainActivityViewModel extends ScanCallback implements DevicesAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

    private final static int REQUEST_ENABLE_BT = 451;
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private AppCompatActivity activity;
    private DataListener dataListener;

    private Handler mHandler;
    private Runnable stopScan;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dataListener.notifyAdapter();
        }
    };

    public MainActivityViewModel(AppCompatActivity activity, DataListener dataListener) {
        this.activity = activity;
        this.dataListener = dataListener;

        mHandler = new Handler();
    }

    public void init() {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            activity.finish();
        }

        bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        onRefresh();
    }

    public void resume() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            activity.startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT
            );
        }
        IntentFilter filter = new IntentFilter("bleUpdated");
        activity.registerReceiver(receiver, filter);
    }

    public void pause() {
        activity.unregisterReceiver(receiver);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) resume();
    }

    public void onDestroy() {
        activity = null;
        dataListener = null;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        try {
            List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
            if (uuids != null) {
                for (ParcelUuid uuid : uuids) {
                    if (uuid.getUuid().equals(BleConstants.FIND_ME_SERVICE)) {
                        App.addDevice(result);
                        dataListener.notifyAdapter();
                        break;
                    }
                }
            }
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public void onDevice(String address) {
        String action = "connect";
        Device result = App.getDevices().get(address);
        if (result.isConnected) {
            result.isAlarmed = !result.isAlarmed;
            if (result.isAlarmed) {
                action = "stopAlarm";
            } else {
                action = "startAlarm";
            }
        }
        activity.startService(new Intent(activity, BleService.class).setAction(action).putExtra("device", result));
    }

    @Override
    public void onRefresh() {
        dataListener.setRefreshing(true);
        App.getDevices().clear();
        dataListener.notifyAdapter();
        stopScan = new Runnable() {
            @Override
            public void run() {
                mHandler.removeCallbacks(stopScan);
                bluetoothAdapter.getBluetoothLeScanner().stopScan(MainActivityViewModel.this);
                dataListener.setRefreshing(false);
            }
        };
        mHandler.postDelayed(stopScan, SCAN_PERIOD);
        bluetoothAdapter.getBluetoothLeScanner().startScan(MainActivityViewModel.this);
    }

    public interface DataListener {
        void notifyAdapter();

        void setRefreshing(Boolean refreshing);
    }
}
