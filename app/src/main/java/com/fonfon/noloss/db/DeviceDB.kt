package com.fonfon.noloss.db

import android.location.Location

import com.fonfon.geohash.GeoHash
import com.fonfon.noloss.lib.Device

import com.fonfon.noloss.lib.Device.ZERO_GEOHASH

class DeviceDB {

  var _id: Long? = null
  var address: String? = null
  var name: String? = null
  var geoHash: String? = null
  var image: String? = null

  constructor() {}

  constructor(address: String, name: String, defaultImage: String, location: Location?) {
    this._id = Device.doHash(address)
    this.address = address
    this.name = name
    geoHash = if (location == null) ZERO_GEOHASH else GeoHash.fromLocation(location, GeoHash.MAX_CHARACTER_PRECISION).toString()
    this.image = defaultImage
  }

  constructor(device: Device) {
    this._id = device._id
    this.address = device.address
    this.name = device.name
    this.geoHash = device.geoHash
    this.image = device.image
  }
}
