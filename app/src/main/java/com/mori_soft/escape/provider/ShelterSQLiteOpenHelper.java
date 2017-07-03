package com.mori_soft.escape.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mori_soft.escape.Util.ShelterCsvReader;
import com.mori_soft.escape.entity.ShelterEntity;

import java.util.List;

/**
 * 避難所情報を格納するSQLiteOpenHelper.
 */

public class ShelterSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = ShelterSQLiteOpenHelper.class.getSimpleName();

    private static final String DB_NAME = "SelectedShelter.db";
    private static final int DB_VERSION = 1;
    private static final String ASSET_FILE_INITIAL_DATA = "201703_iseshi_shelters.csv";

    private Context mContext;

    public ShelterSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        db.execSQL(SHELTER_CREATE);

        // 初期データ設定
        String sql = createInitialDataSql();
        //Log.d(TAG, "init sql: " + sql);
        db.execSQL(sql);
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
            ShelterDbConst.CLM_MEMO        + " TEXT             ," +
            ShelterDbConst.CLM_LAT         + " REAL NOT NULL CHECK(" + ShelterDbConst.CLM_LAT + " >= -90  AND " + ShelterDbConst.CLM_LAT + "<= 90 ) ," +
            ShelterDbConst.CLM_LON         + " REAL NOT NULL CHECK(" + ShelterDbConst.CLM_LON + " >= -180 AND " + ShelterDbConst.CLM_LON + "<= 180) ," +
            ShelterDbConst.CLM_CR_DATE    + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP ," +
            ShelterDbConst.CLM_UP_DATE    + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ")"
            ;

    private static final char DLM = ',';
    private static final String INIT_DATA_INSERT = "INSERT INTO " + ShelterDbConst.TBL_NAME + " (" +
            ShelterDbConst.CLM_ADRS        + DLM +
            ShelterDbConst.CLM_NAME        + DLM +
            ShelterDbConst.CLM_TEL         + DLM +
            ShelterDbConst.CLM_DETAIL      + DLM +
            ShelterDbConst.CLM_IS_SHELTER + DLM +
            ShelterDbConst.CLM_IS_TSUNAMI + DLM +
            ShelterDbConst.CLM_RANK        + DLM +
            ShelterDbConst.CLM_IS_LIVING  + DLM +
            ShelterDbConst.CLM_MEMO        + DLM +
            ShelterDbConst.CLM_LAT         + DLM +
            ShelterDbConst.CLM_LON                + ") VALUES ";
    private static final char SQ = '\'';

    private String createInitialDataSql() {

        StringBuilder sb = new StringBuilder(INIT_DATA_INSERT);

        List<ShelterEntity> list = ShelterCsvReader.parse(mContext, ASSET_FILE_INITIAL_DATA);
        for (ShelterEntity ent : list) {
            sb.append("(");
            addElm(sb, ent.address);
            addElm(sb, ent.shelterName);
            addElm(sb, ent.tel);
            addElm(sb, ent.detail);
            addElm(sb, ent.isShelter);
            addElm(sb, ent.isTsunami);
            addElm(sb, ent.ranking.getRankingValue());
            addElm(sb, ent.isLiving);
            addElm(sb, ent.memo);
            addElm(sb, ent.lat);
            addElm(sb, ent.lon);
            sb.delete(sb.length()-1, sb.length());
            sb.append("),");
        }
        sb.delete(sb.length()-1, sb.length());

        return sb.toString();
    }
    private void addElm(StringBuilder sb, String elm) {
        sb.append(SQ);
        sb.append(elm);
        sb.append(SQ);
        sb.append(DLM);
    }
    private void addElm(StringBuilder sb, int elm) {
        addElm(sb, String.valueOf(elm));
    }
    private void addElm(StringBuilder sb, boolean elm) {
        addElm(sb, String.valueOf(elm ? 1 : 0));
    }
    private void addElm(StringBuilder sb, double elm) {
        addElm(sb, String.valueOf(elm));
    }

}
