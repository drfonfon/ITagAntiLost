package com.fonfon.noloss.ui.newdevice;

import android.location.Location;
import android.util.Pair;

import com.fonfon.noloss.lib.ActivityEvent;
import com.fonfon.noloss.viewstate.NewDevicesViewState;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import io.reactivex.Observable;

public interface NewDeviceView extends MvpView {

  Observable<ActivityEvent> onLifecycleIntent();

  Observable<Object> onRefreshIntent();

  Observable<Pair<String, String>> onDeviceIntent();

  Observable<Location> onNewLocation();

  void render(NewDevicesViewState state);
}
