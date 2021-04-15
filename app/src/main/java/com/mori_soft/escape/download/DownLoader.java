package com.mori_soft.escape.download;

import android.content.Context;
import android.util.Log;

import com.mori_soft.escape.Util.FileUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * サーバーから指定されたファイルをダウンロードしてファイルとして保存するクラス
 */
class DownLoader {

    private static final String TAG = DownLoader.class.getSimpleName();

    private static final String SERVER = "https://s3-ap-northeast-1.amazonaws.com/com.mori-soft.s3.public.escape/v1/";
    private static final String WORK_DIR = "download";

    private Context mContext;

    public DownLoader(Context context) {
        mContext = context;
    }

    public boolean downloadFromNetwork(String target) {
        Response response = getContentsFromNetwork(SERVER + target);
        if (response == null || ! response.isSuccessful() || response.body() == null) {
            Log.w(TAG, "ファイルのダウンロードに失敗しました：" + target);
            return false;
        }

        // ダウンロード先がなければ作っておく
        FileUtil.createFolder(getDownloadFolder(mContext));

        final String fn = getDownloadFolder(mContext) + "/" + target;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fn);
            fos.write(response.body().bytes());
        } catch (FileNotFoundException e) {
            Log.e(TAG, "exception occured: " + fn, e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "exception occured", e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "exception occured", e);
                    return false;
                }
            }
        }
        return true;
    }

    private Response getContentsFromNetwork(String url) {
        Log.d(TAG, "url: " + url);
        Request request = new Request.Builder().url(url).get().build();
        OkHttpClient client = new OkHttpClient();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            Log.e(TAG, "Failed download for " + url, e);
            return null;
        }

        Log.d(TAG, "response: " + response.code());
        return response;
    }

    static String getDownloadFolder(Context context) {
        return context.getExternalFilesDir(null) + "/" + WORK_DIR;
    }

}
