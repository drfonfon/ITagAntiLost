package com.fonfon.noloss.ui.main;

import com.fonfon.noloss.lib.ActivityEvent;
import com.fonfon.noloss.lib.Device;
import com.fonfon.noloss.viewstate.DevicesViewState;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import io.reactivex.Observable;

public interface DevicesView extends MvpView {

  Observable<ActivityEvent> onLifecycleIntent();

  Observable<Object> newDeviceIntent();

  Observable<Device> deleteDeviceIntent();

  Observable<Device> alertDeviceIntent();

  Observable<Device> updateDeviceIntent();

  Observable<Object> refreshIntent();

  void render(DevicesViewState state);
}
