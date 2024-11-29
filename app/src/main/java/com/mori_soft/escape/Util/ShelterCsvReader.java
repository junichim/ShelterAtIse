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

import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.model.Ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV形式避難所データ読み込みクラス.
 *
 * 下記を想定
 *   文字コード UTF-8
 *   改行文字 LF
 */

public class ShelterCsvReader {

    private static final String TAG = ShelterCsvReader.class.getSimpleName();

    private static final int NUM_OF_SHITEI_KINKYUU_HINANSYO_DETAIL = 5;

    public static List<ShelterEntity> parseFromAsset(Context context, String filename) {

        AssetManager am = context.getAssets();
        InputStream is = null;
        try {
            is = am.open(filename);
        } catch (IOException e) {
            Log.e(TAG, "asset内のCSVファイル読み込みに失敗しました: " + filename, e);
        }
        return parse(context, is);
    }
    public static List<ShelterEntity> parse(Context context, String filename) {

        File file = new File(filename);
        InputStream is = null;
        try {
            is = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "CSVファイル読み込みに失敗しました: " + filename, e);
        }
        return parse(context, is);
    }

    private static List<ShelterEntity> parse(Context context, InputStream is) {
        List<ShelterEntity> list = new ArrayList<ShelterEntity>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"));

            String ln = br.readLine(); // 1行(ヘッダ行)飛ばし
            while ((ln = br.readLine()) != null) {
                String[] splt = ln.split(",", -1);
                ShelterEntity ent = new ShelterEntity();

                int i = 0;
                ent.recordId = Integer.valueOf(splt[i++]);
                ent.address  = splt[i++];
                ent.shelterName = splt[i++];
                ent.tel          = splt[i++];
                ent.detail      = splt[i++];
                ent.isShelter   = Boolean.valueOf(splt[i++]);
                ent.isTsunami   = Boolean.valueOf(splt[i++]);
                ent.ranking     = Ranking.convertRanking(splt[i++]);
                ent.isLiving    = Boolean.valueOf(splt[i++]);
                i += NUM_OF_SHITEI_KINKYUU_HINANSYO_DETAIL;
                ent.memo        = splt[i++];

                // 緯度経度が指定されないケースへ対応
                if (splt[i].length() == 0 || splt[i+1].length() == 0) {
                    // 緯度経度がない場合は避難所をエントリーしない
                    Log.w(TAG, "緯度経度がないため避難所を読み込みません。避難所名: " + ent.shelterName);
                } else {
                    try {
                        ent.lat = Double.valueOf(splt[i++]);
                        ent.lon = Double.valueOf(splt[i]);
                        list.add(ent);
                    } catch (Exception e) {
                        // 緯度経度が不正な場合は避難所をエントリーしない
                        Log.w(TAG, "緯度経度が正しい数値ではないため避難所を読み込みません。避難所名: " + ent.shelterName);
                    }
                }
            }

            br.close();
        } catch (IOException e) {
            Log.e(TAG, "CSVファイル読み込みに失敗しました", e);
            list.clear();
        }

        return list;
    }

}
