package com.fonfon.noloss.ui.newdevice

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.fonfon.noloss.BleService
import com.fonfon.noloss.R
import com.fonfon.noloss.ui.LocationActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.android.synthetic.main.activity_new_device.*
import java.util.*

class NewDeviceActivity : LocationActivity() {

  private var adapter: NewDevicesAdapter? = null

  private var bluetoothAdapter: BluetoothAdapter? = null

  private var currentLocation: Location? = null
  private val currentAddresses = ArrayList<String?>()

  private val scanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
      super.onScanResult(callbackType, result)
      result.scanRecord?.serviceUuids?.let { uuids->
        for (uuid in uuids) {
          if (uuid.uuid == BleService.FIND_ME_SERVICE) {
            if (!currentAddresses.contains(result.device.address))
              renderDataState(result.device.address, result.scanRecord!!.deviceName)
            break
          }
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_device)

    adapter = NewDevicesAdapter(object : NewDevicesAdapter.Listener {
      override fun onDevice(address: String, name: String?) {
        //add device
        finish()
      }
    })
    recycler.layoutManager = LinearLayoutManager(this)
    recycler.adapter = adapter

    toolbar.setNavigationIcon(R.drawable.ic_back)
    toolbar.setNavigationOnClickListener { v -> finish() }

    if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
      finish()
    }

    bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    refresh.setOnRefreshListener({ refresh() })

    refresh()

    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult?) {
        super.onLocationResult(locationResult)
        currentLocation = locationResult!!.lastLocation
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
      startActivityForResult(
          Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
          NewDeviceActivity.REQUEST_ENABLE_BT
      )
    } else {
      refresh()
    }
  }

  override fun onPause() {
    bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    super.onPause()
  }

  private fun renderLoadingState(isLoading: Boolean) {
    if (isLoading) {
      adapter!!.clear()
    }
    refresh.isRefreshing = isLoading
    empty_text.visibility = if (isLoading || recycler.adapter.itemCount > 0) View.GONE else View.VISIBLE
  }

  private fun renderDataState(address: String, name: String?) {
    adapter!!.add(address, name!!)
  }

  private fun refresh() {
    renderLoadingState(true)
    bluetoothAdapter!!.bluetoothLeScanner.startScan(scanCallback)

    Handler().postDelayed({
      bluetoothAdapter!!.bluetoothLeScanner.stopScan(scanCallback)
      renderLoadingState(false)
    }, 8000)
  }

  companion object {

    val REQUEST_ENABLE_BT = 451
  }
}
