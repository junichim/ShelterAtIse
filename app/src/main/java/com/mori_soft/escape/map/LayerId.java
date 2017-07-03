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
