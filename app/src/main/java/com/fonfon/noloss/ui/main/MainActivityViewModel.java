package com.fonfon.noloss.ui.main;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BleService;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.ui.SwipeToDismissHelper;
import com.fonfon.noloss.ui.detail.DeviceDetailDialog;
import com.fonfon.noloss.ui.map.MapActivity;
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivityViewModel implements DevicesAdapter.Listener, SwipeToDismissHelper.DeleteListener {

    private final static int REQUEST_ENABLE_BT = 451;
    private static int REQUEST_LOCATION = 1;
    private static String[] PERMISSIONS_LOCATION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private BluetoothAdapter bluetoothAdapter;

    private AppCompatActivity activity;
    private DataListener dataListener;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            byte batteryLevel = intent.getByteExtra(BleService.BATTERY_LEVEL, (byte) 0);
            switch (action) {
                case BleService.DEVICE_CONNECTED:
                    dataListener.deviceConnected(address);
                    break;
                case BleService.DEVICE_DISCONNECTED:
                    dataListener.deviceDisconnected(address);
                    break;
                case BleService.BATTERY_LEVEL_UPDATED:
                    dataListener.deviceBatteryLevelUpdated(address, batteryLevel);
                    break;
                case BleService.CONNECTED_DEVICES:
                    String[] addresses = intent.getStringArrayExtra(BleService.DEVICES_ADDRESSES);
                    if (addresses != null) {
                        for (String addr: addresses) {
                            dataListener.deviceConnected(addr);
                        }
                    }
                    break;
            }
        }
    };

    MainActivityViewModel(AppCompatActivity activity, DataListener dataListener) {
        this.activity = activity;
        this.dataListener = dataListener;
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
                    .setTitle("Разрешения")
                    .setMessage("Нужно разрешение на определение координат, для работы bluetooth")
                    .setPositiveButton("ок", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
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
        }
        Realm.getDefaultInstance()
                .where(Device.class)
                .findAllAsync()
                .addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Device>>() {
                    @Override
                    public void onChange(RealmResults<Device> devices, OrderedCollectionChangeSet changeSet) {
                        dataListener.onDevices(devices);
                        for (Device device : devices) {
                            activity.startService(
                                    new Intent(activity, BleService.class)
                                            .setAction(BleService.CONNECT)
                                            .putExtra(BleService.DEVICE_ADDRESS, device.getAddress())
                            );
                        }
                        devices.removeAllChangeListeners();
                    }
                });
        IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);
        intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
        intentFilter.addAction(BleService.BATTERY_LEVEL_UPDATED);
        intentFilter.addAction(BleService.CONNECTED_DEVICES);
        activity.registerReceiver(receiver, intentFilter);
        activity.startService(
                new Intent(activity, BleService.class)
                        .setAction(BleService.CONNECTED_DEVICES)
        );
    }

    void pause() {
        activity.startService(
                new Intent(activity, BleService.class)
                        .setAction(BleService.STOP_SERVICE)
        );
        activity.unregisterReceiver(receiver);
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) resume();
    }

    void onDestroy() {
        activity = null;
    }

    @Override
    public void onAlarm(final String address, boolean alarm) {
        String action = alarm ? BleService.START_ALARM : BleService.STOP_ALARM;
        activity.startService(
                new Intent(activity, BleService.class)
                        .setAction(action)
                        .putExtra(BleService.DEVICE_ADDRESS, address)
        );
    }

    @Override
    public void onDeviceClick(Device device) {
        DeviceDetailDialog.getInstance(device).show(activity.getFragmentManager(), "dialog");
    }

    public void search(View view) {
        activity.startActivity(new Intent(activity, NewDeviceActivity.class));
        activity.overridePendingTransition(R.anim.slide_up_info, R.anim.no_change);
    }

    public void toMap(View view) {
        activity.startActivity(new Intent(activity, MapActivity.class));
    }

    @Override
    public void onItemDelete(final String tag) {
        dataListener.deviceDeleted(tag);
        activity.startService(
                new Intent(activity, BleService.class)
                        .setAction(BleService.DISCONNECT)
                        .putExtra(BleService.DEVICE_ADDRESS, tag)
        );
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Device device = realm.where(Device.class).equalTo("address", tag).findFirst();
                if(device != null) {
                    device.deleteFromRealm();
                }
            }
        });
    }

    public interface DataListener {
        void onDevices(RealmResults<Device> devices);

        void deviceConnected(String address);

        void deviceDisconnected(String address);

        void deviceBatteryLevelUpdated(String address, byte batteryLevel);

        void deviceDeleted(String address);
    }
}
