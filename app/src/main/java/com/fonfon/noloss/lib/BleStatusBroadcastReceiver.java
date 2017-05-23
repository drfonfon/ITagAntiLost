package com.fonfon.noloss.lib;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.fonfon.noloss.App;
import com.fonfon.noloss.R;

import io.realm.Realm;

public class BleStatusBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && !App.getInstance().isActivityVisible()) {
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            String action = intent.getAction();
            if (address != null && action != null) {
                final Device device = Realm.getDefaultInstance()
                        .where(Device.class)
                        .equalTo(Device.ADDRESS, address)
                        .findFirst();
                if (device != null) {
                    String statusText = null;
                    switch (action) {
                        case BleService.DEVICE_CONNECTED:
                            statusText = context.getResources().getString(R.string.status_connected);
                            break;
                        case BleService.DEVICE_DISCONNECTED:
                            statusText = context.getResources().getString(R.string.status_disconnected);
                            break;
                    }
                    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                            .notify(device.doHash(),
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.ic_find_key)
                                            .setContentTitle(context.getString(R.string.app_name))
                                            .setContentText(device.getName() + " " + statusText)
                                            .build()
                            );
                }
            }
        }
    }
}
