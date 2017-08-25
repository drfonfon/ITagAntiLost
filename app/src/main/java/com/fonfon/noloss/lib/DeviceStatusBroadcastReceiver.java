package com.fonfon.noloss.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fonfon.noloss.BleService;
import com.fonfon.noloss.R;
import com.fonfon.noloss.db.DbHelper;
import com.fonfon.noloss.db.DeviceDB;

import nl.nl2312.rxcupboard2.RxCupboard;

public class DeviceStatusBroadcastReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(final Context context, final Intent intent) {
    if (intent != null) {
      final String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
      final String action = intent.getAction();
      if (action != null && address != null) {
        switch (action) {
          case BleService.DEVICE_BUTTON_CLICKED:
            context.startService(new Intent(context, LocationChangeService.class)
                .putExtra(BleService.DEVICE_ADDRESS, address)
            );
            break;
          case BleService.DEVICE_DISCONNECTED:
            RxCupboard
                .withDefault(DbHelper.getConnection(context))
                .query(DeviceDB.class, "address = ?", address)
                .subscribe(device -> {
                  String statusText = context.getResources().getString(R.string.status_disconnected);
                  NotifyManager.showNotification(device, context, device.getName() + " " + statusText);
                });
            break;
        }
      }
    }
  }
}
