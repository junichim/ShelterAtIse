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
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Toast;

import com.mori_soft.escape.Util.ConnectionUtil;
import com.mori_soft.escape.Util.LocationUtil;
import com.mori_soft.escape.Util.PermissionUtil;
import com.mori_soft.escape.dialog.AboutDialogFragment;
import com.mori_soft.escape.dialog.LegendDialogFragment;
import com.mori_soft.escape.dialog.MapUpdateConfirmationDialogFragment;
import com.mori_soft.escape.dialog.ShelterUpdateConfirmationDialogFragment;
import com.mori_soft.escape.dialog.UpdateConfirmationDialogFragment;
import com.mori_soft.escape.dialog.UsageDialogFragment;
import com.mori_soft.escape.download.OfflineMapCheckerAsyncTaskLoader;
import com.mori_soft.escape.download.OfflineMapDownLoaderAsyncTaskLoader;
import com.mori_soft.escape.download.OfflineShelterCheckerAsyncTaskLoader;
import com.mori_soft.escape.download.OfflineShelterDownLoaderAsyncTaskLoader;
import com.mori_soft.escape.map.MapViewSetupper;
import com.mori_soft.escape.model.GraphHopperWrapper;
import com.mori_soft.escape.model.Ranking;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.map.LayerManager;
import com.mori_soft.escape.model.NearestShelter;
import com.mori_soft.escape.model.NearestShelterAsynkTaskLoader;
import com.mori_soft.escape.model.ShelterType;
import com.mori_soft.escape.model.ShelterUpdater;
import com.mori_soft.escape.provider.ShelterContract;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * 地図表示Fragment.
 */

