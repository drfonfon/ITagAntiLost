package com.fonfon.noloss.ui

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.fonfon.noloss.R
import com.fonfon.noloss.ui.newdevice.NewDevicesFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  var address = ""
  var name = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    button_new.setOnClickListener {
      NewDevicesFragment().show(supportFragmentManager, "tag")
    }

  }

  override fun onResume() {
    super.onResume()

    PreferenceManager.getDefaultSharedPreferences(this).apply {
      address = this.getString("address","-")
      name = this.getString("name","-")
      text_address.text = address
      text_name.text = name
    }
  }
}
