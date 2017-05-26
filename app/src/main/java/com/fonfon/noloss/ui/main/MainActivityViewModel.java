package com.fonfon.noloss.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fonfon.noloss.App;
import com.fonfon.noloss.R;
import com.fonfon.noloss.BleService;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.ui.BleViewModel;
import com.fonfon.noloss.ui.detail.DetailActivity;
import com.fonfon.noloss.ui.map.MapActivity;
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

public final class MainActivityViewModel extends BleViewModel implements
        DevicesAdapter.Listener, SwipeHelper.SwipeListener, SwipeRefreshLayout.OnRefreshListener {

    private final DataListener dataListener;
    private final DevicesAdapter adapter;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null) {
                String action = intent.getAction();
                String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
                if(action != null && address != null) {
                    switch (action) {
                        case BleService.DEVICE_CONNECTED:
                            adapter.deviceConnected(address);
                            break;
                        case BleService.DEVICE_DISCONNECTED:
                            adapter.deviceDisconnected(address);
                            break;
                    }
                }
            }
        }
    };

    MainActivityViewModel(AppCompatActivity activity, DataListener dataListener) {
        super(activity);
        this.dataListener = dataListener;
        this.activity = activity;
        adapter = new DevicesAdapter(this);
    }

    DevicesAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void resume() {
        super.resume();
        IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);
        intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
        activity.registerReceiver(receiver, intentFilter);
        App.getInstance().setVisibleAddress(App.ALL_ADDRESSES);
        onRefresh();
    }

    void pause() {
        activity.unregisterReceiver(receiver);
        activity.startService(new Intent(activity, BleService.class));
        App.getInstance().setVisibleAddress(null);
    }

    @Override
    public void onDeviceClick(String address) {
        DetailActivity.show(activity, address);
    }

    @Override
    public void onDeviceAlerted(String address, boolean isAlarmed) {
        BleService.alert(activity, address, isAlarmed);
    }

    public void searchClick() {
        NewDeviceActivity.show(activity);
    }

    public void mapCLick() {
        MapActivity.show(activity);
    }

    @Override
    public void onItemDelete(final int adapterPosition) {
        final String address = adapter.getDeviceAddressFrom(adapterPosition);
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Device device = realm.where(Device.class).equalTo(Device.ADDRESS, address).findFirst();
                if (device != null) {
                    device.deleteFromRealm();
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                BleService.disconnect(activity, address);
                adapter.deviceDeleted(adapterPosition);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                adapter.notifyItemChanged(adapterPosition);
                Toast.makeText(activity, R.string.delete_error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onRefresh() {
        dataListener.setRefresh(true);
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
                        dataListener.setRefresh(false);
                    }
                });
    }

    interface DataListener {
        void setRefresh(boolean isRefreshing);
    }
}
