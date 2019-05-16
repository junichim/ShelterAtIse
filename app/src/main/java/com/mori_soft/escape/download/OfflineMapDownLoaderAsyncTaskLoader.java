package com.mori_soft.escape.download;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.mori_soft.escape.Util.FileUtil;
import com.mori_soft.escape.map.MapViewSetupper;
import com.mori_soft.escape.model.GraphHopperWrapper;

import java.io.File;
import java.io.IOException;

/**
 * オフラインマップをダウンロードする
 */
public class OfflineMapDownLoaderAsyncTaskLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = OfflineMapDownLoaderAsyncTaskLoader.class.getSimpleName();

    private static final String GHZ_FILE_CHECK_BASE = "ise_check";
    private static final String GHZ_FILE_CHECK = GHZ_FILE_CHECK_BASE + GraphHopperWrapper.SUFX_GHZ;

    public OfflineMapDownLoaderAsyncTaskLoader(Context context) {
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

        if (false == downloadAndCheckOffilneMap()) {
            Log.w(TAG, "オフラインマップの更新準備に失敗しました。更新処理を中止します。");
            return false;
        }

        // オフラインマップの更新
        try {
            final File map = new File(this.getContext().getExternalFilesDir(null) + "/" + MapViewSetupper.MAP_FILE);
            final File ghz = new File(this.getContext().getExternalFilesDir(null) + "/" + GraphHopperWrapper.GHZ_FILE);
            final String tsFn = MapViewSetupper.MAP_TIMESTAMP;
            final File ts = new File(this.getContext().getExternalFilesDir(null) + "/" + tsFn);

            // 既存ファイルの削除
            FileUtil.forceDelete(map);
            FileUtil.forceDelete(ghz);
            FileUtil.forceDelete(ts);
            FileUtil.forceDelete(new File(this.getContext().getExternalFilesDir(null) + "/" + GraphHopperWrapper.GHZ_FILE_BASE));

            // オフラインマップファイルをコピー
            final String folder = DownLoader.getDownloadFolder(this.getContext());

            FileUtil.copyFile(new File(folder + "/" + MapViewSetupper.MAP_FILE), map);
            FileUtil.copyFile(new File(folder + "/" + GraphHopperWrapper.GHZ_FILE), ghz);
            FileUtil.copyFile(new File(folder + "/" + tsFn), ts);
        } catch (IOException e) {
            Log.w(TAG, "オフラインマップの更新に失敗しました。更新処理を中止します。", e);
            return false;
        }
        return true;
    }

    private boolean downloadAndCheckOffilneMap() {

        // ファイルのダウンロード
        downloadOfflineMap();

        // ghz ファイルのコピー
        final String folder = DownLoader.getDownloadFolder(this.getContext());
        final File src = new File(folder + "/" + GraphHopperWrapper.GHZ_FILE);
        final File dst = new File(folder + "/" + GHZ_FILE_CHECK);

        try {
            FileUtil.copyFile(src, dst);
        } catch (IOException e) {
            Log.e(TAG, "チェック用 ghz ファイルコピーに失敗しました", e);
            return false;
        }

        // ghzファイルのチェック
        final boolean result = GraphHopperWrapper.isValidGhzFile(folder + "/" + GHZ_FILE_CHECK_BASE);
        return result;
    }

    private boolean downloadOfflineMap() {
        DownLoader dl = new DownLoader(this.getContext());

        // map ファイルのダウンロード
        if (! dl.downloadFromNetwork(MapViewSetupper.MAP_FILE)) {
            return false;
        }
        // ghz ファイルのダウンロード
        if (! dl.downloadFromNetwork(GraphHopperWrapper.GHZ_FILE)) {
            return false;
        }
        return true;
    }

}
