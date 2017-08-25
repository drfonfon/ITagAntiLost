package com.fonfon.noloss.viewstate;

import com.fonfon.noloss.lib.Device;

import java.util.List;

public interface DevicesViewState {

  final class DataState implements DevicesViewState {
    private final List<Device> data;

    public DataState(List<Device> data) {
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
