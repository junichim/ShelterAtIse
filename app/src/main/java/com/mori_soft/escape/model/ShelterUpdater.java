package com.mori_soft.escape.model;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.mori_soft.escape.Util.AssetFileUtils;
import com.mori_soft.escape.Util.FileUtil;
import com.mori_soft.escape.Util.ShelterCsvReader;
import com.mori_soft.escape.download.Timestamp;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.provider.ShelterContract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 避難所データ更新を行うクラス
 *
 * 避難所データは、ひとつ前のファイルをバックアップとして持ち、
 * 更新失敗時にはロールバックができるようにする
 * （CSVファイルの記述ミスが想定されるため）
 */
public class ShelterUpdater {

    private static final String TAG = ShelterUpdater.class.getSimpleName();

    public final static String SHELTER_FILE = "iseshi_shelters.csv";
    public final static String SHELTER_TIMESTAMP = "shelter_timestamp";
    public final static String BAK_SHELTER_FILE = "iseshi_shelters.csv.bak";
    public final static String BAK_SHELTER_TIMESTAMP = "shelter_timestamp.bak";

    public static void updateShelter(Context context) {
        Log.d(TAG, "避難所データの更新開始");

        // csv ファイルを読み込み
        final String fn = getOfflineShelterFile(context);
        List<ShelterEntity> list = ShelterCsvReader.parse(context, fn);

        updateShelterDatabase(context, list);
    }

    /**
     * 避難所ファイルをロールバックする
     *
     * 避難所データ更新時の処理はトランザクションで保護されているので
     * 更新処理が失敗した場合、ファイルだけを元に戻せばよい
     * @param context
     */
    public static void rollbackShelterFiles(Context context) {
        Log.d(TAG, "避難所ファイルのロールバック開始");

        // バックアップファイルを戻す
        if (! rollbackBackupfiles(context)) {
            throw new RuntimeException("failed to rollback");
        }
    }

    private static void updateShelterDatabase(Context context, List<ShelterEntity> list) {
        // 避難所データの更新, 処理は batch で行う
        ContentResolver resolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // 最初に既存データを削除し、
        ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(ShelterContract.Shelter.CONTENT_URI);
        batch.add(builder.build());

        // その後避難所データを追加する
        for (ShelterEntity ent : list) {
            builder = ContentProviderOperation.newInsert(ShelterContract.Shelter.CONTENT_URI);

            builder.withValue(ShelterContract.Shelter.ADDRESS,    ent.address);
            builder.withValue(ShelterContract.Shelter.NAME,       ent.shelterName);
            builder.withValue(ShelterContract.Shelter.TEL,        ent.tel);
            builder.withValue(ShelterContract.Shelter.DETAIL,     ent.detail);
            builder.withValue(ShelterContract.Shelter.IS_SHELTER, ent.isShelter);
            builder.withValue(ShelterContract.Shelter.IS_TSUNAMI, ent.isTsunami);
            builder.withValue(ShelterContract.Shelter.RANK,       ent.ranking.getRankingValue());
            builder.withValue(ShelterContract.Shelter.IS_LIVING,  ent.isLiving);
            builder.withValue(ShelterContract.Shelter.MEMO,       ent.memo);
            builder.withValue(ShelterContract.Shelter.LAT,        ent.lat);
            builder.withValue(ShelterContract.Shelter.LON,        ent.lon);

            batch.add(builder.build());
        }

        // ShelterContentProvider#applyBatch による処理はトランザクションで保護済み
        try {
            resolver.applyBatch(ShelterContract.AUTHORITY, batch);
        } catch (OperationApplicationException e) {
            Log.w(TAG, "データベースの避難所データ更新時にエラーが発生しました", e);
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            Log.w(TAG, "データベースの避難所データ更新時にエラーが発生しました", e);
            throw new RuntimeException(e);
        }
    }


    private static String getOfflineShelterFile(Context context) {
        return new File(context.getExternalFilesDir(null),  "/" + SHELTER_FILE).getAbsolutePath();
    }
    private static String getOfflineShelterTimestampFile(Context context) {
        return new File(context.getExternalFilesDir(null),  "/" + SHELTER_TIMESTAMP).getAbsolutePath();
    }
    private static String getOfflineBackupShelterFile(Context context) {
        return new File(context.getExternalFilesDir(null),  "/" + BAK_SHELTER_FILE).getAbsolutePath();
    }
    private static String getOfflineBackupShelterTimestampFile(Context context) {
        return new File(context.getExternalFilesDir(null),  "/" + BAK_SHELTER_TIMESTAMP).getAbsolutePath();
    }

    /**
     * 避難所 ファイルの準備
     *
     * 避難所のcsvファイル と shelter_timestamp の２ファイルを利用可能とする
     * @param context
     * @return
     */
    public static boolean prepareOfflineShelterFile(Context context) {
        Log.d(TAG, "prepareOfflineShelterFile");
        return prepareOfflineFile(context, SHELTER_FILE, getOfflineShelterFile(context)) &&
                prepareOfflineFile(context, SHELTER_TIMESTAMP, getOfflineShelterTimestampFile(context));
    }

    private static boolean prepareOfflineFile(Context context, String fnInAsset, String dstFn) {
        File dst = new File(dstFn);

        // ファイルの存在確認
        if (dst.exists() && dst.isFile()) {
            // 準備完了
            Log.d(TAG, "already map file exists : " + dstFn);
            return true;
        }

        return AssetFileUtils.copyFromAsset(context, fnInAsset, dst.getAbsolutePath());
    }

    private static boolean rollbackBackupfiles(Context context) {
        try {
            FileUtil.copyFile(
                    new File(getOfflineBackupShelterFile(context)),
                    new File(getOfflineShelterFile(context)));
            FileUtil.copyFile(
                    new File(getOfflineBackupShelterTimestampFile(context)),
                    new File(getOfflineShelterTimestampFile(context)));
        } catch (IOException e) {
            Log.w(TAG, "避難所ファイルのバックアップからのロールバックに失敗しました", e);
            return false;
        }
        return true;
    }

    public static String getCurrentTimeStamp(Context context) {
        Timestamp ts = new Timestamp(context.getExternalFilesDir(null) + "/" + SHELTER_TIMESTAMP);
        return ts.getYmdString();
    }

}
