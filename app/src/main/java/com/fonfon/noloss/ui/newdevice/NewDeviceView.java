package com.fonfon.noloss.ui.newdevice;

import android.util.Pair;

import com.fonfon.noloss.ActivityEvent;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import io.reactivex.Observable;

interface NewDeviceView extends MvpView {

  Observable<ActivityEvent> onLifecycleIntent();

  Observable<Object> onRefreshIntent();

  Observable<Pair<String, String>> onDeviceIntent();

  void render(NewDevicesViewState state);
}
