package com.fonfon.noloss.lib;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.fonfon.noloss.R;
import com.fonfon.noloss.ui.main.MainActivity;

import java.util.HashMap;
import java.util.UUID;

public final class BleService extends Service {

    public static void connect(Context context, String address) {
        context.startService(
                new Intent(context, BleService.class)
                        .setAction(BleService.CONNECT)
                        .putExtra(BleService.DEVICE_ADDRESS, address)
        );
    }

    public static void alert(Context context, String address, boolean alert) {
        context.startService(
                new Intent(context, BleService.class)
                        .setAction(alert ? BleService.START_ALARM : BleService.STOP_ALARM)
                        .putExtra(BleService.DEVICE_ADDRESS, address)
        );
    }

    public static void stopService(Context context) {
        context.startService(
                new Intent(context, BleService.class)
                        .setAction(BleService.STOP_SERVICE)
        );
    }

    public static void checkConnectedDevices(Context context) {
        context.startService(
                new Intent(context, BleService.class)
                        .setAction(BleService.CONNECTED_DEVICES)
        );
    }

    public static void checkBattery(Context context) {
        context.startService(
                new Intent(context, BleService.class)
                        .setAction(BleService.CHECK_BATTERY)
        );
    }

    public static void disconnect(Context context, String address) {
        context.startService(
                new Intent(context, BleService.class)
                        .setAction(BleService.DISCONNECT)
                        .putExtra(BleService.DEVICE_ADDRESS, address)
        );
    }

    /**
     * Button BLE service
     */
    public static final UUID FIND_ME_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID FIND_ME_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    /**
     * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.immediate_alert.xml">immediate_alert</a>
     */
    public static final UUID IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID ALERT_LEVEL_CHARACTERISTIC = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    /**
     * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.battery_service.xml">battery_service</a>
     */
    public static final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    /**
     * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml">client_characteristic_configuration</a>
     */
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String BATTERY_LEVEL = "BATTERY_LEVEL";
    public static final String DEVICES_ADDRESSES = "DEVICES_ADDRESSES";

    public static final String CONNECT = "CONNECT";
    public static final String START_ALARM = "START_ALARM";
    public static final String STOP_ALARM = "STOP_ALARM";
    public static final String DISCONNECT = "DISCONNECT";
    public static final String CONNECTED_DEVICES = "CONNECTED_DEVICES";
    public static final String CHECK_BATTERY = "CHECK_BATTERY";
    public static final String STOP_SERVICE = "STOP_SERVICE_?";

    public static final String DEVICE_CONNECTED = "DEVICE_CONNECTED";
    public static final String DEVICE_DISCONNECTED = "DEVICE_DISCONNECTED";
    public static final String BATTERY_LEVEL_UPDATED = "BATTERY_LEVEL_UPDATED";
    public static final String DEVICE_BUTTON_CLICKED = "DEVICE_BUTTON_CLICKED";

    public static final int NOTIFICATION_ID = 7007;
    public static final int NOTIFICATION_ERROR_ID = 7008;

    public static final int ALERT_STOP = 0;
    public static final int ALERT_START = 2;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private HashMap<String, BluetoothGatt> bluetoothGatt = new HashMap<>();

    private BluetoothGattCharacteristic buttonCharacteristic;
    private BluetoothGattService immediateAlertService;
    private BluetoothGattCharacteristic alertCharacteristic;
    private BluetoothGattCharacteristic batteryCharacteristic;

