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
    static final String CLM_LAT = "lat";
    static final String CLM_LON = "lon";
    static final String CLM_CR_DATE = "created_date";
    static final String CLM_UP_DATE = "updated_date";

    private ShelterDbConst() {}

}
