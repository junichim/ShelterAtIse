package com.mori_soft.escape.download;

import android.content.Context;
import android.util.Log;

import androidx.loader.content.AsyncTaskLoader;

import com.mori_soft.escape.Util.FileUtil;
import com.mori_soft.escape.map.MapViewSetupper;
import com.mori_soft.escape.model.GraphHopperWrapper;
import com.mori_soft.escape.model.ShelterUpdater;

import java.io.File;
import java.io.IOException;

/**
 * 避難所ファイルをダウンロードする
 *
 * 避難所ファイルは、ひとつ前のファイルをバックアップファイルとして保存し、
 * 更新失敗時に、ロールバック可能としておく
 */
public class OfflineShelterDownLoaderAsyncTaskLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OfflineShelterDownLoaderAsyncTaskLoader.class.getSimpleName();

    public OfflineShelterDownLoaderAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Boolean loadInBackground() {
        Log.d(TAG, "loadInBackground");

        if (false == downloadShelterFile()) {
            Log.w(TAG, "避難所ファイルの更新準備に失敗しました。更新処理を中止します。");

            // ダウンロード済みファイルの削除
            deleteDownloadedFiles();
            return false;
        }

        // 避難所ファイルの更新
        try {
            final File shelter = new File(this.getContext().getExternalFilesDir(null) + "/" + ShelterUpdater.SHELTER_FILE);
            final String tsFn = ShelterUpdater.SHELTER_TIMESTAMP;
            final File ts = new File(this.getContext().getExternalFilesDir(null) + "/" + tsFn);

            // 既存ファイルの削除
            FileUtil.forceDelete(shelter);
            FileUtil.forceDelete(ts);

            // 避難所ファイルを移動
            final String folder = DownLoader.getDownloadFolder(this.getContext());

            FileUtil.psudoMoveFile(new File(folder + "/" + ShelterUpdater.SHELTER_FILE), shelter);
            FileUtil.psudoMoveFile(new File(folder + "/" + tsFn), ts);
        } catch (IOException e) {
            Log.w(TAG, "避難所ファイルの更新に失敗しました。更新処理を中止します。", e);
            return false;
        } finally {
            deleteDownloadedFiles();
        }
        return true;
    }

    private boolean downloadShelterFile() {
        DownLoader dl = new DownLoader(this.getContext());

        // 避難所 ファイルのダウンロード
        if (! dl.downloadFromNetwork(ShelterUpdater.SHELTER_FILE)) {
            Log.e(TAG, "ダウンロードに失敗しました: " + ShelterUpdater.SHELTER_FILE);
            return false;
        }
        return true;
    }

    private void deleteDownloadedFiles() {
        final String folder = DownLoader.getDownloadFolder(this.getContext());
        final File map = new File(folder + "/" + ShelterUpdater.SHELTER_FILE);

        FileUtil.forceDelete(map);
    }

}
