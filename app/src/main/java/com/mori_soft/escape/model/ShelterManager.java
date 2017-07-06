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
