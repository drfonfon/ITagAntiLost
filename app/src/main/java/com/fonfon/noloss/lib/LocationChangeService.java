package com.fonfon.noloss.lib;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.fonfon.geohash.GeoHash;
import com.fonfon.noloss.BleService;
import com.fonfon.noloss.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.realm.Realm;

public class LocationChangeService extends IntentService {

    public static final String LOCATION_CHANGED = "com.fonfon.noloss.LOCATION_CHANGED";
    public static final String LOCATION = "LOCATION";

    public LocationChangeService() {
        super(LocationChangeService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            if (address != null) {
                final GoogleApiClient googleApiClient =
                        new GoogleApiClient.Builder(getApplicationContext())
                                .addApi(LocationServices.API)
                                .build();

                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {
                        if (location != null) {
                            updateDeviceLocation(address, location);
                            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                            googleApiClient.disconnect();
                        }
                    }
                };

                googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        startLocationUpdate(googleApiClient, locationListener);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                });
                googleApiClient.connect();
            }
        }
    }

    private void startLocationUpdate(
            GoogleApiClient googleApiClient,
            LocationListener locationListener
    ) {
        boolean a = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED;
        boolean b = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED;
        if (a && b) {
            googleApiClient.disconnect();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                locationListener);
    }

    private void updateDeviceLocation(final String address, final Location location) {
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Device device = realm.where(Device.class).equalTo(Device.ADDRESS, address).findFirst();
                if (device != null) {

                    GeoHash geoHash = GeoHash.fromLocation(location, GeoHash.MAX_CHARACTER_PRECISION);
                    device.setGeoHash(geoHash.toString());

                    Bitmap bitmap = BitmapUtils.stringToBitMap(device.getImage());

                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                            .notify(device.doHash(), new NotificationCompat.Builder(getApplicationContext())
                                    .setLargeIcon(bitmap)
                                    .setSmallIcon(R.drawable.ic_find_key)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setContentText(device.getName() + " " + getString(R.string.location_updated))
                                    .setVibrate(new long[]{1000, 1000})
                                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                    .build()
                            );
                    sendBroadcast(
                            new Intent(LOCATION_CHANGED)
                                    .putExtra(BleService.DEVICE_ADDRESS, device.getAddress())
                                    .putExtra(LOCATION, location)
                    );
                }
            }
        });
    }
}
