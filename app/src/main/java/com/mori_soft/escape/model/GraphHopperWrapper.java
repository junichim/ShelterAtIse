package com.mori_soft.escape.model;

import android.content.Context;
import android.util.Log;

import com.graphhopper.GraphHopper;

import java.io.File;

/**
 * Created by mor on 2017/06/29.
 */

public class GraphHopperWrapper {

    private static final String TAG = GraphHopperWrapper.class.getSimpleName();
    private static final String GHZ_COMPRESSED_FILE = "kansai"; // real filename is kansai.ghz

    private static GraphHopper mGraphHopper;

    public static GraphHopper getInstance(Context context) {
        if (mGraphHopper == null) {
            mGraphHopper = prepareGraphHopper(context);
        }
        return mGraphHopper;
    }

    private static GraphHopper prepareGraphHopper(Context context) {
        Log.d(TAG, "prepareGraphHopper");
        try {
            GraphHopper tmp = new GraphHopper().forMobile();
            tmp.load(getGraphHopperFolder(context));
            return tmp;
        } catch (Exception e) {
            Log.e(TAG, "Graphhopper file load failed" , e);
            return null;
        }
    }

    private static String getGraphHopperFolder(Context context) {
        // TODO 暫定
//        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/com_mori_soft_escape/gh").getAbsolutePath();
        return new File(context.getExternalFilesDir(null),  "/" + GHZ_COMPRESSED_FILE).getAbsolutePath();
    }


}
