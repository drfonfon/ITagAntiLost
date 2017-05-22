package com.fonfon.noloss.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fonfon.noloss.lib.BleService;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.ui.BleViewModel;
import com.fonfon.noloss.ui.SwipeHelper;
import com.fonfon.noloss.ui.detail.DetailActivity;
import com.fonfon.noloss.ui.map.MapActivity;
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

public final class MainActivityViewModel extends BleViewModel implements DevicesAdapter.Listener, SwipeHelper.DeleteListener {

    private DataListener dataListener;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            switch (action) {
                case BleService.DEVICE_CONNECTED:
                    dataListener.deviceConnected(address);
                    break;
                case BleService.DEVICE_DISCONNECTED:
                    dataListener.deviceDisconnected(address);
                    break;
                case BleService.CONNECTED_DEVICES:
                    String[] addresses = intent.getStringArrayExtra(BleService.DEVICES_ADDRESSES);
                    if (addresses != null) {
                        for (String addr : addresses) {
                            dataListener.deviceConnected(addr);
                        }
                    }
                    break;
            }
        }
    };

    MainActivityViewModel(AppCompatActivity activity, DataListener dataListener) {
        super(activity);
        this.activity = activity;
        this.dataListener = dataListener;
    }

    @Override
    public void resume() {
        super.resume();
        IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);
        intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
        intentFilter.addAction(BleService.CONNECTED_DEVICES);
        activity.registerReceiver(receiver, intentFilter);
        BleService.checkConnectedDevices(activity);
        Realm.getDefaultInstance()
                .where(Device.class)
                .findAllAsync()
                .addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Device>>() {
                    @Override
                    public void onChange(RealmResults<Device> devices, OrderedCollectionChangeSet changeSet) {
                        dataListener.onDevices(devices);
                        for (Device device : devices) {
                            BleService.connect(activity, device.getAddress());
                        }
                        devices.removeAllChangeListeners();
                    }
                });
    }

    void pause() {
        BleService.stopService(activity);
        activity.unregisterReceiver(receiver);
    }

    @Override
    public void onDeviceClick(final Device device) {
        DetailActivity.show(activity, device.getAddress());
    }

    public void search(View view) {
        NewDeviceActivity.show(activity);
    }

    public void toMap(View view) {
        MapActivity.show(activity);
    }

    @Override
    public void onItemDelete(final Object tag, int adapterPosition) {
        final String tg = (String) tag;
        dataListener.deviceDeleted(adapterPosition);
        BleService.disconnect(activity, tg);
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Device device = realm.where(Device.class).equalTo(Device.ADDRESS, tg).findFirst();
                if (device != null) {
                    device.deleteFromRealm();
                }
            }
        });
    }

    @Override
    public void onItemAlert(Object tag, int adapterPosition) {
        final String tg = (String) tag;
        dataListener.deviceAlert(adapterPosition);
        BleService.alert(activity, tg, dataListener.getDeviceAlertStatus(adapterPosition));
    }

    @Override
    public boolean onMove(int adapterPosition) {
        return dataListener.getDeviceAlertStatus(adapterPosition);
    }

    interface DataListener {
        void onDevices(RealmResults<Device> devices);

        void deviceConnected(String address);

        void deviceDisconnected(String address);

        void deviceDeleted(int index);

        void deviceAlert(int index);

        boolean getDeviceAlertStatus(int index);
    }
}
