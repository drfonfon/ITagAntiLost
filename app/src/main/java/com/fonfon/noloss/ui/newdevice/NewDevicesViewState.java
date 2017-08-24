package com.fonfon.noloss.ui.newdevice;

public interface NewDevicesViewState {

  final class LoadingState implements NewDevicesViewState {

    private final boolean isLoading;

    public LoadingState(boolean isLoading) {
      this.isLoading = isLoading;
    }

    public boolean isLoading() {
      return isLoading;
    }

    @Override
    public String toString() {
      return "LoadingState{}";
    }
  }

  final class NewDeviceState implements NewDevicesViewState {
    private final String address;
    private final String name;

    public NewDeviceState(String address, String name) {
      this.address = address;
      this.name = name;
    }

    public String getAddress() {
      return address;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return "DataState{" +
          "detail=" + "address " + address + " name " + name +
          '}';
    }
  }
}
