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
import android.util.Log;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.mori_soft.escape.Util.AssetFileUtils;
import com.mori_soft.escape.Util.FileUtil;

import java.io.File;

public class GraphHopperWrapper {

    private static final String TAG = GraphHopperWrapper.class.getSimpleName();

    public static final String GHZ_FILE_BASE = "ise"; // real filename is ise.ghz
    public static final String SUFX_GHZ = ".ghz";
    public static final String GHZ_FILE = GHZ_FILE_BASE + SUFX_GHZ;

    // graphhopper ファイルを作成した際に config.yaml で指定した情報
    //   ファイルから読み込んだだけでは設定されない
    public static final String GH_PROFILE = "escape_by_foot";
    private static final String GH_PROFILE_VEHICLE = "foot";
    private static final String GH_PROFILE_WEIGHTING = "fastest";

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
            tmp.setProfiles(new Profile(GH_PROFILE).setVehicle(GH_PROFILE_VEHICLE).setWeighting(GH_PROFILE_WEIGHTING));
            tmp.getCHPreparationHandler().setCHProfiles(new CHProfile(GH_PROFILE));
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
        return new File(context.getExternalFilesDir(null),  "/" + GHZ_FILE_BASE).getAbsolutePath();
    }

    public static boolean prepareGraphHopperFile(Context context) {
        final String ghz_folder = getGraphHopperFolder(context);
        File gh = new File(ghz_folder);
        File ghz = new File(ghz_folder + SUFX_GHZ);

        // フォルダ名 or フォルダ名.ghz の存在確認
        if (gh.exists() && gh.isDirectory() || ghz.exists() && ghz.isFile()) {
            // 準備完了
            Log.d(TAG, "already ghz file exists : " + ghz);
            return true;
        }

        return AssetFileUtils.copyFromAsset(context, GHZ_FILE, ghz.getAbsolutePath());
    }

    private static void clearGhzFiles(Context context) {
        final String ghz_folder = getGraphHopperFolder(context);
        File gh = new File(ghz_folder);
        File ghz = new File(ghz_folder + SUFX_GHZ);

        FileUtil.forceDelete(gh);
        FileUtil.forceDelete(ghz);
    }

    /**
     * 引数で指定される ghzBasename ファイルがライブラリで扱えるか否かチェック
     *
     * 実際に読み込んでみて、例外がなければOKとする。
     *
     * 注意
     *   指定された ghzファイルを実際に解凍してチェックする。
     *   このため、ghzファイルのbasenameのフォルダが作成されるが、
     *   終了時には消去する。
     *   このため、もし、basenameと同名フォルダが存在していた場合は、
     *   消去されるので注意すること
     *
     * @param ghzBasename  テスト対象ファイルの絶対パス（拡張子なし）
     * @return  true: 妥当なファイル（読み込み可能）, false: 不正なファイル（読み込み不可能）
     */
    public static boolean isValidGhzFile(String ghzBasename) {
        Log.d(TAG, "isValidGhzFile. check for " + ghzBasename);
        try {
            GraphHopper tmp = new GraphHopper().forMobile();
            tmp.load(ghzBasename);
            tmp.close();
            tmp = null;
            return true;
        } catch (Exception e) {
            Log.w(TAG, "isValidGhzFile: Graphhopper file load failed" , e);
            return false;
        } finally {
            // 消去
            FileUtil.forceDelete(new File(ghzBasename));
            FileUtil.forceDelete(new File(ghzBasename + SUFX_GHZ));
        }
    }

    public static void releaseGraphHopper() {
        if (mGraphHopper != null) {
            mGraphHopper.close();
            mGraphHopper = null;
        }
    }
}
