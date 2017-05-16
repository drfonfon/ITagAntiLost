package com.fonfon.noloss.ui.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.fonfon.noloss.App;
import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BleService;
import com.fonfon.noloss.lib.Constants;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.ui.SwipeToDismissHelper;
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity;

import java.util.ArrayList;

public class MainActivityViewModel implements DevicesAdapter.Listener, SwipeToDismissHelper.DeleteListener {

    private final static int REQUEST_ENABLE_BT = 451;
    private final static int RESULT_DEVICE = 55;

    private BluetoothAdapter bluetoothAdapter;

    private AppCompatActivity activity;
    private DataListener dataListener;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dataListener.notifyAdapter();
        }
    };

    MainActivityViewModel(AppCompatActivity activity, DataListener dataListener) {
        this.activity = activity;
        this.dataListener = dataListener;
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
        IntentFilter filter = new IntentFilter("bleUpdated");
        activity.registerReceiver(receiver, filter);
    }

    void pause() {
        activity.unregisterReceiver(receiver);
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) resume();
        if(resultCode == Activity.RESULT_OK && requestCode == RESULT_DEVICE) {
            App.addDevice((ScanResult) data.getParcelableExtra(Constants.DEVICE));
            dataListener.notifyAdapter();
        }
    }

    void onDestroy() {
        activity = null;
        dataListener = null;
    }

    @Override
    public void onDevice(String address) {
        String action = Constants.CONNECT;
        Device result = App.getDevices().get(address);

        switch (result.getStatus()) {
            case Device.DISCONNECTED:
                action = Constants.CONNECT;
                break;
            case Device.CONNECTION:
                break;
            case Device.CONNECTED:
                action = Constants.START_ALARM;
                result.setStatus(Device.ALARMED);
                break;
            case Device.ALARMED:
                action = Constants.STOP_ALARM;
                result.setStatus(Device.CONNECTED);
                break;
        }
        activity.startService(new Intent(activity, BleService.class).setAction(action).putExtra(Constants.DEVICE, result));
    }

    public void search(View view) {
        activity.startActivityForResult(new Intent(activity, NewDeviceActivity.class), RESULT_DEVICE);
        activity.overridePendingTransition(R.anim.slide_up_info,R.anim.no_change);
    }

    @Override
    public void onItemDelete(int position) {
        Device result = new ArrayList<>(App.getDevices().values()).get(position);
        App.getDevices().remove(result.getScanResult().getDevice().getAddress());
        activity.startService(new Intent(activity, BleService.class).setAction(Constants.DISONNECT).putExtra(Constants.DEVICE, result));
        dataListener.notifyAdapter();
    }

    interface DataListener {
        void notifyAdapter();
    }
}