    private BluetoothGattCallback callback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(
                final BluetoothGatt gatt,
                int status,
                int newState
        ) {
            if (BluetoothGatt.GATT_SUCCESS == status) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    onConnected(gatt);
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    onDisconnected(gatt);
                }
            } else {
                onDisconnected(gatt);
            }
        }

        private void onConnected(BluetoothGatt gatt) {
            gatt.discoverServices();
            bluetoothGatt.put(gatt.getDevice().getAddress(), gatt);
            sendBroadcast(
                    new Intent(DEVICE_CONNECTED)
                            .putExtra(DEVICE_ADDRESS, gatt.getDevice().getAddress())
            );
        }

        private void onDisconnected(BluetoothGatt gatt) {
            gatt.close();
            BluetoothGatt gat = bluetoothGatt.get(gatt.getDevice().getAddress());
            if (gat != null) {
                bluetoothGatt.remove(gat.getDevice().getName());
            }
            if (bluetoothGatt.size() == 0) {
                stopSelf();
            }
            sendBroadcast(
                    new Intent(DEVICE_DISCONNECTED)
                            .putExtra(DEVICE_ADDRESS, gatt.getDevice()
                                    .getAddress())
            );
        }

        @Override
        public void onServicesDiscovered(
                BluetoothGatt gatt,
                int status
        ) {
            for (BluetoothGattService service : gatt.getServices()) {
                if (IMMEDIATE_ALERT_SERVICE.equals(service.getUuid())) {
                    immediateAlertService = service;
                    alertCharacteristic = getCharacteristic(
                            gatt,
                            IMMEDIATE_ALERT_SERVICE,
                            ALERT_LEVEL_CHARACTERISTIC
                    );
                    gatt.readCharacteristic(alertCharacteristic);
                }

                if (BATTERY_SERVICE.equals(service.getUuid())) {
                    batteryCharacteristic = service.getCharacteristics().get(0);
                    gatt.readCharacteristic(batteryCharacteristic);
                }

                if (FIND_ME_SERVICE.equals(service.getUuid())) {
                    if (!service.getCharacteristics().isEmpty()) {
                        buttonCharacteristic = service.getCharacteristics().get(0);
                        setCharacteristicNotification(gatt, buttonCharacteristic, true);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status
        ) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (characteristic.getValue() != null && characteristic.getValue().length > 0) {
                sendBroadcast(
                        new Intent(BATTERY_LEVEL_UPDATED)
                                .putExtra(DEVICE_ADDRESS, gatt.getDevice().getAddress())
                                .putExtra(BATTERY_LEVEL, characteristic.getValue()[0])
                );
            }
        }

        @Override
        public void onCharacteristicChanged(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (buttonCharacteristic.getUuid().equals(characteristic.getUuid())) {
                sendBroadcast(
                        new Intent(DEVICE_BUTTON_CLICKED)
                                .putExtra(DEVICE_ADDRESS, gatt.getDevice().getAddress())
                );
            }
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
        if (initialize()) {
            startForeground(NOTIFICATION_ID, getNotification(getString(R.string.working)));
        } else {
            error(getString(R.string.start_service_error));
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        if (intent != null) {
            String action = intent.getAction();
            String address = intent.getStringExtra(DEVICE_ADDRESS);

            if (action != null) {
                switch (action) {
                    case CONNECTED_DEVICES:
                        String[] addrecces = bluetoothGatt.keySet().toArray(new String[bluetoothGatt.size()]);
                        sendBroadcast(
                                new Intent(CONNECTED_DEVICES)
                                        .putExtra(DEVICES_ADDRESSES, addrecces)
                        );
                        break;
                    case STOP_SERVICE:
                        if (bluetoothGatt.size() == 0) {
                            stopSelf();
                        }
                        break;
                    default:
                        if (address != null && bluetoothAdapter != null) {
                            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                            if (bluetoothDevice != null) {
                                switch (action) {
                                    case CONNECT:
                                        connect(bluetoothDevice);
                                        break;
                                    case DISCONNECT:
                                        disconnect(bluetoothDevice);
                                        break;
                                    case START_ALARM:
                                        immediateAlert(bluetoothDevice.getAddress(), ALERT_START);
                                        break;
                                    case STOP_ALARM:
                                        immediateAlert(bluetoothDevice.getAddress(), ALERT_STOP);
                                        break;
                                    case CHECK_BATTERY:
                                        BluetoothGatt gatt = bluetoothGatt.get(bluetoothDevice.getAddress());
                                        if (gatt != null && batteryCharacteristic != null) {
                                            gatt.readCharacteristic(batteryCharacteristic);
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(
            Intent intent
    ) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter != null;
    }

    private boolean connect(
            @NonNull BluetoothDevice result
    ) {
        if (bluetoothAdapter == null) {
            return false;
        }
        result.connectGatt(this, false, callback);
        return true;
    }

    private void disconnect() {
        if (bluetoothAdapter == null) {
            return;
        }
        for (BluetoothGatt gatt : bluetoothGatt.values()) {
            gatt.disconnect();
        }
        bluetoothGatt.clear();
    }

    private void disconnect(
            @NonNull BluetoothDevice result
    ) {
        if (bluetoothAdapter == null) {
            return;
        }
        if (bluetoothGatt.get(result.getAddress()) != null) {
            bluetoothGatt.get(result.getAddress()).disconnect();
            bluetoothGatt.remove(result.getAddress());
        }
    }

    private void setCharacteristicNotification(
            @NonNull BluetoothGatt bluetoothgatt,
            @NonNull BluetoothGattCharacteristic bluetoothgattcharacteristic,
            boolean flag
    ) {
        bluetoothgatt.setCharacteristicNotification(bluetoothgattcharacteristic, flag);
        if (FIND_ME_CHARACTERISTIC.equals(bluetoothgattcharacteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = bluetoothgattcharacteristic.getDescriptor(
                    CLIENT_CHARACTERISTIC_CONFIG
            );
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothgatt.writeDescriptor(descriptor);
            }
        }
    }

    private BluetoothGattCharacteristic getCharacteristic(
            @NonNull BluetoothGatt bluetoothgatt,
            @NonNull UUID serviceUuid,
            @NonNull UUID characteristicUuid
    ) {
        BluetoothGattService service = bluetoothgatt.getService(serviceUuid);
        if (service != null)
            return service.getCharacteristic(characteristicUuid);
        return null;
    }

    private void immediateAlert(
            @NonNull String address,
            int alertValue
    ) {
        if (immediateAlertService == null
                || immediateAlertService.getCharacteristics() == null
                || immediateAlertService.getCharacteristics().size() == 0) {
            error(getString(R.string.alert_error));
            return;
        }
        final BluetoothGattCharacteristic characteristic = immediateAlertService.getCharacteristics().get(0);
        characteristic.setValue(alertValue, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        BluetoothGatt gatt = bluetoothGatt.get(address);
        if (gatt != null) {
            gatt.writeCharacteristic(characteristic);
        }
    }

    private void error(
            @NonNull String text
    ) {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ERROR_ID, getNotification(text));
    }

    private Notification getNotification(
            @NonNull String text
    ) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_find_key)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(resultPendingIntent).build();
    }

}
