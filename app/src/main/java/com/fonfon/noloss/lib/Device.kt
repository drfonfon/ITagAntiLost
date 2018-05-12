package com.fonfon.noloss.lib

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location

import com.fonfon.geohash.GeoHash
import com.fonfon.noloss.R
import com.fonfon.noloss.db.DeviceDB

class Device(deviceDB: DeviceDB) {

  val _id: Long?
  var address: String? = null
  var name: String? = null
  var geoHash: String? = null

  var isConnected = false
  var isAlerted = false

  init {
    this._id = deviceDB._id
    this.address = deviceDB.address
    this.name = deviceDB.name
    this.geoHash = deviceDB.geoHash
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false
    val device = o as Device?
    return _id == device!!._id &&
        address == device.address &&
        name == device.name
  }

  fun setLocation(location: Location?) {
    geoHash = if (location == null) ZERO_GEOHASH else GeoHash.fromLocation(location, GeoHash.MAX_CHARACTER_PRECISION).toString()
  }

  companion object {

    val ZERO_GEOHASH = "s00000000000"

    fun doHash(address: String): Long {
      var hash = 451
      for (i in 0 until address.length) {
        hash = (hash shl 5) + hash + address[i].toInt()
      }

      val isNegative = hash < 0
      hash = Math.abs(hash)

      var mask: Long = 1
      mask = mask shl 31
      if (isNegative) {
        hash = hash or mask.toInt()
      }

      return hash.toLong()
    }
  }
}
