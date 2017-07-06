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
package com.mori_soft.escape.entity;

import com.mori_soft.escape.model.Ranking;

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
