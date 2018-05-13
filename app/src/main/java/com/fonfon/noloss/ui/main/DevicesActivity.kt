package com.fonfon.noloss.ui.main

import android.content.Intent
import android.os.Bundle
import com.fonfon.noloss.R
import com.fonfon.noloss.ui.LocationActivity
import com.fonfon.noloss.ui.newdevice.NewDeviceActivity
import kotlinx.android.synthetic.main.activity_devices.*

class DevicesActivity : LocationActivity(){

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_devices)

    button_new_device.setOnClickListener { startActivity(Intent(this, NewDeviceActivity::class.java)) }
  }
}
