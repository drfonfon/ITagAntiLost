package com.fonfon.itagantilost;

import android.app.Application;
import android.bluetooth.le.ScanResult;

import java.util.ArrayList;

public class App extends Application {

    private static ArrayList<ScanResult> scanResults = new ArrayList<>();

    public static void add(ScanResult scanResult) {
        scanResults.add(scanResult);
    }

    public static ArrayList<ScanResult> getScanResults() {
        return scanResults;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
