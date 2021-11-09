package com.mori_soft.escape.download;

import android.content.Context;

import androidx.loader.content.AsyncTaskLoader;

import com.mori_soft.escape.map.MapViewSetupper;

/**
 * 最新版のファイルがあるか否かをチェック
 * タイムスタンプファイルの日時を比較することでチェックする
 */
abstract public class OfflineTimestampCheckerAsyncTaskLoader extends AsyncTaskLoader<Boolean> {

    public OfflineTimestampCheckerAsyncTaskLoader(Context context) {
        super(context);
    }

    abstract protected String getTimestampFilename();

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Boolean loadInBackground() {
        // タイムスタンプファイルの最新版があるかいなかチェック

        DownLoader dl = new DownLoader(this.getContext());
        if (! dl.downloadFromNetwork(getTimestampFilename())) {
            return false;
        }

        // 新旧の TS_FILE のタイムスタンプを比較
        Timestamp tsOld = new Timestamp(this.getContext().getExternalFilesDir(null) + "/" + getTimestampFilename());
        Timestamp tsNew = new Timestamp(DownLoader.getDownloadFolder(this.getContext()) + "/" + getTimestampFilename());

        return tsNew.after(tsOld);
    }

}
