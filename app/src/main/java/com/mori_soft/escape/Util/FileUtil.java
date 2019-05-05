package com.mori_soft.escape.Util;

import java.io.File;

public class FileUtil {

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
}
