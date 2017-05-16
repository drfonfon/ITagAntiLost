package com.fonfon.noloss.lib;

import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Device implements Parcelable {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTION = 1;
    public static final int CONNECTED = 2;
    public static final int ALARMED = 3;

    private ScanResult scanResult;
    private String name;
    private byte batteryLevel;
    private int status = DISCONNECTED;

    @IntDef({DISCONNECTED, CONNECTION, CONNECTED, ALARMED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }

    public Device(ScanResult scanResult) {
        this.scanResult = scanResult;
        name = scanResult.getScanRecord().getDeviceName();
        batteryLevel = 0;
        status = DISCONNECTED;
    }

    protected Device(Parcel in) {
        scanResult = in.readParcelable(ScanResult.class.getClassLoader());
        name = in.readString();
        batteryLevel = in.readByte();
        status = in.readInt();
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(byte batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public @Status int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(scanResult, flags);
        dest.writeString(name);
        dest.writeByte(batteryLevel);
        dest.writeInt(status);
    }
}
