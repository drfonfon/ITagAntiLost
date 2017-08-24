package com.fonfon.noloss.ui.main;

import com.fonfon.noloss.lib.Device;

import java.util.List;

interface DevicesViewState {

  final class DataState implements DevicesViewState {
    private final List<Device> data;

    DataState(List<Device> data) {
      this.data = data;
    }

    public List<Device> getData() {
      return data;
    }

    @Override public String toString() {
      return "DataState{" +
          "detail=" + data.toString() +
          '}';
    }
  }

}
