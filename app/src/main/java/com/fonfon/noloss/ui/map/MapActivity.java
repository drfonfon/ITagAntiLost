package com.fonfon.noloss.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.lib.StringBitmapConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmResults;

public final class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static void show(Activity activity) {
        activity.startActivity(new Intent(activity, MapActivity.class));
        activity.overridePendingTransition(R.anim.slide_left, R.anim.no_change);
    }

    boolean isFirstLocationChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
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

    public void showMarkers(final GoogleMap googleMap) {
        RealmResults<Device> devices = Realm.getDefaultInstance().where(Device.class).findAll();
        for (Device device : devices) {
            if (device.getLatitude() != 0 && device.getLongitude() != 0) {
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(device.getLatitude(), device.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                        .flat(true)
                        .title(device.getName())
                );
                String image = device.getImage();
                if (image != null) {
                    Bitmap bitmap = StringBitmapConverter.stringToBitMap(device.getImage());
                    if (bitmap != null) {
                        int dimen = getResources().getDimensionPixelSize(R.dimen.marker_size);
                        Bitmap bmp = Bitmap.createScaledBitmap(bitmap, dimen, dimen, false);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
                    }
                }
            }
        }
    }
}
