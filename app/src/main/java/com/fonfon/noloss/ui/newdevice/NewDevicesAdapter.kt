package com.fonfon.noloss.ui.newdevice

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fonfon.noloss.R
import kotlinx.android.synthetic.main.item_new_device.view.*
import java.util.*

internal class NewDevicesAdapter : RecyclerView.Adapter<NewDevicesAdapter.Holder>() {

  val devices = HashMap<String, String>()
  var onDevice = { address: String, name: String -> }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
      LayoutInflater.from(parent.context).inflate(R.layout.item_new_device, parent, false)
  )

  override fun onBindViewHolder(holder: Holder, position: Int) {
    devices.keys.toTypedArray().also {
      holder.itemView.apply {
        text_name.text = devices[it[position]]
        text_address.text = it[position]
      }
    }
  }

  override fun getItemCount() = devices.size

  fun add(address: String, name: String) {
    devices[address] = name
    notifyDataSetChanged()
  }

  fun clear() {
    devices.clear()
    notifyDataSetChanged()
  }

  internal inner class Holder(view: View) : RecyclerView.ViewHolder(view) {

    init {
      view.setOnClickListener {
        val addresses = devices.keys.toTypedArray()
        if (RecyclerView.NO_POSITION != adapterPosition && addresses.isNotEmpty()) {
          devices[addresses[adapterPosition]]?.let {
            onDevice(addresses[adapterPosition], it)
          }
        }
      }
    }

  }
}

