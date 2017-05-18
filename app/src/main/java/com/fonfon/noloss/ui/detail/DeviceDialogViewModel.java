package com.fonfon.noloss.ui.detail;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableByte;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;
import android.widget.Toast;

import com.fonfon.noloss.lib.Device;

import io.realm.Realm;

public class DeviceDialogViewModel {

    private Context context;
    public Device device;

    public ObservableField<String> name = new ObservableField<>();
    public ObservableBoolean isAlarmed = new ObservableBoolean(false);
    public ObservableInt status = new ObservableInt(0);
    public ObservableByte batterylevel = new ObservableByte((byte) 0);

    public DeviceDialogViewModel(Context context, Device device) {
        this.context = context;
        this.device = device;
        name.set(device.getName());
        isAlarmed.set(device.isAlarmed());
        status.set(device.getStatus());
        batterylevel.set(device.getBatteryLevel());
    }

    public void onDismiss() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        device.setName(name.get());
        realm.commitTransaction();
    }

    public void alertOnOff(View v) {
        Toast.makeText(context, "onAlert", Toast.LENGTH_SHORT).show();
    }

    public void connectDisconnect(View v) {
        Toast.makeText(context, "onDisconnect", Toast.LENGTH_SHORT).show();
    }
}
