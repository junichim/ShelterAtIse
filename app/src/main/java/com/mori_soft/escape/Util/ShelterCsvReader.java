package com.mori_soft.escape.Util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.mori_soft.escape.entity.ShelterEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
                ent.ranking     = getRanking(splt[i++]);
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

    private static int getRanking(String ranking) {
        if (TextUtils.isEmpty(ranking)){
            return 0;
        }
        switch(ranking) {
            case "▲":
                return 1;
            case "☆":
                return 2;
            case "☆☆":
                return 3;
            case "☆☆☆":
                return 4;
            default:
                return 0;
        }
    }

}
