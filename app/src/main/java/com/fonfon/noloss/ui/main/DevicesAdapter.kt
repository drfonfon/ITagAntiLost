package com.fonfon.noloss.ui.main

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fonfon.noloss.R
import com.fonfon.noloss.lib.Device
import kotlinx.android.synthetic.main.item_device.view.*
import java.util.*

internal class DevicesAdapter(context: Context, private val listener: DeviceAdapterListener) : RecyclerView.Adapter<DevicesAdapter.Holder>() {

  private val devices = ArrayList<Device>()

  private val connected = context.getString(R.string.status_connected)
  private val disconnected = context.getString(R.string.status_disconnected)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
      LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
  )

  override fun onBindViewHolder(holder: DevicesAdapter.Holder, position: Int) {
    val device = devices[position]

    holder.itemView.toolbar.title = device.name
    holder.itemView.toolbar.subtitle = device.address

    holder.itemView.viewStatus.isEnabled = device.isConnected
    holder.itemView.fabAlert.setImageResource(if (device.isAlerted) R.drawable.ic_volume_off else R.drawable.ic_volume_up)
    holder.itemView.textStatus.text = if (device.isConnected) connected else disconnected
  }

  override fun getItemCount() = devices.size

  fun setDevices(devices: List<Device>) {
    this.devices.clear()
    this.devices.addAll(devices)

    notifyDataSetChanged()
  }

  internal inner class Holder(view: View) : RecyclerView.ViewHolder(view) {

    init {
      itemView.toolbar.inflateMenu(R.menu.device_detail)
      itemView.fabAlert.setOnClickListener { listener.onAlert(devices[adapterPosition]) }
      itemView.toolbar.setOnMenuItemClickListener { item ->
        when (item.itemId) {
          R.id.menu_delete -> listener.onDelete(devices[adapterPosition])
          R.id.menu_rename -> listener.onRename(devices[adapterPosition])
        }
        false
      }
    }
  }

  internal interface DeviceAdapterListener {
    fun onRename(device: Device)

    fun onDelete(device: Device)

    fun onAlert(device: Device)
  }
}
