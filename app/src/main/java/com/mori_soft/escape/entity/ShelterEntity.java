package com.mori_soft.escape.entity;

import com.mori_soft.escape.Util.Ranking;

/**
 * Created by mor on 2017/06/16.
 */

public class ShelterEntity {

    public static final int INVALID_ID = -1;

    public int recordId;
    public String address;
    public String shelterName;
    public String tel;
    public String detail;
    public boolean isShelter;
    public boolean isTsunami;
    public Ranking ranking;
    public boolean isLiving;
    public String memo;
    public double lat;
    public double lon;

    public ShelterEntity() {
        recordId = INVALID_ID;
        address = "";
        shelterName = "";
        tel = "";
        detail = "";
        isShelter = false;
        isTsunami = false;
        ranking = Ranking.INVALID;
        isLiving = false;
        memo = "";
        lat = 0.0;
        lon = 0.0;
    }
}
