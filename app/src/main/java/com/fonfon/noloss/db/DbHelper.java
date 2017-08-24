package com.fonfon.noloss.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class DbHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "NoLoss.db";
  private static final int DATABASE_VERSION = 1;

  private static SQLiteDatabase database;

  static {
    cupboard().register(DeviceDB.class);
  }

  public synchronized static SQLiteDatabase getConnection(Context context) {
    if (database == null) {
      database = new DbHelper(context.getApplicationContext()).getWritableDatabase();
    }
    return database;
  }

  public DbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    cupboard().withDatabase(db).createTables();
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    cupboard().withDatabase(db).upgradeTables();
  }

}
