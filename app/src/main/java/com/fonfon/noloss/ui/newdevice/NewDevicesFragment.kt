package com.fonfon.noloss.ui.newdevice

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fonfon.noloss.BleService
import com.fonfon.noloss.R
import kotlinx.android.synthetic.main.fragment_new_device.*

class NewDevicesFragment: BottomSheetDialogFragment() {

  private val requestEnableBT = 451
  private val adapter = NewDevicesAdapter()
  private lateinit var bluetoothAdapter: BluetoothAdapter

  private val scanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
      for (uuid in result.scanRecord.serviceUuids) {
        if (BleService.FIND_ME_SERVICE == uuid.uuid) {
          if (!adapter.devices.containsKey(result.device.address))
            adapter.add(result.device.address, result.scanRecord.deviceName)
          break
        }
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
     inflater.inflate(R.layout.fragment_new_device, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activity?.let {
      if (!it.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        Toast.makeText(it, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
        dismiss()
      }
    }

    bluetoothAdapter = (activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    adapter.onDevice = { address: String, name: String ->
      PreferenceManager.getDefaultSharedPreferences(activity).edit()
          .putString("address", address)
          .putString("name", name)
          .apply()
      dismiss()
    }
    recycler.adapter = adapter
  }

  override fun onResume() {
    super.onResume()
    ripple.startRippleAnimation()
    if (!bluetoothAdapter.isEnabled) {
      startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), requestEnableBT)
    } else {
      adapter.clear()
      bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }
  }

  override fun onPause() {
    ripple.stopRippleAnimation()
    bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    super.onPause()
  }

}