public class MapFragment extends Fragment implements
        MapUpdateConfirmationDialogFragment.onUpdateListener,
        ShelterUpdateConfirmationDialogFragment.onUpdateListener {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final String FRAGMENT_TAG_DIALOG_LEGEND = "LEGEND";
    private static final String FRAGMENT_TAG_DIALOG_USAGE = "USAGE";
    private static final String FRAGMENT_TAG_DIALOG_ABOUT = "ABOUT";
    private static final String FRAGMENT_TAG_DIALOG_UPDATE_MAP = "UPDATE_MAP";
    private static final String FRAGMENT_TAG_DIALOG_UPDATE_SHELTER = "UPDATE_SHELTER";

    private static final int SHELTER_LOADER_ID = 1;
    private static final int NEAREST_LOADER_ID = 2;
    private static final int CHECK_UPDATE_MAP_LOADER_ID = 3;
    private static final int UPDATE_MAP_LOADER_ID = 4;
    private static final int CHECK_UPDATE_SHELTER_LOADER_ID = 5;
    private static final int UPDATE_SHELTER_LOADER_ID = 6;

    private static final int DELAY_CHECK_UPDATE = 10 * 1000; // オフラインマップ更新チェックまでの時間 (ms)

    private MapView mMapView;
    private LayerManager mLayerManager;
    private boolean wasSearched;

    private ShelterType mSearchTargetShelterType = ShelterType.INVALID;

    private NearestShelterLoaderCallbacks mNearestLoaderCallbacks;
    private ProgressBar mProgressBar;
    private Handler mHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mNearestLoaderCallbacks = null;
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

        // レイヤーマネージャ
        mLayerManager = new LayerManager(this.getActivity(), mMapView);
    }

    private void setupSpinner() {
        // ツールバーのカスタムビューとして追加
        ActionBar ab = ((AppCompatActivity)this.getActivity()).getSupportActionBar();
        ab.setDisplayShowCustomEnabled(true);
        ab.setCustomView(R.layout.action_spinner);

        Spinner spinner = (Spinner) ab.getCustomView().findViewById(R.id.action_spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.shelter_type, R.layout.spinner_dropdown);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
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
                if (PermissionUtil.checkPermissionGranted(MapFragment.this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 画面表示を更新
                    mLayerManager.updateShelterMarker(mSearchTargetShelterType);
                    // 現在位置を取得したら更新
                    if (mLayerManager.getCurrentLocation() != null) {
                        updateLocationAndSetCenter(mLayerManager.getCurrentLocation());
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

        mHandler = new Handler();

        // 地図データにアクセスできる時だけ表示
        if (PermissionUtil.checkPermissionGranted(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
        Log.d(TAG, "onPause");
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
            case R.id.action_search:
                if (mLayerManager.getCurrentLocation() != null) {
                    updateLocationAndSetCenter(mLayerManager.getCurrentLocation());
                    if (! isSearching()) {
                        Toast.makeText(this.getActivity(), "現在地の最寄りの避難所を検索します。", Toast.LENGTH_SHORT).show();
                        searchNearShelter();
                    }
                }
                return true;
            case R.id.action_legend:
                DialogFragment dialog = new LegendDialogFragment();
                showDialog(dialog, FRAGMENT_TAG_DIALOG_LEGEND);
                return true;
            case R.id.action_usage:
                dialog = new UsageDialogFragment();
                showDialog(dialog, FRAGMENT_TAG_DIALOG_USAGE);
                return true;
            case R.id.action_check_update:
                if (ConnectionUtil.isWiFiConnected(getContext())) {
                    Toast.makeText(this.getActivity(), "オフラインマップデータの更新があるか確認します。", Toast.LENGTH_SHORT).show();
                    checkOfflineMap();
                } else {
                    Toast.makeText(this.getActivity(), "データの更新は WiFi 接続時のみ可能です。", Toast.LENGTH_SHORT).show();
                }
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

        // 避難所ファイル
        res = ShelterUpdater.prepareOfflineShelterFile(this.getActivity());
        if (!res) {
            Log.w(TAG, "避難所 ファイルの準備に失敗しました");
        }

    }

    private void onGrantedMapDraw() {
        Log.d(TAG, "onGrantedMapDraw");

        // マップ
        MapViewSetupper.setupMapView(this.getActivity(), mMapView);

        // 避難所表示
        if (mLayerManager.isSetShelters()) {
            updateShelter();
        } else {
            this.getLoaderManager().initLoader(SHELTER_LOADER_ID, null, new ShelterLoaderCallbacks());
        }
    }

    private void updateShelter() {
        mLayerManager.updateShelterMarker(mSearchTargetShelterType);
    }

    /**
     * 現在位置マーカーを更新し、地図の中心を現在位置に合わせる
     * @param loc
     */
    public void updateLocationAndSetCenter(LatLong loc) {
        mLayerManager.updateCurrentMarker(loc);
        if (LocationUtil.isInTargetArea(loc)) {
            mMapView.setCenter(loc);
        }
    }

    /**
     * 現在位置マーカーを更新する
     *
     * 検索済みフラグ (wasSearched フラグ) が false であれば、
     * 最寄りの避難所までの経路も検索する
     * @param loc
     */
    public void updateCurrentLocation(LatLong loc) {
        Log.d(TAG, "updateCurrentLocation");
        mLayerManager.updateCurrentMarker(loc);

        // 近傍の避難所探索
        if (LocationUtil.isInTargetArea(loc)) {
            Log.d(TAG, "wasSearched: " + wasSearched);
            if (!wasSearched) {
                searchNearShelter();
            }
        }
    }

    private void searchNearShelter() {
        Log.d(TAG, "searchNearShelter");
          if (mLayerManager.getShelterManager() != null) {
            wasSearched = true;
            setNearestLoaderCallbacks();
            getLoaderManager().restartLoader(NEAREST_LOADER_ID, null, mNearestLoaderCallbacks);
        }
    }

    private void setNearestLoaderCallbacks() {
        if (null == mNearestLoaderCallbacks) {
            mNearestLoaderCallbacks = new NearestShelterLoaderCallbacks();
        }
    }

    private boolean isSearching() {
        return mProgressBar.getVisibility() == View.VISIBLE;
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
            updateShelter();

            // 一定時間後、最新のオフラインマップがあるか確認処理を呼び出す
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    checkOfflineMap();
                }
            }, DELAY_CHECK_UPDATE);

            // Loader の破棄
            MapFragment.this.getLoaderManager().destroyLoader(SHELTER_LOADER_ID);
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

            if (data == null || data.size() == 0) {
                Toast.makeText(MapFragment.this.getActivity(), "経路検索ができませんでした。アプリおよび経路情報ファイルが最新であるか確認してください。", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onLoaderReset(Loader<List<NearestShelter.ShelterPath>> loader) {

        }
    }

    // オフラインマップ更新チェック
    private void checkOfflineMap() {
        Log.d(TAG, "checkOfflineMap");

        // WiFi 接続時の判定
        if (ConnectionUtil.isWiFiConnected(getContext())) {
            Log.d(TAG, "WiFi connected. start check offline map update");
            this.getLoaderManager().initLoader(CHECK_UPDATE_MAP_LOADER_ID, null, new OfflineMapCheckerLoaderCallbacks());
        } else {
            Log.d(TAG, "WiFi disconnected. skip offline map update");
        }
    }

    private class OfflineMapCheckerLoaderCallbacks implements LoaderManager.LoaderCallbacks<Boolean> {
        private final String TAG = OfflineMapCheckerLoaderCallbacks.class.getSimpleName();
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            Log.d(TAG, "onCreateLoader");
            return new OfflineMapCheckerAsyncTaskLoader(MapFragment.this.getActivity());
        }
        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean aBoolean) {
            Log.d(TAG, "オフラインマップcheck: " + aBoolean);
            if (aBoolean) {
                // onLoadFinished からは直接 DialogFragment 表示ができない
                // いったん、handler 経由にして、呼び出し側でActivityの状態をチェックする
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        confirmUserMapUpdateOrNot();
                    }
                });
            } else {
                Log.d(TAG, "引き続き、避難所ファイルの更新をチェックします");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkShelter();
                    }
                });
            }
            MapFragment.this.getLoaderManager().destroyLoader(CHECK_UPDATE_MAP_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    }

    private void confirmUserMapUpdateOrNot() {

        if (! this.getActivity().isFinishing() && ! this.getActivity().isDestroyed()) {
            DialogFragment f = MapUpdateConfirmationDialogFragment.getInstance(MapFragment.this);
            showDialog(f, FRAGMENT_TAG_DIALOG_UPDATE_MAP);
        } else {
            Log.w(TAG, "activity is on finishing or destroyed. so update confirmation dialog skipped.");
        };

    }

    // MapUpdateConfirmationDialogFragment のListener
    @Override
    public void onOkClickMap() {
        updateOfflineMap();
    }
    @Override
    public void onCancelClickMap() {
        // 引き続き、避難所ファイルの更新を確認する
        Log.d(TAG, "onCancelClickMap, 避難所ファイルの更新をチェックします");
        checkShelter();
    }

    /**
     * オフラインマップ更新
     */
    private void updateOfflineMap() {
        Log.d(TAG, "updateOfflineMap");
        // オフラインマップ更新
        this.getLoaderManager().initLoader(UPDATE_MAP_LOADER_ID, null, new OfflineMapDownLoaderLoaderCallbacks());
    }

    private class OfflineMapDownLoaderLoaderCallbacks implements LoaderManager.LoaderCallbacks<Boolean> {
        private final String TAG = OfflineMapDownLoaderLoaderCallbacks.class.getSimpleName();
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            Log.d(TAG, "onCreateLoader");
            return new OfflineMapDownLoaderAsyncTaskLoader(MapFragment.this.getActivity());
        }
        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(MapFragment.this.getContext(), "オフラインマップ更新に成功しました。再起動します。", Toast.LENGTH_LONG).show();
                // アプリを再起動
                if (MapFragment.this.getActivity() instanceof MainActivity) {

                    destroyAllLoaders();
                    ((MainActivity)MapFragment.this.getActivity()).restart();
                }

            } else {
                Toast.makeText(MapFragment.this.getContext(), "更新失敗", Toast.LENGTH_LONG).show();
            }
            MapFragment.this.getLoaderManager().destroyLoader(UPDATE_MAP_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    }

    // 避難所ファイル更新チェック
    private void checkShelter() {
        Log.d(TAG, "checkShelter");

        // WiFi 接続時の判定
        if (ConnectionUtil.isWiFiConnected(getContext())) {
            Log.d(TAG, "WiFi connected. start check shelter update");
            this.getLoaderManager().initLoader(CHECK_UPDATE_SHELTER_LOADER_ID, null, new OfflineShelterCheckerLoaderCallbacks());
        } else {
            Log.d(TAG, "WiFi disconnected. skip shelter update");
        }
    }

    private class OfflineShelterCheckerLoaderCallbacks implements LoaderManager.LoaderCallbacks<Boolean> {
        private final String TAG = OfflineShelterCheckerLoaderCallbacks.class.getSimpleName();
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            Log.d(TAG, "onCreateLoader");
            return new OfflineShelterCheckerAsyncTaskLoader(MapFragment.this.getActivity());
        }
        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean aBoolean) {
            Log.d(TAG, "避難所ファイルcheck: " + aBoolean);
            if (aBoolean) {
                // onLoadFinished からは直接 DialogFragment 表示ができない
                // いったん、handler 経由にして、呼び出し側でActivityの状態をチェックする
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        confirmUserShelterUpdateOrNot();
                    }
                });
            }
            MapFragment.this.getLoaderManager().destroyLoader(CHECK_UPDATE_SHELTER_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    }

    private void confirmUserShelterUpdateOrNot() {

        if (! this.getActivity().isFinishing() && ! this.getActivity().isDestroyed()) {
            DialogFragment f = ShelterUpdateConfirmationDialogFragment.getInstance(MapFragment.this);
            showDialog(f, FRAGMENT_TAG_DIALOG_UPDATE_SHELTER);
        } else {
            Log.w(TAG, "activity is on finishing or destroyed. so update confirmation dialog skipped.");
        };

    }

    // ShelterUpdateConfirmationDialogFragment のListener
    @Override
    public void onOkClickShelter() {
        updateShelterFile();
    }

    /**
     * 避難所ファイル更新
     */
    private void updateShelterFile() {
        Log.d(TAG, "updateShelterFile");
        // 避難所ファイル更新
        this.getLoaderManager().initLoader(UPDATE_SHELTER_LOADER_ID, null, new OfflineShelterDownLoaderLoaderCallbacks());
    }

    private class OfflineShelterDownLoaderLoaderCallbacks implements LoaderManager.LoaderCallbacks<Boolean> {
        private final String TAG = OfflineShelterDownLoaderLoaderCallbacks.class.getSimpleName();
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            Log.d(TAG, "onCreateLoader");
            return new OfflineShelterDownLoaderAsyncTaskLoader(MapFragment.this.getActivity());
        }
        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean aBoolean) {
            if (aBoolean) {
                // 避難所データの更新
                try {
                    ShelterUpdater.updateShelter(MapFragment.this.getContext());
                } catch (Exception e) {
                    Log.w(TAG, "避難所データ更新でエラーが発生しました。更新処理を中断します。", e);
                    Toast.makeText(MapFragment.this.getContext(), "避難所ファイル更新時にエラーが発生しました。更新処理を中断します。", Toast.LENGTH_LONG).show();

                    try {
                        ShelterUpdater.rollbackShelterFiles(MapFragment.this.getContext());
                    } catch(Exception ex) {
                        Log.w(TAG, "ロールバックに失敗しました。", ex);
                        Toast.makeText(MapFragment.this.getContext(), "避難所データの復元に失敗しました。データをクリアして再起動してください。", Toast.LENGTH_LONG).show();
                    }

                    MapFragment.this.getLoaderManager().destroyLoader(UPDATE_SHELTER_LOADER_ID);
                    return;
                }

                Toast.makeText(MapFragment.this.getContext(), "避難所ファイル更新に成功しました。再起動します。", Toast.LENGTH_LONG).show();
                // アプリを再起動
                if (MapFragment.this.getActivity() instanceof MainActivity) {

                    destroyAllLoaders();
                    ((MainActivity)MapFragment.this.getActivity()).restart();
                }

            } else {
                Toast.makeText(MapFragment.this.getContext(), "更新失敗", Toast.LENGTH_LONG).show();
            }
            MapFragment.this.getLoaderManager().destroyLoader(UPDATE_SHELTER_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    }

    private void destroyAllLoaders() {
        MapFragment.this.getLoaderManager().destroyLoader(SHELTER_LOADER_ID);
        MapFragment.this.getLoaderManager().destroyLoader(NEAREST_LOADER_ID);
        MapFragment.this.getLoaderManager().destroyLoader(CHECK_UPDATE_MAP_LOADER_ID);
        MapFragment.this.getLoaderManager().destroyLoader(UPDATE_MAP_LOADER_ID);
        MapFragment.this.getLoaderManager().destroyLoader(CHECK_UPDATE_SHELTER_LOADER_ID);
        MapFragment.this.getLoaderManager().destroyLoader(UPDATE_SHELTER_LOADER_ID);
    }
}
