package com.mori_soft.escape.model;

/**
 * Created by mor on 2017/07/01.
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
