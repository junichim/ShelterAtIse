package com.mori_soft.escape.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionUtil {

    /**
     * WiFi でネットワークが接続されているか否か
     *
     * @param context
     * @return
     */
    public static boolean isWiFiConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info != null && info.isConnected()) {
            return info.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

}
