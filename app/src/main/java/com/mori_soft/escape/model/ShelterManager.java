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

import com.mori_soft.escape.entity.ShelterEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mor on 2017/07/06.
 */

public class ShelterManager {

    private Map<Integer, ShelterEntity> mShelters;

    public ShelterManager() {
        mShelters = null;
    }

    public void setShelters(List<ShelterEntity> shelters) {
        mShelters = new HashMap<Integer, ShelterEntity>();
        for (ShelterEntity ent : shelters) {
            mShelters.put(ent.recordId, ent);
        }
    }

    public int size() {
        return mShelters != null ? mShelters.size() : 0;
    }
    public Set<Integer> getKeys() {
        return mShelters != null ? mShelters.keySet() : null;
    }
    public ShelterEntity get(int key) {
        if (mShelters != null && mShelters.containsKey(key)) {
            return mShelters.get(key);
        }
        return null;
    }
    public Collection<ShelterEntity> getValues() {
        return mShelters != null ? mShelters.values() : null;
    }

}
