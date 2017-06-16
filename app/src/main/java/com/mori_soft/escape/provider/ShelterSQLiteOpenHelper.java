package com.mori_soft.escape.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 避難所情報を格納するSQLiteOpenHelper.
 */

public class ShelterSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Shelter.db";
    private static final int DB_VERSION = 1;


    public ShelterSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SHELTER_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static final String SHELTER_CREATE = "CREATE TABLE " + ShelterDbConst.TBL_NAME + "(" +
            ShelterDbConst._ID              + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT ," +
            ShelterDbConst.CLM_ADRS        + " TEXT    NOT NULL ," +
            ShelterDbConst.CLM_NAME        + " TEXT    NOT NULL ," +
            ShelterDbConst.CLM_TEL         + " TEXT             ," +
            ShelterDbConst.CLM_DETAIL      + " TEXT             ," +
            ShelterDbConst.CLM_IS_SHELTER + " INTEGER NOT NULL ," +
            ShelterDbConst.CLM_IS_TSUNAMI + " INTEGER NOT NULL ," +
            ShelterDbConst.CLM_RANK        + " INTEGER          ," +
            ShelterDbConst.CLM_IS_LIVING  + " INTEGER NOT NULL ," +
            ShelterDbConst.CLM_LAT         + " REAL NOT NULL CHECK(" + ShelterDbConst.CLM_LAT + " >= -90  AND " + ShelterDbConst.CLM_LAT + "<= 90 ) ," +
            ShelterDbConst.CLM_LON         + " REAL NOT NULL CHECK(" + ShelterDbConst.CLM_LON + " >= -180 AND " + ShelterDbConst.CLM_LON + "<= 180) ," +
            ShelterDbConst.CLM_CR_DATE    + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            ShelterDbConst.CLM_UP_DATE    + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP"
            ;

}
