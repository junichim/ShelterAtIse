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
package com.mori_soft.escape;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.mori_soft.escape.Util.PermissionUtil;
import com.mori_soft.escape.dialog.AboutDialogFragment;
import com.mori_soft.escape.dialog.LegendDialogFragment;
import com.mori_soft.escape.dialog.UsageDialogFragment;
import com.mori_soft.escape.map.MapViewSetupper;
import com.mori_soft.escape.model.GraphHopperWrapper;
import com.mori_soft.escape.model.Ranking;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.map.LayerManager;
import com.mori_soft.escape.model.NearestShelter;
import com.mori_soft.escape.model.NearestShelterAsynkTaskLoader;
import com.mori_soft.escape.model.ShelterType;
import com.mori_soft.escape.provider.ShelterContract;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mor on 2017/06/27.
 */

public class MapFragment extends Fragment {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final String FRAGMENT_TAG_DIALOG_LEGEND = "LEGEND";
    private static final String FRAGMENT_TAG_DIALOG_USAGE = "USAGE";
    private static final String FRAGMENT_TAG_DIALOG_ABOUT = "ABOUT";

    private static final int SHELTER_LOADER_ID = 1;
    private static final int NEAREST_LOADER_ID = 2;

    private MapView mMapView;
    private LayerManager mLayerManager;
    private boolean wasSearched;

    private ShelterType mSearchTargetShelterType = ShelterType.INVALID;

    private NearestShelterLoaderCallbacks mNearestLoaderCallbacks = new NearestShelterLoaderCallbacks();
    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        this.setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AndroidGraphicFactory.createInstance(this.getActivity().getApplication());
        wasSearched = false;

