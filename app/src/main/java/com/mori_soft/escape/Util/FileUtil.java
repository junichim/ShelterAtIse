package com.mori_soft.escape.Util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    /**
     * 指定された File を削除する
     *
     * File がディレクトリの場合、配下のファイル・ディレクトリもすべて
     * 削除する
     *
     * @param file  消去対象ファイル
     */
    public static void forceDelete(final File file) {

        if (! file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                forceDelete(child);
            }
        }
        file.delete();
    }

    /**
     * 指定されたフォルダを作成する
     */
    public static void createFolder(String folder) {
        File f = new File(folder);
        if (f.isFile()) {
            throw new RuntimeException("argument is file: " + folder);
        }
        if (f.exists()) {
            return;
        }
        f.mkdirs();
    }

    /**
     * ファイルをコピーする
     *
     * @param src  コピー元ファイル
     * @param dst  コピー先ファイル
     * @throws IOException
     */
    public static void copyFile(final File src, final File dst) throws IOException {

        FileChannel fcIn = null;
        FileChannel fcOut = null;
        try {
            fcIn = new FileInputStream(src).getChannel();
            fcOut = new FileOutputStream(dst).getChannel();

            fcIn.transferTo(0, fcIn.size(), fcOut);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "ファイルが見つかりません : " + src + ", " + dst, e);
            throw e;
        } catch (IOException e) {
            Log.e(TAG, "ファイルコピーでエラーが発生しました。 from : " + src + " to : " + dst, e);
            throw e;
        } finally {
            if (fcIn != null) {
                try {
                    fcIn.close();
                } catch (IOException e) {
                    Log.e(TAG, "ファイルクローズに失敗しました: " + src, e);
                    throw e;
                }
            }
            if (fcOut != null) {
                try {
                    fcOut.close();
                } catch (IOException e) {
                    Log.e(TAG, "ファイルクローズに失敗しました: " + dst, e);
                    throw e;
                }
            }
        }
    }

    /**
     * ファイルを疑似的に移動する
     *
     * コピー後、コピー元ファイルを削除することで移動とする
     *
     * @param src  移動元ファイル
     * @param dst  移動先ファイル
     * @throws IOException
     */
    public static void psudoMoveFile(final File src, final File dst) throws IOException {
        copyFile(src, dst);
        forceDelete(src);
    }

}
