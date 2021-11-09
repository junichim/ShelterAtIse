package com.mori_soft.escape.download;

import android.content.Context;

import com.mori_soft.escape.map.MapViewSetupper;
import com.mori_soft.escape.model.ShelterUpdater;

/**
 * 最新版の避難所ファイルがあるか否かをチェック
 */
public class OfflineShelterCheckerAsyncTaskLoader extends OfflineTimestampCheckerAsyncTaskLoader {

    public OfflineShelterCheckerAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected String getTimestampFilename() {
        return ShelterUpdater.SHELTER_TIMESTAMP;
    }
}
