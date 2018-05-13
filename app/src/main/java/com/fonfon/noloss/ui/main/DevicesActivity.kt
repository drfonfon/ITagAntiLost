package com.fonfon.noloss.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.fonfon.geohash.GeoHash
import com.fonfon.noloss.BleService
import com.fonfon.noloss.R
import com.fonfon.noloss.db.DbHelper
import com.fonfon.noloss.db.DeviceDB
import com.fonfon.noloss.lib.Device
import com.fonfon.noloss.lib.LocationChangeService
import com.fonfon.noloss.ui.LocationActivity
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_devices.*

import java.util.ArrayList
import java.util.Locale

import nl.qbusict.cupboard.CupboardFactory.cupboard

class DevicesActivity : LocationActivity(), DevicesAdapter.DeviceAdapterListener {

  private var markerSize: Int = 0

  private var adapter: DevicesAdapter? = null

  private var googleMap: GoogleMap? = null
  private var isCameraUpdated = false

  private val currentDevices = ArrayList<Device>()

  private var receiverRegistered = false
  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
      if (intent != null) {
        val action = intent.action
        val address = intent.getStringExtra(BleService.DEVICE_ADDRESS)
        if (action != null && address != null) {
          when (action) {
            BleService.DEVICE_CONNECTED -> deviceConnect(address, true)
            BleService.DEVICE_DISCONNECTED -> deviceConnect(address, false)
            LocationChangeService.LOCATION_CHANGED -> deviceLocationChange(address, intent.getParcelableExtra(LocationChangeService.LOCATION))
          }
          render()
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_devices)

    markerSize = resources.getDimensionPixelSize(R.dimen.marker_size)

    adapter = DevicesAdapter(this, this)

    recycler.layoutManager = LinearLayoutManager(this)
    recycler.adapter = adapter
    (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        .getMapAsync { googleMap ->
          this.googleMap = googleMap
          if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
          }
          showMarkers()
        }

    val behavior = BottomSheetBehavior.from(bottom_sheet)
    text_total.setOnClickListener { behavior.state = BottomSheetBehavior.STATE_EXPANDED }

    button_new_device.setOnClickListener { startActivity(Intent(this, NewDeviceActivity::class.java)) }
    button_refresh.setOnClickListener { loadData() }

    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult?) {
        super.onLocationResult(locationResult)

        if (googleMap != null) {
          val location = locationResult!!.lastLocation
          if (!googleMap!!.isMyLocationEnabled) {
            if (ActivityCompat.checkSelfPermission(this@DevicesActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@DevicesActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
              googleMap!!.isMyLocationEnabled = true
            }
          }
          if (!isCameraUpdated) {
            googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), googleMap!!.maxZoomLevel - 4))
            isCameraUpdated = true
          }
        }
      }
    }
  }

  private fun updateDevice(device: Device) {
    for (curDevice in currentDevices) {
      if (curDevice._id == device._id) {
        curDevice.name = device.name
        break
      }
    }
    cupboard().withDatabase(DbHelper.getConnection(this)).put(DeviceDB(device))
    render()
  }

  override fun onResume() {
    super.onResume()
    registerReceiver(receiver, IntentFilter(BleService.DEVICE_CONNECTED).apply {
      addAction(BleService.DEVICE_DISCONNECTED)
      addAction(LocationChangeService.LOCATION_CHANGED)
    })
    receiverRegistered = true
    loadData()
  }

  override fun onPause() {
    if (receiverRegistered) {
      unregisterReceiver(receiver)
      receiverRegistered = false
    }
    startService(Intent(this, BleService::class.java))
    super.onPause()
  }

  fun render() {
    text_total.text = String.format(Locale.getDefault(), getString(R.string.total_devices), currentDevices.size)
    adapter!!.setDevices(currentDevices)

    if (googleMap != null) {
      showMarkers()
    }
  }

  override fun onRename(device: Device) {
    @SuppressLint("InflateParams")
    val edit = LayoutInflater.from(this).inflate(R.layout.layout_edit_name, null) as EditText
    AlertDialog.Builder(this)
        .setTitle(R.string.change_name)
        .setView(edit)
        .setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.dismiss() }
        .setPositiveButton(android.R.string.ok) { dialog, which ->
          if (edit.text.toString().trim { it <= ' ' }.isNotEmpty()) {
            device.name = edit.text.toString().trim { it <= ' ' }
            updateDevice(device)
          }
          dialog.dismiss()
        }
        .show()
  }

  override fun onDelete(device: Device) {
    BleService.disconnect(this, device.address)
    currentDevices.remove(device)
    cupboard().withDatabase(DbHelper.getConnection(this)).delete(DeviceDB::class.java, device._id!!)
    render()
  }

  override fun onAlert(device: Device) {
    val index = currentDevices.indexOf(device)
    if (index > -1) {
      currentDevices[index].isAlerted = !currentDevices[index].isAlerted
      BleService.alert(this, currentDevices[index].address, currentDevices[index].isAlerted)
      render()
    }
  }

  private fun showMarkers() {
    googleMap!!.clear()
    for (device in currentDevices) {
      val center = GeoHash.fromString(device.geoHash).getCenter()
      googleMap!!.addMarker(MarkerOptions()
          .position(LatLng(center.getLatitude(), center.getLongitude()))
          .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
          .flat(true)
          .title(device.name)
          .snippet(device.address)
      )
    }
  }

  private fun loadData() {
    val list = cupboard().withDatabase(DbHelper.getConnection(this))
        .query(DeviceDB::class.java)
        .list()
    val devices = ArrayList<Device>()
    for (deviceDB in list) {
      BleService.connect(this, deviceDB.address)
      devices.add(Device(deviceDB))
    }

    currentDevices.clear()
    currentDevices.addAll(devices)

    render()
  }

  private fun deviceConnect(address: String?, connected: Boolean) {
    for (i in currentDevices.indices) {
      if (currentDevices[i].address == address) {
        currentDevices[i].isConnected = connected
        break
      }
    }
  }

  private fun deviceLocationChange(address: String?, location: Location) {
    for (i in currentDevices.indices) {
      if (currentDevices[i].address == address) {
        currentDevices[i].setLocation(location)
        break
      }
    }
  }
}
