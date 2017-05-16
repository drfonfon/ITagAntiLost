package com.fonfon.noloss;

import android.app.Application;
import android.bluetooth.le.ScanResult;

import com.fonfon.noloss.lib.Device;

import java.util.HashMap;

public class App extends Application {

    private static HashMap<String, Device> devices = new HashMap<>();

    public static void addDevice(ScanResult scanResult) {
        devices.put(scanResult.getDevice().getAddress(), new Device(scanResult));
    }

    public static HashMap<String, Device> getDevices() {
        return devices;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
