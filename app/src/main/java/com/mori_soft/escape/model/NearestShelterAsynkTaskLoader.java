package com.mori_soft.escape.model;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.graphhopper.GraphHopper;
import com.mori_soft.escape.entity.ShelterEntity;

import org.mapsforge.core.model.LatLong;

import java.io.File;
import java.util.List;

/**
 * Created by mor on 2017/06/29.
 */

public class NearestShelterAsynkTaskLoader extends AsyncTaskLoader<List<NearestShelter.ShelterPath>> {

    private static final String TAG = NearestShelterAsynkTaskLoader.class.getSimpleName();

    private List<ShelterEntity> mShelters;
    private LatLong mCurrent;
    private GraphHopper mGraphHopper;

    public NearestShelterAsynkTaskLoader(Context context, List<ShelterEntity> shelters, LatLong loc) {
        super(context);
        mShelters = shelters;
        mCurrent = loc;
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

        if (mCurrent == null) {
            return null;
        }
        NearestShelter ns = new NearestShelter(mGraphHopper);
        List<NearestShelter.ShelterPath> paths = ns.sortNearestShelter(mShelters, mCurrent);
        return paths;
    }

}
