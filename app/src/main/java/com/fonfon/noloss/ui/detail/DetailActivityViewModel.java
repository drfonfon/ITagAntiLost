package com.fonfon.noloss.ui.detail;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BitmapTransform;
import com.fonfon.noloss.lib.BleService;
import com.fonfon.noloss.lib.ClickBroadcastReceiver;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.SaveImageService;
import com.fonfon.noloss.lib.StringBitmapConverter;
import com.fonfon.noloss.ui.BleViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import io.realm.Realm;

public final class DetailActivityViewModel extends BleViewModel implements OnMapReadyCallback, Toolbar.OnMenuItemClickListener {

    private static final int GALLERY_REQUEST = 2;
    private static final IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);

    static {
        intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
        intentFilter.addAction(BleService.BATTERY_LEVEL_UPDATED);
        intentFilter.addAction(ClickBroadcastReceiver.LOCATION_CHANGED);
    }

    public Device device;

    public ObservableField<String> name = new ObservableField<>("");
    public ObservableInt batteryLevel = new ObservableInt(0);
    public ObservableBoolean isAlarmed = new ObservableBoolean(false);
    public ObservableBoolean isConnected = new ObservableBoolean(false);

    private Marker marker;
    private GoogleMap googleMap;
    private DataListener dataListener;
    private boolean isDeleted = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            byte battery = intent.getByteExtra(BleService.BATTERY_LEVEL, (byte) 0);
            Location location = intent.getParcelableExtra(ClickBroadcastReceiver.LOCATION);
            if (action != null && address != null && address.equals(device.getAddress())) {
                switch (action) {
                    case BleService.DEVICE_CONNECTED:
                        isConnected.set(true);
                        break;
                    case BleService.DEVICE_DISCONNECTED:
                        isConnected.set(false);
                        break;
                    case BleService.BATTERY_LEVEL_UPDATED:
                        batteryLevel.set(battery);
                        device.setBatteryLevel(battery);
                        break;
                    case ClickBroadcastReceiver.LOCATION_CHANGED:
                        if(location != null) {
                            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                            if(marker != null) {
                                marker.setPosition(position);
                            } else {
                                createMarker(position);
                            }
                        }
                        break;
                }
            }
        }
    };

    DetailActivityViewModel(AppCompatActivity activity, String address, DataListener dataListener) {
        super(activity);
        this.activity = activity;
        this.device = Realm.getDefaultInstance().where(Device.class).equalTo(Device.ADDRESS, address).findFirst();
        this.dataListener = dataListener;

        name.set(device.getName());
        batteryLevel.set(device.getBatteryLevel());
        isAlarmed.set(device.isAlarmed());
        isConnected.set(device.isConnected());
        dataListener.onImage(StringBitmapConverter.stringToBitMap(device.getImage()));
    }

    @Override
    public void resume() {
        activity.registerReceiver(receiver, intentFilter);
        BleService.connect(activity, device.getAddress());
        BleService.checkBattery(activity);
    }

    void pause() {
        BleService.stopService(activity);
        activity.unregisterReceiver(receiver);
        if (!isDeleted && !device.getName().equals(name.get())) {
            Realm.getDefaultInstance().beginTransaction();
            device.setName(name.get());
            Realm.getDefaultInstance().commitTransaction();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST:
                    final Uri selectedImage = data.getData();
                    SaveImageService.start(activity, selectedImage, device.getAddress());
                    try {
                        final Bitmap bitmap = BitmapTransform.transform(
                                MediaStore.Images.Media.getBitmap(activity.getContentResolver(), selectedImage)
                        );
                        dataListener.onImage(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(activity, R.string.load_image_error, Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
    }

    public void imageClick(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activity.startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    public void alertClick(View view) {
        BleService.alert(activity, device.getAddress(), device.isAlarmed());
        device.setAlarmed(!device.isAlarmed());
        isAlarmed.set(device.isAlarmed());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (device.getLatitude() != 0 && device.getLongitude() != 0) {
            LatLng latLng = new LatLng(device.getLatitude(), device.getLongitude());
            createMarker(latLng);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            final String address = device.getAddress();
            isDeleted = true;
            BleService.disconnect(activity, address);
            Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Device device = realm.where(Device.class).equalTo(Device.ADDRESS, address).findFirst();
                    if (device != null) {
                        device.deleteFromRealm();
                    }
                }
            });
            activity.finish();
        }
        return false;
    }

    private void createMarker(LatLng position) {
        marker = googleMap.addMarker(new MarkerOptions()
                .title(device.getName())
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, googleMap.getMaxZoomLevel() - 5));
    }

    interface DataListener {
        void onImage(Bitmap image);
    }
}
