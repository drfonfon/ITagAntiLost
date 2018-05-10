package com.fonfon.noloss.lib

import android.os.Looper

import io.reactivex.Observer

object ObservableUtils {

  fun checkMainThread(observer: Observer<*>): Boolean {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      observer.onError(IllegalStateException("""Expected to be called on the main thread but was ${Thread.currentThread().name}"""))
      return false
    }
    return true
  }
}
