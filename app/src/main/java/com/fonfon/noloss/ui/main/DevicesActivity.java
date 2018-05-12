package com.fonfon.noloss.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fonfon.geohash.GeoHash;
import com.fonfon.noloss.BleService;
import com.fonfon.noloss.R;
import com.fonfon.noloss.db.DbHelper;
import com.fonfon.noloss.db.DeviceDB;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.LocationChangeService;
import com.fonfon.noloss.ui.LocationActivity;
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public final class DevicesActivity extends LocationActivity implements DevicesAdapter.DeviceAdapterListener {

    RecyclerView recyclerView;
    TextView textTotal;
    ImageButton fabNewDevice;
    ImageButton buttonRefresh;
    LinearLayout bottomSheet;
    ImageView imageSwipeUp;
    int markerSize;

    private DevicesAdapter adapter;

    private GoogleMap googleMap;
    private boolean isCameraUpdated = false;

    private ArrayList<Device> currentDevices = new ArrayList<>();

    private boolean receiverRegistered = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
                if (action != null && address != null) {
                    switch (action) {
                        case BleService.DEVICE_CONNECTED:
                            deviceConnect(address, true);
                            break;
                        case BleService.DEVICE_DISCONNECTED:
                            deviceConnect(address, false);
                            break;
                        case LocationChangeService.LOCATION_CHANGED:
                            deviceLocationChange(address, intent.getParcelableExtra(LocationChangeService.LOCATION));
                            break;
                    }
                    render();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        recyclerView = findViewById(R.id.recycler);
        textTotal = findViewById(R.id.text_total);
        fabNewDevice = findViewById(R.id.button_new_device);
        buttonRefresh = findViewById(R.id.button_refresh);
        bottomSheet = findViewById(R.id.bottom_sheet);
        imageSwipeUp = findViewById(R.id.image_swipe_up);

        markerSize = getResources().getDimensionPixelSize(R.dimen.marker_size);

        adapter = new DevicesAdapter(this, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(googleMap -> {
                    this.googleMap = googleMap;
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                    if (currentDevices != null) {
                        showMarkers();
                    }
                });

        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        View.OnClickListener expandClick = v -> behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        textTotal.setOnClickListener(expandClick);
        imageSwipeUp.setOnClickListener(expandClick);

        fabNewDevice.setOnClickListener(v -> startActivity(new Intent(this, NewDeviceActivity.class)));

        buttonRefresh.setOnClickListener(v -> loadData());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (googleMap != null) {
                    Location location = locationResult.getLastLocation();
                    if (!googleMap.isMyLocationEnabled()) {
                        if (ActivityCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        == PackageManager.PERMISSION_GRANTED) {
                            googleMap.setMyLocationEnabled(true);
                        }
                    }
                    if (!isCameraUpdated) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), googleMap.getMaxZoomLevel() - 4));
                        isCameraUpdated = true;
                    }
                }
            }
        };
    }

    private void updateDevice(Device device) {
        for (Device curDevice : currentDevices) {
            if (curDevice.get_id().equals(device.get_id())) {
                curDevice.setName(device.getName());
                break;
            }
        }
        cupboard().withDatabase(DbHelper.getConnection(this)).put(new DeviceDB(device));
        render();
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

    public void render() {
        textTotal.setText(String.format(Locale.getDefault(), getString(R.string.total_devices), currentDevices.size()));
        adapter.setDevices(currentDevices);

        if (googleMap != null) {
            showMarkers();
        }
    }

    @Override
    public void onRename(@NonNull Device device) {
        @SuppressLint("InflateParams")
        EditText edit = (EditText) LayoutInflater.from(this).inflate(R.layout.layout_edit_name, null);
        new AlertDialog.Builder(this)
                .setTitle(R.string.change_name)
                .setView(edit)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (edit.getText().toString().trim().length() > 0) {
                        device.setName(edit.getText().toString().trim());
                        updateDevice(device);
                    }
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDelete(@NonNull Device device) {
        BleService.disconnect(this, device.getAddress());
        currentDevices.remove(device);
        cupboard().withDatabase(DbHelper.getConnection(this)).delete(DeviceDB.class, device.get_id());
        render();
    }

    @Override
    public void onAlert(@NonNull Device device) {
        int index = currentDevices.indexOf(device);
        if (index > -1) {
            currentDevices.get(index).setAlerted(!currentDevices.get(index).isAlerted());
            BleService.alert(this, currentDevices.get(index).getAddress(), currentDevices.get(index).isAlerted());
            render();
        }
    }

    private void showMarkers() {
        googleMap.clear();
        for (Device device: currentDevices) {
            Location center = GeoHash.fromString(device.getGeoHash()).getCenter();
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(center.getLatitude(), center.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                    .flat(true)
                    .title(device.getName())
                    .snippet(device.getAddress())
            );
        }
    }

    public void resume() {
        IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);
        intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
        intentFilter.addAction(LocationChangeService.LOCATION_CHANGED);
        registerReceiver(receiver, intentFilter);
        receiverRegistered = true;
        loadData();
    }

    public void pause() {
        if (receiverRegistered) {
            unregisterReceiver(receiver);
            receiverRegistered = false;
        }
        startService(new Intent(this, BleService.class));
    }

    private void loadData() {
        List<DeviceDB> list = cupboard().withDatabase(DbHelper.getConnection(this))
                .query(DeviceDB.class)
                .list();
        List<Device> devices = new ArrayList<>();
        for (DeviceDB deviceDB: list) {
            BleService.connect(this, deviceDB.getAddress());
            devices.add(new Device(deviceDB));
        }

        currentDevices.clear();
        currentDevices.addAll(devices);

        render();
    }

    private void deviceConnect(String address, boolean connected) {
        for (int i = 0; i < currentDevices.size(); i++) {
            if (currentDevices.get(i).getAddress().equals(address)) {
                currentDevices.get(i).setConnected(connected);
                break;
            }
        }
    }

    private void deviceLocationChange(String address, Location location) {
        for (int i = 0; i < currentDevices.size(); i++) {
            if (currentDevices.get(i).getAddress().equals(address)) {
                currentDevices.get(i).setLocation(location);
                break;
            }
        }
    }
}
