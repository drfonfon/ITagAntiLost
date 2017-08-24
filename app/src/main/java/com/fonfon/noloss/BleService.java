package com.fonfon.noloss;

import android.app.Notification;
import android.app.NotificationManager;
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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.fonfon.noloss.ui.main.DevicesActivity;

import java.util.HashMap;
import java.util.UUID;

public final class BleService extends Service {

  /**
   * Connect device from address
   *
   * @param context start context
   * @param address BLE device address FF:FF:FF:FF:FF:FF format
   */
  public static void connect(Context context, String address) {
    if (context != null && address != null) {
      context.startService(new Intent(context, BleService.class)
          .setAction(BleService.CONNECT)
          .putExtra(BleService.DEVICE_ADDRESS, address)
      );
    }
  }

  /**
   * Alert device from address
   *
   * @param context start context
   * @param address BLE device address FF:FF:FF:FF:FF:FF format
   * @param alert   alert - on/off
   */
  public static void alert(Context context, String address, boolean alert) {
    if (context != null && address != null) {
      context.startService(new Intent(context, BleService.class)
          .setAction(alert ? BleService.START_ALARM : BleService.STOP_ALARM)
          .putExtra(BleService.DEVICE_ADDRESS, address)
      );
    }
  }

  /**
   * Disconnect device from address
   *
   * @param context start context
   * @param address BLE device address FF:FF:FF:FF:FF:FF format
   */
  public static void disconnect(Context context, String address) {
    if (context != null && address != null) {
      context.startService(new Intent(context, BleService.class)
          .setAction(BleService.DISCONNECT)
          .putExtra(BleService.DEVICE_ADDRESS, address)
      );
    }
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
   * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml">client_characteristic_configuration</a>
   */
  public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

  public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

  public static final String CONNECT = "CONNECT";
  public static final String START_ALARM = "START_ALARM";
  public static final String STOP_ALARM = "STOP_ALARM";
  public static final String DISCONNECT = "DISCONNECT";

  public static final String DEVICE_CONNECTED = "com.fonfon.noloss.DEVICE_CONNECTED";
  public static final String DEVICE_DISCONNECTED = "com.fonfon.noloss.DEVICE_DISCONNECTED";
  public static final String DEVICE_BUTTON_CLICKED = "com.fonfon.noloss.DEVICE_BUTTON_CLICKED";

  public static final int NOTIFICATION_ID = 7007;
  public static final int NOTIFICATION_ERROR_ID = 7008;

  public static final int ALERT_STOP = 0;
  public static final int ALERT_START = 2;

  private final class BlePair {
    BluetoothGatt gatt;
    BluetoothGattCharacteristic alertCharacteristic;

    BlePair(BluetoothGatt gatt) {
      this.gatt = gatt;
    }
  }

  private final HashMap<String, BlePair> bluetoothGatt = new HashMap<>();

  private BluetoothManager bluetoothManager;
  private BluetoothAdapter bluetoothAdapter;

  private BluetoothGattCharacteristic buttonCharacteristic;

  private final BluetoothGattCallback callback = new BluetoothGattCallback() {

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
      if (BluetoothGatt.GATT_SUCCESS == status) {
        switch (newState) {
          case BluetoothProfile.STATE_CONNECTED:
            onConnected(gatt);
            break;
          case BluetoothProfile.STATE_DISCONNECTED:
            onDisconnected(gatt);
            break;
        }
      } else {
        onDisconnected(gatt);
      }
    }

    private void onConnected(BluetoothGatt gatt) {
      gatt.discoverServices();
      bluetoothGatt.put(gatt.getDevice().getAddress(), new BlePair(gatt));
      sendBroadcast(
          new Intent(DEVICE_CONNECTED)
              .putExtra(DEVICE_ADDRESS, gatt.getDevice().getAddress())
      );
    }

    private void onDisconnected(BluetoothGatt gatt) {
      gatt.close();
      BlePair pair = bluetoothGatt.get(gatt.getDevice().getAddress());
      if (pair != null) {
        bluetoothGatt.remove(gatt.getDevice().getAddress());
        sendBroadcast(
            new Intent(DEVICE_DISCONNECTED)
                .putExtra(DEVICE_ADDRESS, gatt.getDevice().getAddress())
        );
      }
      if (bluetoothGatt.size() == 0) {
        stopSelf();
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      for (BluetoothGattService service : gatt.getServices()) {
        if (IMMEDIATE_ALERT_SERVICE.equals(service.getUuid())) {
          BlePair pair = bluetoothGatt.get(gatt.getDevice().getAddress());
          if (pair != null) {
            pair.alertCharacteristic = getCharacteristic(
                gatt,
                IMMEDIATE_ALERT_SERVICE,
                ALERT_LEVEL_CHARACTERISTIC
            );
            gatt.readCharacteristic(pair.alertCharacteristic);
          }
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
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && bluetoothAdapter != null) {
      String action = intent.getAction();
      String address = intent.getStringExtra(DEVICE_ADDRESS);

      if (action != null && address != null) {
        switch (action) {
          case CONNECT:
            connect(address);
            break;
          case DISCONNECT:
            disconnect(address);
            break;
          case START_ALARM:
            immediateAlert(address, ALERT_START);
            break;
          case STOP_ALARM:
            immediateAlert(address, ALERT_STOP);
            break;
        }
      } else if (bluetoothGatt.size() == 0) {
        stopSelf();
      }
    } else if (bluetoothGatt.size() == 0) {
      stopSelf();
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
    disconnect(false);
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
      @NonNull String address
  ) {
    if (bluetoothAdapter == null) {
      return false;
    }
    if (bluetoothGatt.get(address) != null) {
      sendBroadcast(
          new Intent(DEVICE_CONNECTED)
              .putExtra(DEVICE_ADDRESS, address)
      );
    } else {
      bluetoothAdapter.getRemoteDevice(address).connectGatt(this, false, callback);
    }
    return true;
  }

  private void disconnect(boolean isNotify) {
    if (bluetoothAdapter == null) {
      return;
    }
    for (BlePair pair : bluetoothGatt.values()) {
      pair.gatt.disconnect();
      if (isNotify) {
        sendBroadcast(
            new Intent(DEVICE_DISCONNECTED)
                .putExtra(DEVICE_ADDRESS, pair.gatt.getDevice().getAddress())
        );
      }
    }
    bluetoothGatt.clear();
    stopSelf();
  }

  private void disconnect(
      @NonNull String address
  ) {
    if (bluetoothAdapter == null) {
      return;
    }
    if (bluetoothGatt.get(address) != null) {
      bluetoothGatt.get(address).gatt.disconnect();
      bluetoothGatt.remove(address);
    }
    if (bluetoothGatt.size() == 0) {
      stopSelf();
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
      String address,
      int alertValue
  ) {
    if (address != null) {
      BlePair pair = bluetoothGatt.get(address);
      if (pair != null && pair.gatt != null && pair.alertCharacteristic != null) {
        pair.alertCharacteristic.setValue(alertValue, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        pair.gatt.writeCharacteristic(pair.alertCharacteristic);
      }
    }
  }

  private void error(String text) {
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        .notify(NOTIFICATION_ERROR_ID, getNotification(text));
  }

  private Notification getNotification(String text) {
    Intent resultIntent = new Intent(this, DevicesActivity.class);
    PendingIntent resultPendingIntent = PendingIntent.getActivity(
        this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
    );
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    return new NotificationCompat.Builder(this)
        .setLargeIcon(bitmap)
        .setSmallIcon(R.drawable.ic_find_key)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(text)
        .setContentIntent(resultPendingIntent).build();
  }

}