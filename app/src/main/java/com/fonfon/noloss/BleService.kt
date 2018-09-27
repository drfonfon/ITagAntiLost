package com.fonfon.noloss

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.fonfon.noloss.ui.MainActivity
import java.util.*

class BleService : Service() {

  private val bluetoothGatt = HashMap<String, BlePair>()

  private var bluetoothManager: BluetoothManager? = null
  private var bluetoothAdapter: BluetoothAdapter? = null

  private var buttonCharacteristic: BluetoothGattCharacteristic? = null

  private val callback = object : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
      if (BluetoothGatt.GATT_SUCCESS == status) {
        when (newState) {
          BluetoothProfile.STATE_CONNECTED -> onConnected(gatt)
          BluetoothProfile.STATE_DISCONNECTED -> onDisconnected(gatt)
        }
      } else {
        onDisconnected(gatt)
      }
    }

    private fun onConnected(gatt: BluetoothGatt) {
      gatt.discoverServices()
      bluetoothGatt[gatt.device.address] = BlePair(gatt)
      sendBroadcast(Intent(DEVICE_CONNECTED).putExtra(DEVICE_ADDRESS, gatt.device.address))
    }

    private fun onDisconnected(gatt: BluetoothGatt) {
      gatt.close()
      bluetoothGatt[gatt.device.address]?.let {
        bluetoothGatt.remove(gatt.device.address)
        sendBroadcast(Intent(DEVICE_DISCONNECTED).putExtra(DEVICE_ADDRESS, gatt.device.address))
      }
      if (bluetoothGatt.isEmpty()) {
        stopSelf()
      }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
      gatt.services.forEach {
        if (IMMEDIATE_ALERT_SERVICE == it.uuid) {
          bluetoothGatt[gatt.device.address]?.let {
            it.alertCharacteristic = getCharacteristic(
                gatt,
                IMMEDIATE_ALERT_SERVICE,
                ALERT_LEVEL_CHARACTERISTIC
            )
            gatt.readCharacteristic(it.alertCharacteristic)
          }
        }

        if (FIND_ME_SERVICE == it.uuid) {
          if (!it.characteristics.isEmpty()) {
            buttonCharacteristic = it.characteristics[0]
            setCharacteristicNotification(gatt, buttonCharacteristic!!, true)
          }
        }
      }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
      super.onCharacteristicChanged(gatt, characteristic)
      if (buttonCharacteristic!!.uuid == characteristic.uuid) {
        sendBroadcast(
            Intent(DEVICE_BUTTON_CLICKED)
                .putExtra(DEVICE_ADDRESS, gatt.device.address)
        )
      }
    }
  }

  private inner class BlePair internal constructor(internal var gatt: BluetoothGatt?) {
    internal var alertCharacteristic: BluetoothGattCharacteristic? = null
  }

  override fun onCreate() {
    super.onCreate()

    if (initialize()) {
      startForeground(NOTIFICATION_ID, getNotification(getString(R.string.working)))
    } else {
      error(getString(R.string.start_service_error))
      stopSelf()
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent != null && bluetoothAdapter != null) {
      val action = intent.action
      val address = intent.getStringExtra(DEVICE_ADDRESS)

      if (action != null && address != null) {
        when (action) {
          CONNECT -> connect(address)
          DISCONNECT -> disconnect(address)
          START_ALARM -> immediateAlert(address, ALERT_START)
          STOP_ALARM -> immediateAlert(address, ALERT_STOP)
        }
      } else if (bluetoothGatt.size == 0) {
        stopSelf()
      }
    } else if (bluetoothGatt.size == 0) {
      stopSelf()
    }

    return Service.START_STICKY
  }

  override fun onBind(intent: Intent): IBinder? {
    throw UnsupportedOperationException("Not yet implemented")
  }

  override fun onDestroy() {
    super.onDestroy()
    disconnect(false)
  }

  private fun initialize(): Boolean {
    if (bluetoothManager == null) {
      bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
      if (bluetoothManager == null) {
        return false
      }
    }

    bluetoothAdapter = bluetoothManager!!.adapter
    return bluetoothAdapter != null
  }

  private fun connect(address: String): Boolean {
    if (bluetoothAdapter == null) {
      return false
    }
    if (bluetoothGatt[address] != null) {
      sendBroadcast(
          Intent(DEVICE_CONNECTED)
              .putExtra(DEVICE_ADDRESS, address)
      )
    } else {
      bluetoothAdapter!!.getRemoteDevice(address).connectGatt(this, false, callback)
    }
    return true
  }

  private fun disconnect(isNotify: Boolean) {
    if (bluetoothAdapter == null) {
      return
    }
    for (pair in bluetoothGatt.values) {
      pair.gatt!!.disconnect()
      if (isNotify) {
        sendBroadcast(
            Intent(DEVICE_DISCONNECTED)
                .putExtra(DEVICE_ADDRESS, pair.gatt!!.device.address)
        )
      }
    }
    bluetoothGatt.clear()
    stopSelf()
  }

  private fun disconnect(
      address: String
  ) {
    if (bluetoothAdapter == null) {
      return
    }
    if (bluetoothGatt[address] != null) {
      bluetoothGatt[address]!!.gatt!!.disconnect()
      bluetoothGatt.remove(address)
    }
    if (bluetoothGatt.size == 0) {
      stopSelf()
    }
  }

  private fun setCharacteristicNotification(
      bluetoothgatt: BluetoothGatt,
      bluetoothgattcharacteristic: BluetoothGattCharacteristic,
      flag: Boolean
  ) {
    bluetoothgatt.setCharacteristicNotification(bluetoothgattcharacteristic, flag)
    if (FIND_ME_CHARACTERISTIC == bluetoothgattcharacteristic.uuid) {
      val descriptor = bluetoothgattcharacteristic.getDescriptor(
          CLIENT_CHARACTERISTIC_CONFIG
      )
      if (descriptor != null) {
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        bluetoothgatt.writeDescriptor(descriptor)
      }
    }
  }

  private fun getCharacteristic(bluetoothGatt: BluetoothGatt, serviceUuid: UUID, characteristicUuid: UUID): BluetoothGattCharacteristic {
    return bluetoothGatt.getService(serviceUuid).getCharacteristic(characteristicUuid)
  }

  private fun immediateAlert(
      address: String?,
      alertValue: Int
  ) {
    if (address != null) {
      val pair = bluetoothGatt[address]
      if (pair?.gatt != null && pair.alertCharacteristic != null) {
        pair.alertCharacteristic!!.setValue(alertValue, BluetoothGattCharacteristic.FORMAT_UINT8, 0)
        pair.gatt!!.writeCharacteristic(pair.alertCharacteristic)
      }
    }
  }

  private fun error(text: String) {
    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        .notify(NOTIFICATION_ERROR_ID, getNotification(text))
  }

  private fun getNotification(text: String): Notification {
    val resultIntent = Intent(this, MainActivity::class.java)
    val resultPendingIntent = PendingIntent.getActivity(
        this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )
    val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
    return NotificationCompat.Builder(this)
        .setLargeIcon(bitmap)
        .setSmallIcon(R.drawable.ic_find_key)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(text)
        .setContentIntent(resultPendingIntent).build()
  }

  companion object {

    /**
     * Connect device from address
     *
     * @param context start context
     * @param address BLE device address FF:FF:FF:FF:FF:FF format
     */
    fun connect(context: Context?, address: String?) {
      if (context != null && address != null) {
        context.startService(Intent(context, BleService::class.java)
            .setAction(BleService.CONNECT)
            .putExtra(BleService.DEVICE_ADDRESS, address)
        )
      }
    }

    /**
     * Alert device from address
     *
     * @param context start context
     * @param address BLE device address FF:FF:FF:FF:FF:FF format
     * @param alert   alert - on/off
     */
    fun alert(context: Context?, address: String?, alert: Boolean) {
      if (context != null && address != null) {
        context.startService(Intent(context, BleService::class.java)
            .setAction(if (alert) BleService.START_ALARM else BleService.STOP_ALARM)
            .putExtra(BleService.DEVICE_ADDRESS, address)
        )
      }
    }

    /**
     * Disconnect device from address
     *
     * @param context start context
     * @param address BLE device address FF:FF:FF:FF:FF:FF format
     */
    fun disconnect(context: Context?, address: String?) {
      if (context != null && address != null) {
        context.startService(Intent(context, BleService::class.java)
            .setAction(BleService.DISCONNECT)
            .putExtra(BleService.DEVICE_ADDRESS, address)
        )
      }
    }

    /**
     * Button BLE service
     */
    val FIND_ME_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    val FIND_ME_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    /**
     * @see [immediate_alert](https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.immediate_alert.xml)
     */
    val IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
    val ALERT_LEVEL_CHARACTERISTIC = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")

    /**
     * @see [client_characteristic_configuration](https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml)
     */
    val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    val DEVICE_ADDRESS = "DEVICE_ADDRESS"

    val CONNECT = "CONNECT"
    val START_ALARM = "START_ALARM"
    val STOP_ALARM = "STOP_ALARM"
    val DISCONNECT = "DISCONNECT"

    val DEVICE_CONNECTED = "com.fonfon.noloss.DEVICE_CONNECTED"
    val DEVICE_DISCONNECTED = "com.fonfon.noloss.DEVICE_DISCONNECTED"
    val DEVICE_BUTTON_CLICKED = "com.fonfon.noloss.DEVICE_BUTTON_CLICKED"

    val NOTIFICATION_ID = 7007
    val NOTIFICATION_ERROR_ID = 7008

    val ALERT_STOP = 0
    val ALERT_START = 2

    val GATT_MAX_CONNECTED_DEVICES = 15
  }

}