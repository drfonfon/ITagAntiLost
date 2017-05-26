package com.fonfon.noloss.lib;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Device extends RealmObject implements Parcelable {

    @Ignore
    public static final String ADDRESS = "address";
    @Ignore
    public static final String ZERO_GEOHASH = "s00000000000";

    @PrimaryKey
    private String address;
    private String name;
    private String geoHash;
    private String image;

    @Ignore
    private boolean isConnected;

    public Device() {
    }

    public Device(String address, String name, String defaultImage) {
        this.address = address;
        this.name = name;
        geoHash = ZERO_GEOHASH;
        this.image = defaultImage;
    }

    protected Device(Parcel in) {
        address = in.readString();
        name = in.readString();
        geoHash = in.readString();
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

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isConnected() {
        return isConnected;
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
        dest.writeString(geoHash);
        dest.writeString(image);
    }
}
