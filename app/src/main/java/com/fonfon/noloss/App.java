package com.fonfon.noloss;

import android.app.Application;

import io.realm.Realm;

public final class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
    }
}
