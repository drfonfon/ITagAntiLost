package com.fonfon.noloss.ui.main

import com.fonfon.noloss.DevicesViewState
import com.fonfon.noloss.lib.ActivityEvent
import com.fonfon.noloss.lib.Device
import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.Observable

interface DevicesView : MvpView {

  fun onLifecycleIntent(): Observable<ActivityEvent>

  fun newDeviceIntent(): Observable<Any>

  fun deleteDeviceIntent(): Observable<Device>

  fun alertDeviceIntent(): Observable<Device>

  fun updateDeviceIntent(): Observable<Device>

  fun refreshIntent(): Observable<Any>

  fun render(state: DevicesViewState)
}
