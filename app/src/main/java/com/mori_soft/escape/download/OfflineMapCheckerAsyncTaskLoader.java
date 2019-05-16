package com.mori_soft.escape.download;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.mori_soft.escape.map.MapViewSetupper;

/**
 * 最新版のオフラインマップがあるか否かをチェック
 */
public class OfflineMapCheckerAsyncTaskLoader extends AsyncTaskLoader<Boolean> {

    private static final String TS_FILE = MapViewSetupper.MAP_TIMESTAMP;

    public OfflineMapCheckerAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Boolean loadInBackground() {
        // オフライン地図の最新版があるかいなかチェック

        DownLoader dl = new DownLoader(this.getContext());
        if (! dl.downloadFromNetwork(TS_FILE)) {
            return false;
        }

        // 新旧の TS_FILE のタイムスタンプを比較
        Timestamp tsOld = new Timestamp(this.getContext().getExternalFilesDir(null) + "/" + TS_FILE);
        Timestamp tsNew = new Timestamp(DownLoader.getDownloadFolder(this.getContext()) + "/" + TS_FILE);

        return tsNew.after(tsOld);
    }

}
