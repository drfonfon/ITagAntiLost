package com.fonfon.noloss.ui.map;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BleService;
import com.fonfon.noloss.lib.ClickBroadcastReceiver;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.StringBitmapConverter;
import com.fonfon.noloss.ui.BleViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

class MapActivityViewModel extends BleViewModel implements OnMapReadyCallback {

    private boolean isFirstLocationChange = false;
    private int markerSize;
    private ArrayList<Marker> markers = new ArrayList<>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            Location location = intent.getParcelableExtra(ClickBroadcastReceiver.LOCATION);
            if(action != null && action.equals(ClickBroadcastReceiver.LOCATION_CHANGED) && address != null && location != null) {
                for (Marker marker: markers) {
                    if(marker.getSnippet().equals(address)) {
                        marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                        marker.setVisible(marker.getPosition().latitude != 0 && marker.getPosition().longitude  != 0);
                        break;
                    }
                }
            }
        }
    };

    MapActivityViewModel(AppCompatActivity activity) {
        super(activity);
        markerSize = activity.getResources().getDimensionPixelSize(R.dimen.marker_size);
    }

    @Override
    public void resume() {
        super.resume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ClickBroadcastReceiver.LOCATION_CHANGED);
        activity.registerReceiver(receiver, intentFilter);
    }

    void pause() {
        activity.unregisterReceiver(receiver);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (location != null && !isFirstLocationChange) {
                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(),
                                            location.getLongitude()),
                                    googleMap.getMaxZoomLevel() - 5
                            )
                    );
                    isFirstLocationChange = true;
                }
            }
        });
        showMarkers(googleMap);
    }

    private void showMarkers(final GoogleMap googleMap) {
        RealmResults<Device> devices = Realm.getDefaultInstance().where(Device.class).findAll();
        for (Device device : devices) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(device.getLatitude(), device.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                    .flat(true)
                    .visible(device.getLatitude() != 0 && device.getLongitude() != 0)
                    .title(device.getName())
                    .snippet(device.getAddress())
            );
            markers.add(marker);
            String image = device.getImage();
            if (image != null) {
                Bitmap bitmap = StringBitmapConverter.stringToBitMap(device.getImage());
                if (bitmap != null) {
                    Bitmap bmp = Bitmap.createScaledBitmap(bitmap, markerSize, markerSize, false);
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
                }
            }
        }
    }
}
