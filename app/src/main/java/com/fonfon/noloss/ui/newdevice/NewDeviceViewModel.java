package com.fonfon.noloss.ui.newdevice;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BleConstants;
import com.fonfon.noloss.lib.Constants;

import java.util.List;

public class NewDeviceViewModel extends ScanCallback implements NewDevicesAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

    private final static int REQUEST_ENABLE_BT = 451;
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter bluetoothAdapter;

    private AppCompatActivity activity;
    private DataListener dataListener;

    private Handler mHandler;
    private Runnable stopScan;


    NewDeviceViewModel(AppCompatActivity activity, DataListener dataListener) {
        this.activity = activity;
        this.dataListener = dataListener;

        mHandler = new Handler();
    }

    void init() {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            activity.finish();
        }

        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    void resume() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            activity.startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT
            );
        }
        onRefresh();
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        try {
            List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
            if (uuids != null) {
                for (ParcelUuid uuid : uuids) {
                    if (uuid.getUuid().equals(BleConstants.FIND_ME_SERVICE)) {
                        dataListener.onResult(result);
                        break;
                    }
                }
            }
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public void onRefresh() {
        dataListener.setRefresh(true);
        dataListener.clear();
        stopScan = new Runnable() {
            @Override
            public void run() {
                mHandler.removeCallbacks(stopScan);
                bluetoothAdapter.getBluetoothLeScanner().stopScan(NewDeviceViewModel.this);
                dataListener.setRefresh(false);
            }
        };
        mHandler.postDelayed(stopScan, SCAN_PERIOD);
        bluetoothAdapter.getBluetoothLeScanner().startScan(NewDeviceViewModel.this);
    }

    @Override
    public void onDevice(ScanResult result) {
        activity.setResult(Activity.RESULT_OK, new Intent().putExtra(Constants.DEVICE, result));
        activity.finish();
    }

    interface DataListener {
        void onResult(ScanResult scanResult);
        void clear();
        void setRefresh(boolean isRefreshing);
    }
}
