package com.fonfon.noloss.lib;

import android.os.Looper;

import io.reactivex.Observer;

public final class ObservableUtils {

  public static boolean checkMainThread(Observer<?> observer) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      observer.onError(new IllegalStateException(
          "Expected to be called on the main thread but was " + Thread.currentThread().getName()));
      return false;
    }
    return true;
  }
}
