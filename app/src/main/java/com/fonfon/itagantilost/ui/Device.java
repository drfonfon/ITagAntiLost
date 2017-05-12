package com.fonfon.itagantilost.ui;

import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable {

    public ScanResult scanResult;
    public String name;
    public String address;
    public byte batteryLevel;
    public boolean isConnected;
    public boolean isAlarmed;
    public boolean isClicked;

    public Device(ScanResult scanResult) {
        this.scanResult = scanResult;
        address = scanResult.getDevice().getAddress();
        name = scanResult.getScanRecord().getDeviceName();
        if (name == null)
            name = address;
        batteryLevel = 0;
        isAlarmed = false;
        isConnected = false;
        isClicked = false;
    }

    public void click() {
        isClicked = true;
    }

    protected Device(Parcel in) {
        scanResult = in.readParcelable(ScanResult.class.getClassLoader());
        name = in.readString();
        address = in.readString();
        batteryLevel = in.readByte();
        isConnected = in.readByte() != 0;
        isAlarmed = in.readByte() != 0;
        isClicked = in.readByte() != 0;
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
        dest.writeString(address);
        dest.writeByte(batteryLevel);
        dest.writeByte((byte) (isConnected ? 1 : 0));
        dest.writeByte((byte) (isAlarmed ? 1 : 0));
        dest.writeByte((byte) (isClicked ? 1 : 0));
    }
}
