package com.fonfon.noloss.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.fonfon.noloss.R;
import com.fonfon.noloss.lib.Device;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

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
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        Realm.getDefaultInstance().where(Device.class).findAllAsync().addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Device>>() {
            @Override
            public void onChange(RealmResults<Device> devices, OrderedCollectionChangeSet changeSet) {
                for (Device device : devices) {
                    if (device.getLatitude() != 0 && device.getLongitude() != 0) {
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(device.getLatitude(), device.getLongitude()))
                                .title(device.getName())
                        );
                    }
                }
            }
        });
    }
}
