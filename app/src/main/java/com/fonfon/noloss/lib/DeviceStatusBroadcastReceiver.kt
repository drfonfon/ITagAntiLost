package com.fonfon.noloss.lib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fonfon.noloss.BleService
import com.fonfon.noloss.R
import com.fonfon.noloss.db.DbHelper
import com.fonfon.noloss.db.DeviceDB
import nl.qbusict.cupboard.CupboardFactory.cupboard

class DeviceStatusBroadcastReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent != null) {
      val address = intent.getStringExtra(BleService.DEVICE_ADDRESS)
      val action = intent.action
      if (action != null && address != null) {
        when (action) {
          BleService.DEVICE_BUTTON_CLICKED -> context.startService(Intent(context, LocationChangeService::class.java)
              .putExtra(BleService.DEVICE_ADDRESS, address)
          )
          BleService.DEVICE_DISCONNECTED -> {
            val device: DeviceDB = cupboard().withDatabase(DbHelper.getConnection(context))
                .query(DeviceDB::class.java)
                .withSelection("address = ?", address)
                .get()
            val statusText = context.resources.getString(R.string.status_disconnected)
            NotifyManager.showNotification(device, context, device.name + " " + statusText)
          }
        }
      }
    }
  }
}
