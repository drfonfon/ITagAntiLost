package com.fonfon.noloss.lib;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.fonfon.noloss.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.realm.Realm;

public final class ClickBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(
            final Context context,
            Intent intent
    ) {
        if (intent != null) {
            String address = intent.getStringExtra(BleService.DEVICE_ADDRESS);
            if (address != null) {
                final Device device = Realm.getDefaultInstance()
                        .where(Device.class)
                        .equalTo(Device.ADDRESS, address)
                        .findFirst();
                if (device != null) {
                    final GoogleApiClient googleApiClient = getGoogleApiClient(context);

                    final LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(final Location location) {
                            updateDeviceLocation(context, device, location);
                            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                            googleApiClient.disconnect();
                        }
                    };

                    googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            startLocationUpdate(context, googleApiClient, locationListener);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    });
                    googleApiClient.connect();
                }
            }
        }
    }

    private void startLocationUpdate(
            Context context,
            GoogleApiClient googleApiClient,
            LocationListener locationListener
    ) {
        boolean a = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED;
        boolean b = ActivityCompat.checkSelfPermission(context,
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

    private void updateDeviceLocation(
            Context context,
            final Device device,
            final Location location
    ) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                device.setLocation(location);
            }
        });
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(device.doHash(),
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_find_key)
                                .setContentTitle(context.getString(R.string.app_name))
                                .setContentText(device.getName() + context.getString(R.string.location_updated))
                                .build()
                );
    }

    private synchronized GoogleApiClient getGoogleApiClient(
            Context context
    ) {
        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
    }

    
}
