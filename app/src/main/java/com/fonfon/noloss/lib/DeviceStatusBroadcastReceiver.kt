package com.fonfon.noloss.lib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fonfon.noloss.BleService

class DeviceStatusBroadcastReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent != null) {
      val address = intent.getStringExtra(BleService.DEVICE_ADDRESS)
      val action = intent.action
      if (action != null && address != null) {
        when (action) {
          BleService.DEVICE_BUTTON_CLICKED -> {
            //TODO
          }
          BleService.DEVICE_DISCONNECTED -> {
            //TODO
          }
        }
      }
    }
  }
}
