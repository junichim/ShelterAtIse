package com.mori_soft.escape;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * Created by mor on 2017/06/27.
 */

public class PermissionUtil {

    public static boolean checkPermissionGranted(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermission(Activity activity, String permission, int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // TODO 説明ダイアログの表示と応答
            // 一度パーミッション要請を拒否された場合
        }
        // 常に拒否の場合はこっち。requestPermissionも自動的に拒否
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

}
