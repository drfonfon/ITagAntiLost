package com.fonfon.noloss.ui.newdevice;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BitmapUtils;
import com.fonfon.noloss.BleService;
import com.fonfon.noloss.lib.Device;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

final class NewDeviceViewModel implements NewDevicesAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

    private final static int REQUEST_ENABLE_BT = 451;
    private static int REQUEST_LOCATION = 1;
    private static String[] PERMISSIONS_LOCATION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private AppCompatActivity activity;
    private BluetoothAdapter bluetoothAdapter;

    private static final long SCAN_PERIOD = 8000;

    private final DataListener dataListener;
    private final NewDevicesAdapter adapter;

    private final Handler handler;
    private final Runnable stopScan;
    private final List<String> currentAddresses = new ArrayList<>();

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getScanRecord() != null) {
                List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
                if (uuids != null) {
                    for (ParcelUuid uuid : uuids) {
                        if (uuid.getUuid().equals(BleService.FIND_ME_SERVICE)) {
                            if (!currentAddresses.contains(result.getDevice().getAddress()))
                                adapter.add(result.getDevice().getAddress(), result.getScanRecord().getDeviceName());
                            break;
                        }
                    }
                }
            }
        }
    };

    private String defaultImage;

    NewDeviceViewModel(AppCompatActivity activity, DataListener dataListener) {
        this.activity = activity;
        this.dataListener = dataListener;

        adapter = new NewDevicesAdapter(this);
        handler = new Handler();
        stopScan = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(stopScan);
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                NewDeviceViewModel.this.dataListener.setRefresh(false);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                defaultImage = BitmapUtils.bitmapToString(
                        BitmapFactory.decodeResource(
                                NewDeviceViewModel.this.activity.getResources(),
                                R.mipmap.ic_launcher
                        )
                );
            }
        }).run();
    }

    NewDevicesAdapter getAdapter() {
        return adapter;
    }

    void init() {
        if (checkPermission()) {
            if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                activity.finish();
            }

            BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            requestLocationPermission();
        }
    }


    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(activity, PERMISSIONS_LOCATION[0]) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSIONS_LOCATION[0])
                && ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSIONS_LOCATION[1])) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.ble_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    void resume() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            activity.startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT
            );
        } else {
            onRefresh();
        }
    }

    void pause() {
        handler.removeCallbacks(stopScan);
        if (bluetoothAdapter != null) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null && scanCallback != null) {
                scanner.stopScan(scanCallback);
            }
        }
    }

    void onActivityResult(int requestCode) {
        if (requestCode == REQUEST_ENABLE_BT) resume();
    }

    void onDestroy() {
        activity = null;
        bluetoothAdapter = null;
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
                        adapter.clear();
                        handler.postDelayed(stopScan, SCAN_PERIOD);
                        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
                    }
                });
    }

    @Override
    public void onDevice(final String address, final String name) {
        if (defaultImage != null) {
            Realm.getDefaultInstance()
                    .executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(new Device(address, name, defaultImage));
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            activity.finish();
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            Toast.makeText(activity, R.string.add_device_error, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(activity, R.string.add_device_error, Toast.LENGTH_SHORT).show();
        }
    }

    interface DataListener {
        void setRefresh(boolean isRefreshing);
    }
}
