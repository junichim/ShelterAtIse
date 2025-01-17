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

/**
 * 伊勢市の定義による避難所種類.
 *
 * @see <a href="http://www.city.ise.mie.jp/8280.htm">避難所の考え方と指定の避難所</a>
 */

public enum ShelterType {

    INVALID(-1),
    TSUNAMI(0),
    DESIGNATION(1);

    private int mIdx;

    private ShelterType(int index) {
        mIdx = index;
    }
    public static ShelterType getShelterType(int index) {
        switch(index) {
            case 0:
                return TSUNAMI;
            case 1:
                return DESIGNATION;
            default:
                return INVALID;
        }
    }

}
