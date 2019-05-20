package com.mori_soft.escape.download;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * タイムスタンプファイルを扱うためのクラス
 *
 * タイムスタンプファイル
 *   テキストファイル
 *   １行のみで構成
 *   ISO-8601形式で記入
 */
class Timestamp {

    private static final String TAG = Timestamp.class.getSimpleName();

    private static final String DATE_FMT = "yyyy-MM-dd'T'hh:mm:ssZ";
    private static final SimpleDateFormat mSdf = new SimpleDateFormat(DATE_FMT);

    private final Date mTimestampDate;

    public Timestamp(String fn) {
        mTimestampDate = getTimestamp(fn);
    }
    public boolean isValid() {
        return mTimestampDate != null;
    }
    public boolean before(Timestamp t2) {
        if (mTimestampDate == null || t2 == null) {
            // 比較できない場合は false
            return false;
        }
        return mTimestampDate.before(t2.getDate());
    }
    public boolean after(Timestamp t2) {
        if (mTimestampDate == null || t2 == null) {
            // 比較できない場合は false
            return false;
        }
        return mTimestampDate.after(t2.getDate());
    }
    public String toString() {
        return mSdf.format(mTimestampDate);
    }

    private  Date getDate() {
        return mTimestampDate;
    }

    // ファイルに記載された日時を取得
    private static Date getTimestamp(String fn) {

        File f = new File(fn);
        Date dt = null;
        String ts = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));

            ts = br.readLine();
            dt = mSdf.parse(ts);

            Log.d(TAG, "タイムスタンプ : " + dt.toString() + ", for " + fn);
            br.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "タイムスタンプファイルが見つかりません : " + fn, e);
        } catch (IOException e) {
            Log.e(TAG, "タイムスタンプファイルオープンに失敗しました : " + fn, e);
        } catch (ParseException e) {
            Log.e(TAG, "タイムスタンプファイルの中身が日付ではありません : " + ts, e);
        }

        return dt;
    }
}
