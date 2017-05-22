package com.fonfon.noloss.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fonfon.noloss.R;

public class BleViewModel {

    private final static int REQUEST_ENABLE_BT = 451;
    private static int REQUEST_LOCATION = 1;
    private static String[] PERMISSIONS_LOCATION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public AppCompatActivity activity;
    public BluetoothAdapter bluetoothAdapter;

    public BleViewModel(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void init() {
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) resume();
    }

    public void resume() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            activity.startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT
            );
        }
    }

    public void onDestroy() {
        activity = null;
        bluetoothAdapter = null;
    }
}
