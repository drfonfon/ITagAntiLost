package com.fonfon.noloss.ui.newdevice

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fonfon.noloss.R
import com.jakewharton.rxbinding2.view.RxView
import java.util.*

internal class NewDevicesAdapter(private val listener: NewDevicesAdapter.Listener) : RecyclerView.Adapter<NewDevicesAdapter.Holder>() {

  private val devices = HashMap<String, String>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
      LayoutInflater.from(parent.context).inflate(R.layout.item_new_device, parent, false)
  )

  override fun onBindViewHolder(holder: Holder, position: Int) {
    val keySet = devices.keys
    val addresses = keySet.toTypedArray()
    holder.textName.text = devices[addresses[position]]
    holder.textAddress.text = addresses[position]
  }

  override fun onViewRecycled(holder: Holder) {
    holder.itemView.setOnClickListener(null)
    super.onViewRecycled(holder)
  }

  override fun getItemCount(): Int {
    return devices.size
  }

  fun add(address: String, name: String) {
    devices[address] = name
    notifyDataSetChanged()
  }

  fun clear() {
    devices.clear()
    notifyDataSetChanged()
  }

  internal inner class Holder(view: View) : RecyclerView.ViewHolder(view) {

    val textName: TextView = view.findViewById(R.id.text_name)
    val textAddress: TextView = view.findViewById(R.id.text_address)

    init {

      RxView.clicks(view)
          .map { adapterPosition }
          .filter { p -> p != RecyclerView.NO_POSITION }
          .subscribe { p ->
            val keySet = devices.keys
            val addresses = keySet.toTypedArray()
            if (addresses.isNotEmpty()) {
              listener.onDevice(addresses[p], devices[addresses[p]])
            }
          }
    }

  }

  internal interface Listener {
    fun onDevice(address: String, name: String?)
  }
}

