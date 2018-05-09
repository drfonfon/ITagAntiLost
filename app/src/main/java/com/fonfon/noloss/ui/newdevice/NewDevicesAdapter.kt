package com.fonfon.noloss.ui.newdevice

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fonfon.noloss.R
import kotlinx.android.synthetic.main.item_new_device.view.*
import java.util.*

internal class NewDevicesAdapter(private val listener: NewDevicesAdapter.Listener) : RecyclerView.Adapter<NewDevicesAdapter.Holder>() {

  private val devices = HashMap<String, String>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
      LayoutInflater.from(parent.context).inflate(R.layout.item_new_device, parent, false)
  )

  override fun onBindViewHolder(holder: Holder, position: Int) {
    val addresses = devices.keys.toTypedArray()
    holder.itemView.text_name.text = devices[addresses[position]]
    holder.itemView.text_address.text = addresses[position]
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
        if (adapterPosition != RecyclerView.NO_POSITION) {
          val addresses = devices.keys.toTypedArray()
          if (addresses.isNotEmpty()) {
            listener.onDevice(addresses[adapterPosition], devices[addresses[adapterPosition]])
          }
        }
      }
    }

  }

  internal interface Listener {
    fun onDevice(address: String, name: String?)
  }
}

