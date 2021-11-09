package com.mori_soft.escape.download;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;

import com.mori_soft.escape.map.MapViewSetupper;

/**
 * 最新版のオフラインマップがあるか否かをチェック
 */
public class OfflineMapCheckerAsyncTaskLoader extends OfflineTimestampCheckerAsyncTaskLoader {

    public OfflineMapCheckerAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected String getTimestampFilename() {
        return MapViewSetupper.MAP_TIMESTAMP;
    }
}
