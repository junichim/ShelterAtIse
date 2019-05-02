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

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.graphhopper.GraphHopper;
import com.mori_soft.escape.entity.ShelterEntity;

import org.mapsforge.core.model.LatLong;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * 近傍の避難所検索のためのAsyncTaskLoader.
 */

public class NearestShelterAsynkTaskLoader extends AsyncTaskLoader<List<NearestShelter.ShelterPath>> {

    private static final String TAG = NearestShelterAsynkTaskLoader.class.getSimpleName();

    private Collection<ShelterEntity> mShelters;
    private LatLong mCurrent;
    private ShelterType mShelterType;
    private GraphHopper mGraphHopper;

    public NearestShelterAsynkTaskLoader(Context context, Collection<ShelterEntity> shelters, LatLong loc, ShelterType shelterType) {
        super(context);
        mShelters = shelters;
        mCurrent = loc;
        mShelterType = shelterType;
        mGraphHopper = GraphHopperWrapper.getInstance(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<NearestShelter.ShelterPath> loadInBackground() {
        Log.d(TAG, "loadInBackground");

        if (null == mGraphHopper) {
            Log.w(TAG, "no Graphhopper instance. Skip nearest shelter calculation.");
            return null;
        }

        NearestShelter ns = new NearestShelter(mGraphHopper);
        List<NearestShelter.ShelterPath> paths = ns.sortNearestShelter(mShelters, mCurrent, mShelterType);
        return paths;
    }

}
