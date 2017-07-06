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

import android.provider.BaseColumns;

/**
 * 伊勢市避難所テーブル定義用定数.
 */

class ShelterDbConst implements BaseColumns {

    // テーブルおよびカラム名
    static final String TBL_NAME = "shelter_at_ise";

    static final String CLM_ADRS = "address";
    static final String CLM_NAME = "name";
    static final String CLM_TEL  = "tel";
    static final String CLM_DETAIL = "detail";
    static final String CLM_IS_SHELTER = "is_shelter";
    static final String CLM_IS_TSUNAMI = "is_tsunami";
    static final String CLM_RANK = "safty_ranking";
    static final String CLM_IS_LIVING = "is_living";
    static final String CLM_MEMO = "memo";
    static final String CLM_LAT = "lat";
    static final String CLM_LON = "lon";
    static final String CLM_CR_DATE = "created_date";
    static final String CLM_UP_DATE = "updated_date";

    private ShelterDbConst() {}

}
