package com.fonfon.noloss

import com.fonfon.noloss.lib.Device

interface DevicesViewState {

  class DataState(val data: List<Device>) : DevicesViewState {
    override fun toString() = "DataState{detail= $data }"
  }

}

interface NewDevicesViewState {

  class LoadingState(val isLoading: Boolean) : NewDevicesViewState {
    override fun toString() = "LoadingState{}"
  }

  class NewDeviceState(val address: String, val name: String) : NewDevicesViewState {
    override fun toString() = "DataState{detail=address $address name $name}"
  }
}
