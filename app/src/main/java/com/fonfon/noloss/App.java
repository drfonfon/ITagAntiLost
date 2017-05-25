package com.fonfon.noloss;

import android.app.Application;

import io.realm.Realm;

public final class App extends Application {

    public static final String ALL_ADDRESSES = "ALL_ADDRESSES";

    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private String visibleAddress = null;

    public void setVisibleAddress(
            String visibleAddress
    ) {
        this.visibleAddress = visibleAddress;
    }

    public String getVisibleAddress() {
        return visibleAddress;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Realm.init(getApplicationContext());
    }
}
