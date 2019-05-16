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
package com.mori_soft.escape.Util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * アセット内ファイル関係のユーティリティクラス.
 */

public class AssetFileUtils {
    private static final String TAG = AssetFileUtils.class.getSimpleName();

    public static boolean copyFromAsset(Context context, String filename_in_asset, String output_fullname) {
        Log.d(TAG, "copyFromAsset : " + filename_in_asset);

        AssetManager am = context.getAssets();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            InputStream is = am.open(filename_in_asset);
            bis = new BufferedInputStream(is);

            File outFile = new File(output_fullname);
            OutputStream os = new FileOutputStream(outFile);
            bos = new BufferedOutputStream(os);

            copyFile(bis, bos);
        } catch (IOException e) {
            Log.e(TAG, "asset内のファイルコピーに失敗しました: from " + filename_in_asset + " to " + output_fullname, e);
            return false;
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException e) {
                Log.e(TAG, "asset内のファイルクローズに失敗しました: " + filename_in_asset, e);
                return false;
            }
            try {
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                Log.e(TAG, "asset内のファイルクローズに失敗しました: " + output_fullname, e);
                return false;
            }
        }
        return true;
    }

    private static void copyFile(BufferedInputStream bis, BufferedOutputStream bos) throws IOException {
        int bf;
        while ((bf = bis.read()) >= 0) {
            bos.write(bf);
        }
        bos.flush();
    }

}