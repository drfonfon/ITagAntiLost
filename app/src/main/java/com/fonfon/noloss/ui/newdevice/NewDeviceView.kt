package com.fonfon.noloss.ui.newdevice

import android.location.Location
import android.util.Pair
import com.fonfon.noloss.NewDevicesViewState

import com.fonfon.noloss.lib.ActivityEvent
import com.hannesdorfmann.mosby3.mvp.MvpView

import io.reactivex.Observable

interface NewDeviceView : MvpView {

  fun onLifecycleIntent(): Observable<ActivityEvent>

  fun onRefreshIntent(): Observable<Any>

  fun onDeviceIntent(): Observable<Pair<String, String>>

  fun onNewLocation(): Observable<Location>

  fun render(state: NewDevicesViewState)
}
