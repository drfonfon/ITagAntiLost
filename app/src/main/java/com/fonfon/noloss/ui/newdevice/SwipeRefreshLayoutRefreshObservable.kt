package com.fonfon.noloss.ui.newdevice

import android.support.v4.widget.SwipeRefreshLayout

import com.fonfon.noloss.lib.ObservableUtils
import com.jakewharton.rxbinding2.internal.Notification

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

internal class SwipeRefreshLayoutRefreshObservable(private val view: SwipeRefreshLayout) : Observable<Any>() {

  override fun subscribeActual(observer: Observer<in Any>) {
    if (!ObservableUtils.checkMainThread(observer)) {
      return
    }
    val listener = Listener(view, observer)
    observer.onSubscribe(listener)
    view.setOnRefreshListener(listener)
  }

  internal class Listener(private val view: SwipeRefreshLayout, private val observer: Observer<in Any>) : MainThreadDisposable(), SwipeRefreshLayout.OnRefreshListener {

    override fun onRefresh() {
      if (!isDisposed) {
        observer.onNext(Notification.INSTANCE)
      }
    }

    override fun onDispose() = view.setOnRefreshListener(null)
  }
}

