package com.fonfon.noloss.ui.newdevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fonfon.noloss.BleService;
import com.fonfon.noloss.R;
import com.fonfon.noloss.db.DbHelper;
import com.fonfon.noloss.db.DeviceDB;
import com.fonfon.noloss.ui.LocationActivity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import java.util.ArrayList;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public final class NewDeviceActivity extends LocationActivity {

    public final static int REQUEST_ENABLE_BT = 451;

    RecyclerView recycler;
    SwipeRefreshLayout refresh;
    Toolbar toolbar;
    TextView empty;

    private NewDevicesAdapter adapter;

    private BluetoothAdapter bluetoothAdapter;

    private Location currentLocation;
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
                                renderDataState(result.getDevice().getAddress(), result.getScanRecord().getDeviceName());
                            break;
                        }
                    }
                }
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_device);

        recycler = findViewById(R.id.recycler);
        refresh = findViewById(R.id.refresh);
        toolbar = findViewById(R.id.toolbar);
        empty = findViewById(R.id.empty_text);

        adapter = new NewDevicesAdapter((address, name) -> {
            cupboard().withDatabase(DbHelper.getConnection(this)).put(new DeviceDB(address, name.trim(), currentLocation));
            finish();
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        refresh.setOnRefreshListener(this::refresh);

        refresh();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    @Override
    protected void onPause() {
        pause();
        super.onPause();
    }

    private void renderLoadingState(boolean isLoading) {
        if (isLoading) {
            adapter.clear();
        }
        refresh.setRefreshing(isLoading);
        empty.setVisibility(isLoading || recycler.getAdapter().getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    private void renderDataState(String address, String name) {
        adapter.add(address, name);
    }

    private void resume() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    NewDeviceActivity.REQUEST_ENABLE_BT
            );
        } else {
            refresh();
        }
    }

    private void pause() {
        if (bluetoothAdapter != null) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null && scanCallback != null) {
                scanner.stopScan(scanCallback);
            }
        }
    }

    private void refresh() {
        List<DeviceDB> devices = cupboard()
                .withDatabase(DbHelper.getConnection(this))
                .query(DeviceDB.class)
                .list();
        List<String> addresses = new ArrayList<>();
        for (DeviceDB device : devices) {
            addresses.add(device.getAddress());
        }

        currentAddresses.addAll(addresses);
        renderLoadingState(true);
        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);

        new Handler().postDelayed(() -> {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            renderLoadingState(false);
        }, 8000);
    }
}
