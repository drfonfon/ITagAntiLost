package com.fonfon.noloss.lib;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.fonfon.noloss.App;
import com.fonfon.noloss.R;
import com.fonfon.noloss.ui.main.MainActivity;

import java.util.HashMap;
import java.util.UUID;

public class BleService extends Service {

    public static final int NOTIFICATION_ID = 7007;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private HashMap<String, BluetoothGatt> bluetoothGatt = new HashMap<>();

    private BluetoothGattCharacteristic buttonCharacteristic;
    private BluetoothGattService immediateAlertService;
    private BluetoothGattCharacteristic alertCharacteristic;
    private BluetoothGattCharacteristic batteryCharacteristic;

    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (BluetoothGatt.GATT_SUCCESS == status) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    App.getDevices().get(gatt.getDevice().getAddress()).setStatus(Device.CONNECTED);
                    sendBroadcast(new Intent("bleUpdated"));
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close();
                    App.getDevices().get(gatt.getDevice().getAddress()).setStatus(Device.DISCONNECTED);
                    sendBroadcast(new Intent("bleUpdated"));
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
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

            if (characteristic.getValue() != null && characteristic.getValue().length > 0) {
                final byte level = characteristic.getValue()[0];
                App.getDevices().get(gatt.getDevice().getAddress()).setBatteryLevel(level);
                sendBroadcast(new Intent("bleUpdated"));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //TODO: device click
            sendBroadcast(new Intent("bleUpdated"));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            gatt.readCharacteristic(batteryCharacteristic);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (!initialize()) {
            //TODO
        }
        startForeground(NOTIFICATION_ID, getNotification("Запуск сервиса"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Device device = intent.getParcelableExtra(Constants.DEVICE);
        if (action != null && device != null) {
            switch (action) {
                case Constants.CONNECT:
                    connect(device.getScanResult());
                    break;
                case Constants.DISONNECT:
                    disconnect(device.getScanResult());
                    break;
                case Constants.START_ALARM:
                    immediateAlert(device.getScanResult().getDevice().getAddress(), 2);
                    break;
                case Constants.STOP_ALARM:
                    immediateAlert(device.getScanResult().getDevice().getAddress(), 0);
                    break;

            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private Notification getNotification(String text) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_list)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(resultPendingIntent).build();
    }

    public boolean connect(ScanResult result) {
        if (bluetoothAdapter == null) {
            return false;
        }

        bluetoothGatt.put(result.getDevice().getAddress(), result.getDevice().connectGatt(this, false, callback));
        return true;
    }

    public void disconnect() {
        if (bluetoothAdapter == null) {
            return;
        }
        for (BluetoothGatt gatt : bluetoothGatt.values()) {
            gatt.disconnect();
        }
    }

    public void disconnect(ScanResult result) {
        if (bluetoothAdapter == null) {
            return;
        }
        bluetoothGatt.get(result.getDevice().getAddress()).disconnect();
        bluetoothGatt.remove(result.getDevice().getAddress());
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

    public void immediateAlert(String address, int alertType) {
        if (immediateAlertService == null || immediateAlertService.getCharacteristics() == null || immediateAlertService.getCharacteristics().size() == 0) {
            //somethingGoesWrong();
            return;
        }
        final BluetoothGattCharacteristic characteristic = immediateAlertService.getCharacteristics().get(0);
        characteristic.setValue(alertType, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        bluetoothGatt.get(address).writeCharacteristic(characteristic);
    }
}
