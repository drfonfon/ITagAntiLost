package com.fonfon.itagantilost;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fonfon.itagantilost.lib.BleConstants;

import java.util.UUID;

public class DetailActivity extends AppCompatActivity {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    private BluetoothGattCharacteristic buttonCharacteristic;
    private BluetoothGattService immediateAlertService;
    private BluetoothGattCharacteristic alertCharacteristic;
    private BluetoothGattCharacteristic batteryCharacteristic;

    private Handler handler = new Handler();
    private Runnable trackRemoteRssi = null;

    private ScanResult result;

    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (BluetoothGatt.GATT_SUCCESS == status) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    Log.d("debug", "connected");
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close();
                    Log.d("debug", "disconnected");
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            launchTrackingRemoteRssi(gatt);
            Log.d("debug", "onServicesDiscovered");

            for (BluetoothGattService service : gatt.getServices()) {
                if (BleConstants.IMMEDIATE_ALERT_SERVICE.equals(service.getUuid())) {
                    immediateAlertService = service;
                    alertCharacteristic = getCharacteristic(gatt, BleConstants.IMMEDIATE_ALERT_SERVICE, BleConstants.ALERT_LEVEL_CHARACTERISTIC);
                    gatt.readCharacteristic(alertCharacteristic);
                }

                if (BleConstants.BATTERY_SERVICE.equals(service.getUuid())) {
                    batteryCharacteristic = service.getCharacteristics().get(0);
                    gatt.readCharacteristic(batteryCharacteristic);
                }

                if (BleConstants.FIND_ME_SERVICE.equals(service.getUuid())) {
                    if (!service.getCharacteristics().isEmpty()) {
                        buttonCharacteristic = service.getCharacteristics().get(0);
                        setCharacteristicNotification(gatt, buttonCharacteristic, true);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("debug", "onCharacteristicRead");

            if (characteristic.getValue() != null && characteristic.getValue().length > 0) {
                final byte level = characteristic.getValue()[0];
                textView.setText(String.valueOf(level));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("debug", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Toast.makeText(getApplicationContext(), "onCharacteristicChanged", Toast.LENGTH_SHORT).show();
            Log.d("debug", "onCharacteristicChanged");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            gatt.readCharacteristic(batteryCharacteristic);
            Log.d("debug", "onDescriptorWrite");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d("debug", "onReadRemoteRssi");

            Log.d("debug", String.valueOf(getDistance(rssi, status)));
        }
    };

    double getDistance(int rssi, int txPower) {
    /*
     * RSSI = TxPower - 10 * n * lg(d)
     * n = 2 (in free space)
     *
     * d = 10 ^ ((TxPower - RSSI) / (10 * n))
     */
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    private int alertType = 0;
    private AppCompatTextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        result = getIntent().getParcelableExtra("device");
        if (result == null) {
            finish();
        }

        if (!initialize()) {
            finish();
        }

        textView = (AppCompatTextView) findViewById(R.id.text);

        findViewById(R.id.alert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                immediateAlert(alertType);

                if (alertType == 0) {
                    alertType = 2;
                } else {
                    alertType = 0;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }

    //УРОВЕНЬ СИГНАЛА
    private void launchTrackingRemoteRssi(final BluetoothGatt gatt) {
        if (trackRemoteRssi != null) {
            handler.removeCallbacks(trackRemoteRssi);
        }

        trackRemoteRssi = new Runnable() {
            @Override
            public void run() {
                gatt.readRemoteRssi();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(trackRemoteRssi);
    }

    public boolean connect() {
        if (bluetoothAdapter == null) {
            return false;
        }
        bluetoothGatt = result.getDevice().connectGatt(this, false, callback);
        return true;
    }

    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.disconnect();
    }

    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter != null;
    }

    private void setCharacteristicNotification(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, boolean flag) {
        bluetoothgatt.setCharacteristicNotification(bluetoothgattcharacteristic, flag);
        if (BleConstants.FIND_ME_CHARACTERISTIC.equals(bluetoothgattcharacteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = bluetoothgattcharacteristic.getDescriptor(BleConstants.CLIENT_CHARACTERISTIC_CONFIG);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothgatt.writeDescriptor(descriptor);
            }
        }
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt bluetoothgatt, UUID serviceUuid, UUID characteristicUuid) {
        if (bluetoothgatt != null) {
            BluetoothGattService service = bluetoothgatt.getService(serviceUuid);
            if (service != null)
                return service.getCharacteristic(characteristicUuid);
        }
        return null;
    }

    public void immediateAlert(int alertType) {
        if (immediateAlertService == null || immediateAlertService.getCharacteristics() == null || immediateAlertService.getCharacteristics().size() == 0) {
            //somethingGoesWrong();
            return;
        }
        final BluetoothGattCharacteristic characteristic = immediateAlertService.getCharacteristics().get(0);
        characteristic.setValue(alertType, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        bluetoothGatt.writeCharacteristic(characteristic);
    }
}
