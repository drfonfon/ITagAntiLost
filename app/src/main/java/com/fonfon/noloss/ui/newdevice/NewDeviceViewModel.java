package com.fonfon.noloss.ui.newdevice;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BitmapUtils;
import com.fonfon.noloss.lib.BleService;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.ui.BleViewModel;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

final class NewDeviceViewModel extends BleViewModel implements NewDevicesAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

    private static final long SCAN_PERIOD = 8000;

    private final DataListener dataListener;
    private final NewDevicesAdapter adapter;

    private final Handler handler;
    private final Runnable stopScan;
    private final List<String> currentAddresses = new ArrayList<>();

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getScanRecord() != null) {
                List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
                if (uuids != null) {
                    for (ParcelUuid uuid : uuids) {
                        if (uuid.getUuid().equals(BleService.FIND_ME_SERVICE)) {
                            if (!currentAddresses.contains(result.getDevice().getAddress()))
                                adapter.add(result.getDevice().getAddress(), result.getScanRecord().getDeviceName());
                            break;
                        }
                    }
                }
            }
        }
    };

    private String defaultImage;

    NewDeviceViewModel(AppCompatActivity activity, DataListener dataListener) {
        super(activity);
        this.activity = activity;
        this.dataListener = dataListener;

        adapter = new NewDevicesAdapter(this);
        handler = new Handler();
        stopScan = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(stopScan);
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                NewDeviceViewModel.this.dataListener.setRefresh(false);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                defaultImage = BitmapUtils.bitmapToString(
                        BitmapFactory.decodeResource(
                                NewDeviceViewModel.this.activity.getResources(),
                                R.mipmap.ic_launcher
                        )
                );
            }
        }).run();
    }

    NewDevicesAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void resume() {
        super.resume();
        onRefresh();
    }

    void pause() {
        handler.removeCallbacks(stopScan);
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
    }

    @Override
    public void onRefresh() {
        Realm.getDefaultInstance()
                .where(Device.class)
                .findAllAsync()
                .addChangeListener(new RealmChangeListener<RealmResults<Device>>() {
                    @Override
                    public void onChange(RealmResults<Device> devices) {
                        currentAddresses.clear();
                        for (Device device : devices) {
                            currentAddresses.add(device.getAddress());
                        }
                        devices.removeAllChangeListeners();
                        dataListener.setRefresh(true);
                        adapter.clear();
                        handler.postDelayed(stopScan, SCAN_PERIOD);
                        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
                    }
                });
    }

    @Override
    public void onDevice(final String address, final String name) {
        if (defaultImage != null) {
            Realm.getDefaultInstance()
                    .executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(new Device(address, name, defaultImage));
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            activity.finish();
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            Toast.makeText(activity, R.string.add_device_error, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(activity, R.string.add_device_error, Toast.LENGTH_SHORT).show();
        }
    }

    interface DataListener {
        void setRefresh(boolean isRefreshing);
    }
}