        // 避難所種別スピナーの追加
        setupSpinner();
    }

    private void setupSpinner() {
        // ツールバーのカスタムビューとして追加
        ActionBar ab = ((AppCompatActivity)this.getActivity()).getSupportActionBar();
        ab.setDisplayShowCustomEnabled(true);
        ab.setCustomView(R.layout.action_spinner);

        Spinner spinner = (Spinner) ab.getCustomView().findViewById(R.id.action_spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.shelter_type, R.layout.spinner_dropdown);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position + ", id: " + id);
                switch (position) {
                    case 0:
                        mSearchTargetShelterType = ShelterType.TSUNAMI;
                        break;
                    case 1:
                        mSearchTargetShelterType = ShelterType.DESIGNATION;
                        break;
                    default:
                        mSearchTargetShelterType = ShelterType.INVALID;
                }

                // 地図データにアクセスできる時だけ画面表示を更新
                if (PermissionUtil.checkPermissionGranted(MapFragment.this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // 画面表示を更新
                    mLayerManager.updateShelterMarker(mSearchTargetShelterType);
                    // 現在位置を取得したら更新
                    if (mLayerManager.getCurrentLocation() != null) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        wasSearched = false;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected");
            }
        });
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        // 地図データにアクセスできる時だけ表示
        if (PermissionUtil.checkPermissionGranted(this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            prepareMapFiles();
            onGrantedMapDraw();
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        mMapView.destroyAll();;
        AndroidGraphicFactory.clearResourceMemoryCache();

        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_legend:
                DialogFragment dialog = new LegendDialogFragment();
                showDialog(dialog, FRAGMENT_TAG_DIALOG_LEGEND);
                return true;
            case R.id.action_usage:
                dialog = new UsageDialogFragment();
                showDialog(dialog, FRAGMENT_TAG_DIALOG_USAGE);
                return true;
            case R.id.action_about:
                dialog = new AboutDialogFragment();
                showDialog(dialog, FRAGMENT_TAG_DIALOG_ABOUT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void prepareMapFiles() {
        // オフラインマップ
        boolean res = MapViewSetupper.prepareOfflineMapFile(this.getActivity());
        if (!res) {
            Log.w(TAG, "オフラインマップ ファイルの準備に失敗しました");
        }

        // GraphHopper ファイル
        res = GraphHopperWrapper.prepareGraphHopperFile(this.getActivity());
        if (!res) {
            Log.w(TAG, "GraphHopper ファイルの準備に失敗しました");
        }
    }

    private void onGrantedMapDraw() {
        Log.d(TAG, "onGrantedMapDraw");
        mLayerManager = new LayerManager(this.getActivity(), mMapView);

        // マップ
        MapViewSetupper.setupMapView(this.getActivity(), mMapView);

        // 避難所表示
        this.getLoaderManager().initLoader(SHELTER_LOADER_ID, null, new ShelterLoaderCallbacks());
    }

    public void updateLastLocation(LatLong loc) {
        mLayerManager.updateCurrentMarker(loc);
        mMapView.setCenter(loc);
    }
    public void updateCurrentLocation(LatLong loc) {
        Log.d(TAG, "updateCurrentLocation");
        mLayerManager.updateCurrentMarker(loc);

        // 近傍の避難所探索
        if (!wasSearched) {
            searchNearShelter();
        }
    }

    private void searchNearShelter() {
        Log.d(TAG, "searchNearShelter");
          if (mLayerManager.getShelterManager() != null) {
            wasSearched = true;
            getLoaderManager().restartLoader(NEAREST_LOADER_ID, null, mNearestLoaderCallbacks);
        }
    }

    private void showDialog(DialogFragment f, String tag) {
        f.show(this.getFragmentManager(), tag);
    }


    private class ShelterLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        private final String TAG = ShelterLoaderCallbacks.class.getSimpleName();
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");

            String where_clause;
            switch (mSearchTargetShelterType) {
                case TSUNAMI:
                    where_clause = ShelterContract.Shelter.IS_TSUNAMI + " = 1 ";
                    break;
                case DESIGNATION:
                    where_clause = ShelterContract.Shelter.IS_SHELTER + " = 1 ";
                    break;
                default:
                    where_clause = null;
                    break;
            }
            return new CursorLoader(MapFragment.this.getActivity(), ShelterContract.Shelter.CONTENT_URI,
                    new String[]{
                            ShelterContract._ID,
                            ShelterContract.Shelter.ADDRESS,
                            ShelterContract.Shelter.NAME,
                            ShelterContract.Shelter.TEL,
                            ShelterContract.Shelter.DETAIL,
                            ShelterContract.Shelter.IS_SHELTER,
                            ShelterContract.Shelter.IS_TSUNAMI,
                            ShelterContract.Shelter.RANK,
                            ShelterContract.Shelter.IS_LIVING,
                            ShelterContract.Shelter.MEMO,
                            ShelterContract.Shelter.LAT,
                            ShelterContract.Shelter.LON,
                    },
                    where_clause, // where clause
                    null,        // where arguments
                    null         // order by
                    );
        }
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.d(TAG, "onLoadFinished");

            if (data == null || data.getCount() == 0) {
                return;
            }

            // 詰め替え
            List<ShelterEntity> shelters = new ArrayList<ShelterEntity>();

            data.moveToFirst();
            do {
                ShelterEntity ent = new ShelterEntity();
                ent.recordId    = data.getInt(data.getColumnIndex(ShelterContract._ID));
                ent.address      = data.getString(data.getColumnIndex(ShelterContract.Shelter.ADDRESS));
                ent.shelterName = data.getString(data.getColumnIndex(ShelterContract.Shelter.NAME));
                ent.tel          = data.getString(data.getColumnIndex(ShelterContract.Shelter.TEL));
                ent.detail       = data.getString(data.getColumnIndex(ShelterContract.Shelter.DETAIL));
                ent.isShelter   = data.getInt(data.getColumnIndex(ShelterContract.Shelter.IS_SHELTER)) == ShelterContract.DB_INT_TRUE;
                ent.isTsunami   = data.getInt(data.getColumnIndex(ShelterContract.Shelter.IS_TSUNAMI)) == ShelterContract.DB_INT_TRUE;
                ent.ranking     = Ranking.convertRanking(data.getInt(data.getColumnIndex(ShelterContract.Shelter.RANK)));
                ent.isLiving    = data.getInt(data.getColumnIndex(ShelterContract.Shelter.IS_LIVING)) == ShelterContract.DB_INT_TRUE;
                ent.memo         = data.getString(data.getColumnIndex(ShelterContract.Shelter.MEMO));
                ent.lat          = data.getDouble(data.getColumnIndex(ShelterContract.Shelter.LAT));
                ent.lon          = data.getDouble(data.getColumnIndex(ShelterContract.Shelter.LON));

                shelters.add(ent);
            } while (data.moveToNext());

            mLayerManager.setShelters(shelters);
            mLayerManager.updateShelterMarker(mSearchTargetShelterType);
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }


    private class NearestShelterLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<NearestShelter.ShelterPath>> {
        private final String TAG = NearestShelterLoaderCallbacks.class.getSimpleName();
        @Override
        public Loader<List<NearestShelter.ShelterPath>> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");

            mProgressBar.setVisibility(View.VISIBLE);

            return new NearestShelterAsynkTaskLoader(MapFragment.this.getActivity(),
                    (mLayerManager.getShelterManager() != null ? mLayerManager.getShelterManager().getValues() : null),
                    mLayerManager.getCurrentLocation(), mSearchTargetShelterType);
        }
        @Override
        public void onLoadFinished(Loader<List<NearestShelter.ShelterPath>> loader, List<NearestShelter.ShelterPath> data) {
            Log.d(TAG, "onLoadFinished");
            Log.d(TAG, "data size:" + (data == null ? "null" : data.size()));

            mProgressBar.setVisibility(View.INVISIBLE);

            mLayerManager.updateShelterMarker(mSearchTargetShelterType);
            mLayerManager.updateNearShelterMarker(data, mSearchTargetShelterType);
            mLayerManager.updateNearestShelterPath();
        }
        @Override
        public void onLoaderReset(Loader<List<NearestShelter.ShelterPath>> loader) {

        }
    }

}
