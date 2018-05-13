package com.fonfon.noloss.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import nl.qbusict.cupboard.CupboardFactory.cupboard

class DbHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  override fun onCreate(db: SQLiteDatabase) {
    cupboard().withDatabase(db).createTables()
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    cupboard().withDatabase(db).upgradeTables()
  }

  companion object {

    private const val DATABASE_NAME = "NoLoss.db"
    private const val DATABASE_VERSION = 1

    private var database: SQLiteDatabase? = null

    init {
      cupboard().register(DeviceDB::class.java)
    }

    @Synchronized
    fun getConnection(context: Context): SQLiteDatabase? {
      if (database == null) {
        database = DbHelper(context.applicationContext).writableDatabase
      }
      return database
    }
  }

}
