package com.mori_soft.escape.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * 避難所情報のコンテンツプロバイダ.
 */

public class ShelterContentProvider extends ContentProvider {

    private static final int INT_SHELTERS = 1;
    private static final int INT_SHELTER_ITEM = 2;

    private ShelterSQLiteOpenHelper mHelper;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(ShelterContract.AUTHORITY, ShelterContract.Shelter.PATH, INT_SHELTERS);
        mUriMatcher.addURI(ShelterContract.AUTHORITY, ShelterContract.Shelter.PATH + "/#", INT_SHELTER_ITEM);
    }


    @Override
    public boolean onCreate() {
        mHelper = new ShelterSQLiteOpenHelper(this.getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case INT_SHELTERS:
                return ShelterContract.Shelter.CONTENT_TYPE;
            case INT_SHELTER_ITEM:
                return ShelterContract.Shelter.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri.toString());
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (mUriMatcher.match(uri)) {
            case INT_SHELTERS:
                qb.setTables(ShelterDbConst.TBL_NAME);
                break;
            case INT_SHELTER_ITEM:
                qb.setTables(ShelterDbConst.TBL_NAME);
                qb.appendWhere(ShelterDbConst._ID + "=" + ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri.toString());
        }

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(this.getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // insert は shelters の形式で呼ばれる（ shelters/# ではない）
        if (mUriMatcher.match(uri) == INT_SHELTER_ITEM) {
            throw new IllegalArgumentException("Unknown uri: " + uri.toString());
        }
        if (null == values) {
            throw new IllegalArgumentException("Row data is null");
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long rowid = db.insert(ShelterDbConst.TBL_NAME, null, values);

        if (rowid > 0) {
            Uri res = ContentUris.withAppendedId(ShelterContract.Shelter.CONTENT_URI, rowid);

            this.getContext().getContentResolver().notifyChange(res, null);
            return res;
        }
        throw new SQLException("Failed to insert row: " + uri.toString());
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int cnt;
        switch (mUriMatcher.match(uri)) {
            case INT_SHELTERS:
                cnt = db.delete(ShelterDbConst.TBL_NAME, selection, selectionArgs);
                break;
            case INT_SHELTER_ITEM:
                final long target_id = ContentUris.parseId(uri);
                cnt = db.delete(ShelterDbConst.TBL_NAME, getSelectionClause(target_id, selection), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri : " + uri.toString());
        }

        this.getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int cnt;
        switch (mUriMatcher.match(uri)) {
            case INT_SHELTERS:
                cnt = db.update(ShelterDbConst.TBL_NAME, values, selection, selectionArgs);
                break;
            case INT_SHELTER_ITEM:
                final long target_id = ContentUris.parseId(uri);
                cnt = db.update(ShelterDbConst.TBL_NAME, values, getSelectionClause(target_id, selection), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri : " + uri.toString());
        }

        this.getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            ContentProviderResult[] result = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return result;
        } finally {
            db.endTransaction();
        }
    }

    private String getSelectionClause(long target_id, String selection) {
        return ShelterDbConst._ID + " = " + target_id + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + " )" : "" );
    }

}
