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
package com.mori_soft.escape.map;

/**
 * Created by mor on 2017/07/03.
 */
final class LayerId {
    static final int INVALID = -1;

    private final LayerType mType;
    private final int mNum; // SelectedShelter, NearShelter の場合に recordId を与える

    public LayerId(LayerType type) {
        mType = type;
        mNum = INVALID;
    }

    public LayerId(LayerType type, int num) {
        mType = type;
        mNum = num;
    }

    public LayerType getMarkerType() {
        return mType;
    }

    public int getNum() {
        return mNum;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LayerId) {
            LayerId mid = (LayerId) obj;
            return mType == mid.mType &&
                    mNum == mid.mNum;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + mType.hashCode();
        result = 31 * result + mNum;
        return result;
    }
}
