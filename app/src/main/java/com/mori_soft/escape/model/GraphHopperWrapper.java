/*
 * Copyright 2017 Junichi MORI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mori_soft.escape.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.graphhopper.GraphHopper;
import com.mori_soft.escape.Util.AssetFileUtils;
import com.mori_soft.escape.Util.FileUtil;
import com.mori_soft.escape.entity.ShelterEntity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class GraphHopperWrapper {

    private static final String TAG = GraphHopperWrapper.class.getSimpleName();
    private static final String GHZ_COMPRESSED_FILE = "ise"; // real filename is ise.ghz
    private static final String SUFX_GHZ = ".ghz";

    private static GraphHopper mGraphHopper;

    public static GraphHopper getInstance(Context context) {
        if (mGraphHopper == null) {
            mGraphHopper = prepareGraphHopper(context);
        }
        return mGraphHopper;
    }

    /**
     * Graphhopper の準備
     *
     * 経路情報ファイルを読み込む。
     * ここでは、 ghz ファイルを指定することを想定する。
     * graphhopper#load 内部にて、unzip処理が行われる。
     *
     * @param context
     * @return
     */
    private static GraphHopper prepareGraphHopper(Context context) {
        Log.d(TAG, "prepareGraphHopper");
        try {
            GraphHopper tmp = new GraphHopper().forMobile();
            tmp.load(getGraphHopperFolder(context));
            return tmp;
        } catch (Exception e) {
            Log.e(TAG, "Graphhopper file load failed" , e);
            // 読み込みに失敗した場合は、ファイルを消去する
            clearGhzFiles(context);
            return null;
        }
    }

    private static String getGraphHopperFolder(Context context) {
        // 外部ストレージ領域にあるパッケージ用のフォルダ（アンインストールで消える）を利用する
        // 例 /sdcard/Android/data/パッケージ名/files になる
        return new File(context.getExternalFilesDir(null),  "/" + GHZ_COMPRESSED_FILE).getAbsolutePath();
    }

    public static boolean prepareGraphHopperFile(Context context) {
        final String ghz_folder = getGraphHopperFolder(context);
        File gh = new File(ghz_folder);
        File ghz = new File(ghz_folder + SUFX_GHZ);

        // フォルダ名 or フォルダ名.ghz の存在確認
        if (gh.exists() && gh.isDirectory() || ghz.exists() && ghz.isFile()) {
            // 準備完了
            return true;
        }

        return AssetFileUtils.copyFromAsset(context, GHZ_COMPRESSED_FILE + SUFX_GHZ, ghz.getAbsolutePath());
    }

    private static void clearGhzFiles(Context context) {
        final String ghz_folder = getGraphHopperFolder(context);
        File gh = new File(ghz_folder);
        File ghz = new File(ghz_folder + SUFX_GHZ);

        FileUtil.forceDelete(gh);
        FileUtil.forceDelete(ghz);
    }

}
