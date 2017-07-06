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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mor on 2017/06/27.
 */

public class ShelterCsvReader {

    private static final String TAG = ShelterCsvReader.class.getSimpleName();

    public static List<ShelterEntity> parse(Context context, String filename) {
        List<ShelterEntity> list = new ArrayList<ShelterEntity>();

        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open(filename);
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
                i += 4;
                ent.memo        = splt[i++];
                ent.lat         = Double.valueOf(splt[i++]);
                ent.lon         = Double.valueOf(splt[i]);

                list.add(ent);
            }

            br.close();
        } catch (IOException e) {
            Log.e(TAG, "asset内のCSVファイル読み込みに失敗しました: " + filename, e);
            list.clear();
        }

        return list;
    }

}
