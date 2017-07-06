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
package com.mori_soft.escape.model;

import android.text.TextUtils;

/**
 * 安全度ランク.
 */

public enum Ranking {

    INVALID(0),
    PARTIAL_UNSAFE(1),
    UNCONFIRMED_SAFE(2),
    STANDARD_SAFE(3),
    ENOUGH_SAFE(4);

    private static final String RANK_TRIANGLE   = "▲";
    private static final String RANK_ONE_STAR   = "☆";
    private static final String RANK_TWO_STAR   = "☆☆";
    private static final String RANK_THREE_STAR = "☆☆☆";

    private int mRanking;

    private Ranking(int ranking) {
        mRanking = ranking;
    }

    public static Ranking convertRanking(String star) {
        if (TextUtils.isEmpty(star)){
            return INVALID;
        }
        switch(star) {
            case RANK_TRIANGLE:
                return PARTIAL_UNSAFE;
            case RANK_ONE_STAR:
                return UNCONFIRMED_SAFE;
            case RANK_TWO_STAR:
                return STANDARD_SAFE;
            case RANK_THREE_STAR:
                return ENOUGH_SAFE;
            default:
                return INVALID;
        }
    }
    public static Ranking convertRanking(int ranking) {
        switch(ranking) {
            case 1:
                return PARTIAL_UNSAFE;
            case 2:
                return UNCONFIRMED_SAFE;
            case 3:
                return STANDARD_SAFE;
            case 4:
                return ENOUGH_SAFE;
            default:
                return INVALID;
        }
    }
    public int getRankingValue() {
        return mRanking;
    }
    public String getRankingStar() {
        switch(mRanking) {
            case 1:
                return RANK_TRIANGLE;
            case 2:
                return RANK_ONE_STAR;
            case 3:
                return RANK_TWO_STAR;
            case 4:
                return RANK_THREE_STAR;
            default:
                return "";
        }
    }

}
