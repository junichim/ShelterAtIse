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
package com.mori_soft.escape.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 避難所情報のコンテンツプロバイダのコントラクトクラス.
 */

public final class ShelterContract implements BaseColumns {

    private static final String PROVIDER = ShelterContentProvider.class.getSimpleName();
    public static final String AUTHORITY = "com.mori_soft.escape.provider." + PROVIDER;

    private ShelterContract() {};

    public static final class Shelter {

        public static final String PATH = "shelter";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

        // MIME タイプ
        private static final String MIME_SUB_TYPE = "vnd." + AUTHORITY + "." + PATH;
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_SUB_TYPE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_SUB_TYPE;

        // カーソル用列名
        public static final String ADDRESS = ShelterDbConst.CLM_ADRS;
        public static final String NAME     = ShelterDbConst.CLM_NAME;
        public static final String TEL      = ShelterDbConst.CLM_TEL;
        public static final String DETAIL   = ShelterDbConst.CLM_DETAIL;
        public static final String IS_SHELTER = ShelterDbConst.CLM_IS_SHELTER;
        public static final String IS_TSUNAMI = ShelterDbConst.CLM_IS_TSUNAMI;
        public static final String RANK        = ShelterDbConst.CLM_RANK;
        public static final String IS_LIVING  = ShelterDbConst.CLM_IS_LIVING;
        public static final String MEMO        = ShelterDbConst.CLM_MEMO;
        public static final String LAT = ShelterDbConst.CLM_LAT;
        public static final String LON = ShelterDbConst.CLM_LON;

        private Shelter() {};

    }

    public static final int DB_INT_TRUE = 1;
    public static final int DB_INT_FALSE = 0;

}
