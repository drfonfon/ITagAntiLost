package com.fonfon.noloss.lib

import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.support.v4.app.NotificationCompat

import com.fonfon.noloss.R
import com.fonfon.noloss.db.DeviceDB

internal object NotifyManager {

  fun showNotification(device: DeviceDB, context: Context, message: String) {
    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        .notify(Device.doHash(device.address!!).toInt(),
            NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_find_key)
                .setContentTitle(context.getString(R.string.app_name))
                .setVibrate(longArrayOf(1000, 1000))
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentText(message)
                .build())
  }
}
