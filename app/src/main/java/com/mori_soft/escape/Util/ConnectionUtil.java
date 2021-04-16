package com.mori_soft.escape.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionUtil {

    /**
     * WiFi でネットワークが接続されているか否か
     *
     * context が null の場合は常に false として扱う
     * @param context
     * @return
     */
    public static boolean isWiFiConnected(Context context) {

        // Fragment の状態によっては null となる場合があるため
        if (context == null) {
            return false;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info != null && info.isConnected()) {
            return info.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

}
