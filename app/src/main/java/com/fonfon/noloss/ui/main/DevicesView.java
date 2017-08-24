package com.fonfon.noloss.ui.main;

import com.fonfon.noloss.ActivityEvent;
import com.fonfon.noloss.lib.Device;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import io.reactivex.Observable;

interface DevicesView extends MvpView {

  Observable<ActivityEvent> onLifecycleIntent();

  Observable<Object> newDeviceIntent();

  Observable<Device> deleteDeviceIntent();

  Observable<Device> alertDeviceIntent();

  Observable<Device> renameDeviceIntent();

  Observable<Device> editImageDeviceIntent();

  void render(DevicesViewState state);
}
