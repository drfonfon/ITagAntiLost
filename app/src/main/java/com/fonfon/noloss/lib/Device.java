package com.fonfon.noloss.lib;

import android.bluetooth.le.ScanResult;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Device extends RealmObject implements Parcelable {

    @Ignore
    public static final String ADDRESS = "address";

    @PrimaryKey
    private String address;
    private String name;
    private double latitude;
    private double longitude;
    private String image;
    @Ignore
    private byte batteryLevel;
    @Ignore
    private boolean isConnected = false;
    @Ignore
    private boolean isAlarmed = false;

    public Device() {
    }

    public Device(ScanResult scanResult) {
        address = scanResult.getDevice().getAddress();
        name = scanResult.getScanRecord().getDeviceName();
        latitude = 0;
        longitude = 0;
        batteryLevel = 0;
        isConnected = false;
        isAlarmed = false;
        image = null;
    }

    protected Device(Parcel in) {
        address = in.readString();
        name = in.readString();
        batteryLevel = in.readByte();
        isConnected = in.readByte() == 0;
        isAlarmed = in.readByte() == 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
        image = in.readString();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setAlarmed(boolean alarmed) {
        isAlarmed = alarmed;
    }

    public boolean isAlarmed() {
        return isAlarmed;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setLocation(Location location) {
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }
    }

    public Location getLocation() {
        Location location = new Location("device");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    int doHash() {
        int hash = 451;
        for (int i = 0; i < address.length(); i++) {
            hash = ((hash << 5) + hash) + address.charAt(i);
        }

        boolean isNegative = (hash < 0);
        hash = Math.abs(hash);

        long mask = 1;
        mask <<= 31;
        if (isNegative) {
            hash |= mask;
        }

        return hash;
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
        dest.writeString(address);
        dest.writeString(name);
        dest.writeByte(batteryLevel);
        dest.writeByte((byte) (isConnected ? 0 : 1));
        dest.writeByte((byte) (isAlarmed ? 0 : 1));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(image);
    }
}
