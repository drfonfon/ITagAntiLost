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

public final class MainActivityViewModel extends BleViewModel implements DevicesAdapter.Listener, SwipeHelper.SwipeListener {

    private static final IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);

    private DevicesAdapter adapter;

    static {
        intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            switch (action) {
                case BleService.DEVICE_CONNECTED:
                    adapter.deviceConnected(address);
                    break;
                case BleService.DEVICE_DISCONNECTED:
                    adapter.deviceDisconnected(address);
                    break;
            }
        }
    };

    MainActivityViewModel(AppCompatActivity activity) {
        super(activity);
        this.activity = activity;
        adapter = new DevicesAdapter(this);
    }

    DevicesAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void resume() {
        super.resume();
        activity.registerReceiver(receiver, intentFilter);
        Realm.getDefaultInstance()
                .where(Device.class)
                .findAllAsync()
                .addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Device>>() {
                    @Override
                    public void onChange(RealmResults<Device> devices, OrderedCollectionChangeSet changeSet) {
                        adapter.addDevices(devices);
                        for (Device device : devices) {
                            BleService.connect(activity, device.getAddress());
                        }
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
    public void onItemDelete(int adapterPosition) {
        final String tg = adapter.deviceDeleted(adapterPosition);
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
    public void onItemAlert(int adapterPosition) {
        final String address = adapter.deviceAlerted(adapterPosition);
        BleService.alert(activity, address, adapter.getDeviceAlertStatus(adapterPosition));
    }

    @Override
    public boolean onMove(int adapterPosition) {
        return adapter.getDeviceAlertStatus(adapterPosition);
    }

}
