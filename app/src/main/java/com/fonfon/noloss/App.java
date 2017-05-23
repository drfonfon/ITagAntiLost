package com.fonfon.noloss;

import android.app.Application;

import io.realm.Realm;

public final class App extends Application {

    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private boolean activityVisible = false;

    public void setActivityVisible(boolean activityVisible) {
        this.activityVisible = activityVisible;
    }

    public boolean isActivityVisible() {
        return activityVisible;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Realm.init(getApplicationContext());
    }
}
