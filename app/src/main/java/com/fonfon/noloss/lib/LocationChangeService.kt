package com.fonfon.noloss.lib

import android.Manifest
import android.app.IntentService
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat

import com.fonfon.geohash.GeoHash
import com.fonfon.noloss.BleService
import com.fonfon.noloss.R
import com.fonfon.noloss.db.DbHelper
import com.fonfon.noloss.db.DeviceDB
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import nl.qbusict.cupboard.CupboardFactory.cupboard

class LocationChangeService : IntentService(LocationChangeService::class.java.name) {

  override fun onHandleIntent(intent: Intent?) {
    if (intent != null) {
      val address = intent.getStringExtra(BleService.DEVICE_ADDRESS)
      if (address != null) {
        val googleApiClient = GoogleApiClient.Builder(applicationContext)
            .addApi(LocationServices.API)
            .build()

        val locationListener = object : LocationListener {
          override fun onLocationChanged(location: Location?) {
            if (location != null) {
              updateDeviceLocation(address, location)
              LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
              googleApiClient.disconnect()
            }
          }
        }

        googleApiClient.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
          override fun onConnected(bundle: Bundle?) {
            startLocationUpdate(googleApiClient, locationListener)
          }

          override fun onConnectionSuspended(i: Int) {

          }
        })
        googleApiClient.connect()
      }
    }
  }

  private fun startLocationUpdate(
      googleApiClient: GoogleApiClient,
      locationListener: LocationListener
  ) {
    val a = ActivityCompat.checkSelfPermission(applicationContext,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
    val b = ActivityCompat.checkSelfPermission(applicationContext,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
    if (a && b) {
      googleApiClient.disconnect()
      return
    }
    LocationServices.FusedLocationApi.requestLocationUpdates(
        googleApiClient,
        LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
        locationListener)
  }

  private fun updateDeviceLocation(address: String?, location: Location?) {
    val database = DbHelper.getConnection(applicationContext)
    val deviceDB = cupboard().withDatabase(database)
        .query(DeviceDB::class.java)
        .withSelection("address = ?", address).get()
    deviceDB.geoHash = GeoHash.fromLocation(location, GeoHash.MAX_CHARACTER_PRECISION).toString()
    cupboard().withDatabase(database).put(deviceDB)
    NotifyManager.showNotification(deviceDB, applicationContext,
        deviceDB.name + " " + getString(R.string.location_updated))
    sendBroadcast(
        Intent(LOCATION_CHANGED)
            .putExtra(BleService.DEVICE_ADDRESS, deviceDB.address)
            .putExtra(LOCATION, location)
    )
  }

  companion object {

    const val LOCATION_CHANGED = "com.fonfon.noloss.LOCATION_CHANGED"
    val LOCATION = "LOCATION"
  }
}
