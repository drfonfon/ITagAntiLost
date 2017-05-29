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

import com.fonfon.geohash.GeoHash;
import com.fonfon.noloss.BleService;
import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.BitmapUtils;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.LocationChangeService;
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

final class MapActivityViewModel implements OnMapReadyCallback {

    private final AppCompatActivity activity;
    private boolean isFirstLocationChange = false;
    private final int markerSize;
    private final ArrayList<Marker> markers = new ArrayList<>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            Location location = intent.getParcelableExtra(LocationChangeService.LOCATION);
            if(action != null && action.equals(LocationChangeService.LOCATION_CHANGED) && address != null && location != null) {
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
        this.activity = activity;
        markerSize = activity.getResources().getDimensionPixelSize(R.dimen.marker_size);
    }

    void resume() {
        activity.registerReceiver(receiver, new IntentFilter(LocationChangeService.LOCATION_CHANGED));
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
            GeoHash geoHash = GeoHash.fromString(device.getGeoHash());
            Location center = geoHash.getCenter();
            createMarker(googleMap, new LatLng(center.getLatitude(), center.getLongitude()), device);
        }
    }

    private void createMarker(GoogleMap googleMap, LatLng point, Device device) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(point)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .flat(true)
                .title(device.getName())
                .visible(!Device.ZERO_GEOHASH.equals(device.getGeoHash()))
                .snippet(device.getAddress())
        );
        markers.add(marker);
        Bitmap bitmap = BitmapUtils.stringToBitMap(device.getImage());
        if (bitmap != null) {
            Bitmap bmp = Bitmap.createScaledBitmap(bitmap, markerSize, markerSize, false);
            bitmap.recycle();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
        }
    }
}
