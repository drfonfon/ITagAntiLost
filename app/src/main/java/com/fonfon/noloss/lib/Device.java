package com.fonfon.noloss.lib;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.fonfon.geohash.GeoHash;
import com.fonfon.noloss.R;
import com.fonfon.noloss.db.DeviceDB;

public final class Device {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Device device = (Device) o;
    return _id.equals(device.get_id()) &&
        address.equals(device.getAddress()) &&
        name.equals(device.getName()) &&
        image.equals(device.getImage());
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

  public void setLocation(Location location) {
    geoHash = location == null ? ZERO_GEOHASH : GeoHash.fromLocation(location, GeoHash.MAX_CHARACTER_PRECISION).toString();
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

  public static Bitmap getBitmapImage(String image, Resources resources) {
    Bitmap bitmap = null;
    if (!image.equals("img")) {
      bitmap = BitmapUtils.stringToBitMap(image);
    }
    if (bitmap == null) {
      bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
    }
    return bitmap;
  }
}
