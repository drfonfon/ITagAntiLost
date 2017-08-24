package com.fonfon.noloss.lib;

import android.os.Parcel;
import android.os.Parcelable;

import com.fonfon.noloss.db.DeviceDB;

public class Device implements Parcelable {

  public static final String ADDRESS = "address";
  public static final String ZERO_GEOHASH = "s00000000000";

  private Long _id;
  private String address;
  private String name;
  private String geoHash;
  private String image;

  private boolean isConnected = false;
  private boolean isAlerted = false;

  public Device(DeviceDB deviceDB) {
    this._id = deviceDB.get_id();
    this.address = deviceDB.getAddress();
    this.name = deviceDB.getName();
    this.geoHash = deviceDB.getGeoHash();
    this.image = deviceDB.getImage();
  }

  protected Device(Parcel in) {
    _id = in.readLong();
    address = in.readString();
    name = in.readString();
    geoHash = in.readString();
    image = in.readString();
  }

  public Long get_id() {
    return _id;
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

  public boolean isConnected() {
    return isConnected;
  }

  public void setConnected(boolean connected) {
    isConnected = connected;
  }

  public boolean isAlerted() {
    return isAlerted;
  }

  public void setAlerted(boolean alerted) {
    isAlerted = alerted;
  }

  public static long doHash(String address) {
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
    dest.writeLong(_id);
    dest.writeString(address);
    dest.writeString(name);
    dest.writeString(geoHash);
    dest.writeString(image);
  }
}
