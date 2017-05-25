package com.fonfon.noloss.lib;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.fonfon.noloss.App;
import com.fonfon.noloss.R;

import io.realm.Realm;

public class DeviceDisconnectBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String visibleAddress = App.getInstance().getVisibleAddress();
        boolean addrr = visibleAddress != null && !visibleAddress.equals(App.ALL_ADDRESSES);
        if (intent != null && (visibleAddress == null || addrr)) {
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            addrr = visibleAddress != null && !visibleAddress.equals(address);
            if (address != null && (visibleAddress == null || addrr)) {
                final Device device = Realm.getDefaultInstance()
                        .where(Device.class)
                        .equalTo(Device.ADDRESS, address)
                        .findFirst();
                if (device != null) {
                    String statusText = context.getResources().getString(R.string.status_disconnected);;
                    Bitmap bitmap;
                    if (device.getAddress() != null) {
                        bitmap = BitmapUtils.stringToBitMap(device.getAddress());
                    } else {
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                    }

                    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                            .notify(device.doHash(),
                                    new NotificationCompat.Builder(context)
                                            .setLargeIcon(bitmap)
                                            .setSmallIcon(R.drawable.ic_find_key)
                                            .setContentTitle(context.getString(R.string.app_name))
                                            .setVibrate(new long[]{1000, 1000})
                                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                            .setContentText(device.getName() + " " + statusText)
                                            .build())
                    ;
                }
            }
        }
    }
}
