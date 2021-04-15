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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mori_soft.escape.MainActivity;
import com.mori_soft.escape.R;
import com.mori_soft.escape.dialog.InfoDialogFragment;

/**
 * パーミッション関係のユーティリティクラス.
 */

public class PermissionUtil {

    private static final String TAG = PermissionUtil.class.getSimpleName();

    private static final String DIALOG_EXPLAIN_STORAGE = "dialog_explain_storage";
    private static final String DIALOG_EXPLAIN_LOCATION = "dialog_explain_location";

    public static boolean checkPermissionGranted(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermission(final AppCompatActivity activity, final String permission, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // 一度パーミッション要請を拒否された場合
            Log.d(TAG, "パーミッションが必要な理由の表示");

            InfoDialogFragment.onInfoDialogListener callback = new InfoDialogFragment.onInfoDialogListener() {
                @Override
                public void onOkClickListener() {
                    // 常に拒否の場合はこっち。requestPermissionも自動的に拒否
                    ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                }
            };

            // パーミッションが必要な理由を説明するダイアログを表示
            if (requestCode == MainActivity.PERMISSION_REQUEST_CODE_WRITE_STORAGE) {
                InfoDialogFragment df = InfoDialogFragment.getInstance(
                        R.string.dialog_info_need_storage_permission_title,
                        R.string.dialog_info_need_storage_permission_abstract,
                        R.string.dialog_info_need_storage_permission_detail);
                df.setClickListener(callback);
                df.show(activity.getSupportFragmentManager(), DIALOG_EXPLAIN_STORAGE);

            } else if (requestCode == MainActivity.PERMISSION_REQUEST_CODE_LOCATION) {
                InfoDialogFragment df = InfoDialogFragment.getInstance(
                        R.string.dialog_info_need_location_permission_title,
                        R.string.dialog_info_need_location_permission_abstract,
                        R.string.dialog_info_need_location_permission_detail);
                df.setClickListener(callback);
                df.show(activity.getSupportFragmentManager(), DIALOG_EXPLAIN_LOCATION);

            } else {
                Log.w(TAG, "unexpected requestCode: " + requestCode);
                throw new RuntimeException("Invalid Permission Request: unexpected permission requestcode");
            }
        } else {
            // 初回起動時 および 常に拒否の場合はこっち
            // 常に拒否の場合は requestPermission も自動的に拒否
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

}
