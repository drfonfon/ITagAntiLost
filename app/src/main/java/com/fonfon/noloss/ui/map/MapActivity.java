package com.fonfon.noloss.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.CircleTransform;
import com.fonfon.noloss.lib.Device;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmResults;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static void show(Activity activity) {
        activity.startActivity(new Intent(activity, MapActivity.class));
        activity.overridePendingTransition(R.anim.slide_left, R.anim.no_change);
    }

    private RealmResults<Device> devices;
    private CircleTransform circleTransform = new CircleTransform();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        showMarkers(googleMap);
    }

    public void showMarkers(final GoogleMap googleMap) {
        devices = Realm.getDefaultInstance().where(Device.class).findAll();
        for (Device device : devices) {
            if (device.getLatitude() != 0 && device.getLongitude() != 0) {

                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(device.getLatitude(), device.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(device.getBitmap(), getResources().getDimensionPixelSize(R.dimen.marker_size),  getResources().getDimensionPixelSize(R.dimen.marker_size), false)))
                        .flat(true)
                        .title(device.getName())
                );

              //  markers.add(new PicassoMarker(marker));

//                Bitmap bitmap =
//
//                Picasso.with(this)
//                        .load(device.getImageUri())
//                        .error(R.mipmap.ic_launcher)
//                        .resizeDimen(R.dimen.fab_margin, R.dimen.fab_margin)
//                        .onlyScaleDown()
//                        .transform(circleTransform)
//                        .into(markers.get(markers.size() - 1));
            }
        }
    }
}
