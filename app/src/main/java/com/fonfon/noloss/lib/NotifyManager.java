package com.fonfon.noloss.lib;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.fonfon.noloss.R;
import com.fonfon.noloss.db.DeviceDB;

final class NotifyManager {

  static void showNotification(DeviceDB device, Context context, String message) {
    Bitmap bitmap = Device.getBitmapImage(device.getImage(), context.getResources());
    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
        .notify((int) Device.doHash(device.getAddress()),
            new NotificationCompat.Builder(context)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_find_key)
                .setContentTitle(context.getString(R.string.app_name))
                .setVibrate(new long[]{1000, 1000})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentText(message)
                .build());
  }
}
