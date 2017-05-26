package com.fonfon.noloss.ui.detail;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.fonfon.geohash.GeoHash;
import com.fonfon.noloss.App;
import com.fonfon.noloss.R;
import com.fonfon.noloss.BleService;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.LocationChangeService;
import com.fonfon.noloss.lib.SaveImageService;
import com.fonfon.noloss.ui.BleViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;

public final class DetailActivityViewModel extends BleViewModel implements OnMapReadyCallback, Toolbar.OnMenuItemClickListener {

    private static final int GALLERY_REQUEST = 2;

    public Device device;
    public ObservableField<String> name = new ObservableField<>("");
    public ObservableBoolean isAlarmed = new ObservableBoolean(false);
    public ObservableBoolean isConnected = new ObservableBoolean(false);
    public ObservableField<String> stringImage = new ObservableField<>("");

    private Marker marker;
    private GoogleMap googleMap;
    private boolean isDeleted = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            if (action != null && address != null && address.equals(device.getAddress())) {
                switch (action) {
                    case BleService.DEVICE_CONNECTED:
                        isConnected.set(true);
                        break;
                    case BleService.DEVICE_DISCONNECTED:
                        isConnected.set(false);
                        break;
                    case LocationChangeService.LOCATION_CHANGED:
                        Location location = intent.getParcelableExtra(LocationChangeService.LOCATION);
                        if (location != null) {
                            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                            if (marker != null) {
                                marker.setPosition(position);
                            } else {
                                createMarker(position);
                            }
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, googleMap.getMaxZoomLevel() - 5));
                        }
                        break;
                }
            }
        }
    };

    DetailActivityViewModel(AppCompatActivity activity, String address) {
        super(activity);
        this.activity = activity;
        this.device = Realm.getDefaultInstance().where(Device.class).equalTo(Device.ADDRESS, address).findFirst();

        name.set(device.getName());
        isConnected.set(false);
        stringImage.set(device.getImage());

        device.addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel realmModel) {
                name.set(device.getName());
                stringImage.set(device.getImage());
            }
        });
    }

    @Override
    public void resume() {
        App.getInstance().setVisibleAddress(device.getAddress());
        IntentFilter intentFilter = new IntentFilter(BleService.DEVICE_CONNECTED);
        intentFilter.addAction(BleService.DEVICE_DISCONNECTED);
        intentFilter.addAction(LocationChangeService.LOCATION_CHANGED);
        activity.registerReceiver(receiver, intentFilter);
        BleService.connect(activity, device.getAddress());
    }

    void pause() {
        App.getInstance().setVisibleAddress(null);
        activity.startService(new Intent(activity, BleService.class));
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
                    break;
            }
    }

    public void imageClick() {
        activity.startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), GALLERY_REQUEST);
    }

    public void alertClick() {
        isAlarmed.set(!isAlarmed.get());
        BleService.alert(activity, device.getAddress(), isAlarmed.get());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        GeoHash geoHash = GeoHash.fromString(device.getGeoHash());
        Location location = geoHash.getCenter();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        createMarker(latLng);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 5));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            final String address = device.getAddress();
            device.removeAllChangeListeners();
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
                    isDeleted = true;
                    activity.finish();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    Toast.makeText(activity, R.string.delete_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return false;
    }

    private void createMarker(LatLng position) {
        marker = googleMap.addMarker(new MarkerOptions()
                .title(device.getName())
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );
    }

}
