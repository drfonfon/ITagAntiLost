package com.fonfon.itagantilost;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivityViewModel extends ScanCallback implements DevicesAdapter.Listener {

    private final static int REQUEST_ENABLE_BT = 451;
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private AppCompatActivity activity;
    private DataListener dataListener;

    private Handler mHandler;
    private Runnable stopScan;

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
    }

    public void resume() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            activity.startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT
            );
        }
    }

    public void pause() {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) resume();
    }

    public void onDestroy() {
        activity = null;
        dataListener = null;
    }

    public void search(View view) {
        dataListener.clear();
        stopScan = new Runnable() {
            @Override
            public void run() {
                mHandler.removeCallbacks(stopScan);
                bluetoothAdapter.getBluetoothLeScanner().stopScan(MainActivityViewModel.this);
            }
        };
        mHandler.postDelayed(stopScan, SCAN_PERIOD);
        bluetoothAdapter.getBluetoothLeScanner().startScan(MainActivityViewModel.this);
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        dataListener.onDevice(result);
    }

    @Override
    public void onDevice(ScanResult result) {
        activity.startActivity(new Intent(activity, DetailActivity.class).putExtra("device", result));
    }

    public interface DataListener {
        void onDevice(ScanResult scanResult);
        void clear();
    }
}
