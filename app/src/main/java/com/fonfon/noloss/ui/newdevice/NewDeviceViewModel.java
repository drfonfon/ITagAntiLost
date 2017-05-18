package com.fonfon.noloss.ui.newdevice;

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
import com.fonfon.noloss.lib.BleService;
import com.fonfon.noloss.lib.Device;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class NewDeviceViewModel extends ScanCallback implements NewDevicesAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

    private final static int REQUEST_ENABLE_BT = 451;
    private static final long SCAN_PERIOD = 8000;

    private BluetoothAdapter bluetoothAdapter;

    private AppCompatActivity activity;
    private DataListener dataListener;

    private Handler handler;
    private Runnable stopScan;
    private List<String> currentAddresses = new ArrayList<>();

    NewDeviceViewModel(AppCompatActivity activity, DataListener dataListener) {
        this.activity = activity;
        this.dataListener = dataListener;

        handler = new Handler();
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

    void pause() {
        handler.removeCallbacks(stopScan);
        bluetoothAdapter.getBluetoothLeScanner().stopScan(NewDeviceViewModel.this);
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) resume();
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
        if (uuids != null) {
            for (ParcelUuid uuid : uuids) {
                if (uuid.getUuid().equals(BleService.FIND_ME_SERVICE)) {
                    if (!currentAddresses.contains(result.getDevice().getAddress()))
                        dataListener.onResult(result);
                    break;
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        Realm.getDefaultInstance()
                .where(Device.class)
                .findAllAsync()
                .addChangeListener(new RealmChangeListener<RealmResults<Device>>() {
                    @Override
                    public void onChange(RealmResults<Device> devices) {
                        currentAddresses.clear();
                        for (Device device : devices) {
                            currentAddresses.add(device.getAddress());
                        }
                        devices.removeAllChangeListeners();
                        dataListener.setRefresh(true);
                        dataListener.clear();
                        stopScan = new Runnable() {
                            @Override
                            public void run() {
                                handler.removeCallbacks(stopScan);
                                bluetoothAdapter.getBluetoothLeScanner().stopScan(NewDeviceViewModel.this);
                                dataListener.setRefresh(false);
                            }
                        };
                        handler.postDelayed(stopScan, SCAN_PERIOD);
                        bluetoothAdapter.getBluetoothLeScanner().startScan(NewDeviceViewModel.this);
                    }
                });
    }

    @Override
    public void onDevice(final ScanResult result) {
        Device.addToRealm(
                result,
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        activity.finish();
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        Toast.makeText(activity, R.string.add_device_error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    interface DataListener {
        void onResult(ScanResult scanResult);

        void clear();

        void setRefresh(boolean isRefreshing);
    }
}
