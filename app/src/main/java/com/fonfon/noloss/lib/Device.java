package com.fonfon.noloss.lib;

import android.bluetooth.le.ScanResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import io.realm.Realm;
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
    @Ignore
    private Bitmap bitmap;

    public Device() {
        if(image != null) {
            bitmap = stringToBitMap(image);
        }
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
        if(image != null) {
            bitmap = stringToBitMap(image);
        }
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
        if(image != null) {
            bitmap = stringToBitMap(image);
        } else {
            bitmap = null;
        }
    }

    public Bitmap getBitmap() {
        if(bitmap == null && image != null) {
            bitmap = stringToBitMap(image);
        }
        return bitmap;
    }

    public void setBitmapImage(Bitmap bitmap) {
        this.image = bitMapToString(bitmap);
        this.bitmap = bitmap;
    }

    private String bitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
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

    public static void addToRealm(
            final ScanResult scanResult,
            Realm.Transaction.OnSuccess onSuccess,
            Realm.Transaction.OnError onError
    ) {
        Realm.getDefaultInstance()
                .executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(new Device(scanResult));
                    }
                }, onSuccess, onError);
    }

    public int doHash() {
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
