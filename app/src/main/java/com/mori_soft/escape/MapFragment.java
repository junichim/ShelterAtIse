package com.mori_soft.escape;

import android.Manifest;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.graphhopper.GraphHopper;
import com.mori_soft.escape.Util.Ranking;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.map.MarkerManager;
import com.mori_soft.escape.model.NearestShelter;
import com.mori_soft.escape.model.NearestShelterAsynkTaskLoader;
import com.mori_soft.escape.provider.ShelterContract;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mor on 2017/06/27.
 */

public class MapFragment extends Fragment {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final int SHELTER_LOADER_ID = 1;
    private static final int NEAREST_LOADER_ID = 2;

    private final static String MAP_FILE = "japan_multi.map";

    private MapView mMapView;
    private MarkerManager mMarkerManager;
    private List<ShelterEntity> mShelters;
    private boolean wasSearched; // TODO savedInstanceState へ

    private NearestShelterLoaderCallbacks mNearestLoaderCallbacks = new NearestShelterLoaderCallbacks();
    private List<NearestShelter.ShelterPath> mNearest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AndroidGraphicFactory.createInstance(this.getActivity().getApplication());
        wasSearched = false;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        // 地図データにアクセスできる時だけ表示
        if (PermissionUtil.checkPermissionGranted(this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
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

    private void onGrantedMapDraw() {
        Log.d(TAG, "onGrantedMapDraw");
        mMarkerManager = new MarkerManager(this.getActivity(), mMapView);

        // マップ
        setMapView();
        displayMap();

        // 避難所表示
        this.getLoaderManager().initLoader(SHELTER_LOADER_ID, null, new ShelterLoaderCallbacks());
    }

    private void setMapView() {
        mMapView.setClickable(true);
        mMapView.getMapScaleBar().setVisible(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setZoomLevelMin((byte)10);
        mMapView.setZoomLevelMax((byte)20);
    }

    private void displayMap() {
        Log.d(TAG, "displayMap");
        TileCache tileCache = AndroidUtil.createTileCache(this.getActivity(), "mapcache", mMapView.getModel().displayModel.getTileSize(), 1f, mMapView.getModel().frameBufferModel.getOverdrawFactor() );

        File file = new File(Environment.getExternalStorageDirectory() + "/Download/", MAP_FILE);
        if (! file.exists()) {
            Log.e(TAG, "file not found: " + file.getAbsolutePath());
            Toast.makeText(this.getActivity(), "オフライン地図ファイルがありません", Toast.LENGTH_LONG).show(); // TODO エラー処理
//            finish();
            return;
        }

        MapDataStore mds = new MapFile(file);
        TileRendererLayer trl = new TileRendererLayer(tileCache, mds, mMapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        trl.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

        mMapView.getLayerManager().getLayers().add(trl);

        mMapView.setCenter(new LatLong(34.491297, 136.709685)); // 伊勢市駅
        mMapView.setZoomLevel((byte)12);
    }

    public void updateLastLocation(LatLong loc) {
        mMarkerManager.updateCurrentMarker(loc);
    }
    public void updateCurrentLocation(LatLong loc) {
        Log.d(TAG, "updateCurrentLocation");
        mMarkerManager.updateCurrentMarker(loc);

        // 近傍の避難所探索
        if (!wasSearched) {
            searchNearShelter();
        }
    }

    private void searchNearShelter() {
        Log.d(TAG, "searchNearShelter");
        if (mShelters != null) {
            wasSearched = true;
            getLoaderManager().restartLoader(NEAREST_LOADER_ID, null, mNearestLoaderCallbacks);
        }
    }


    private class ShelterLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        private final String TAG = ShelterLoaderCallbacks.class.getSimpleName();
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
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
                    null, // TODO where clause
                    null, // TODO where arguments
                    null
                    );
        }
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.d(TAG, "onLoadFinished");

            if (data == null || data.getCount() == 0) {
                mShelters = null;
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

            mMarkerManager.updateShelterMarker(shelters);
            mShelters = shelters;
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
            return new NearestShelterAsynkTaskLoader(MapFragment.this.getActivity(), mShelters, mMarkerManager.getCurrentLocation());
        }
        @Override
        public void onLoadFinished(Loader<List<NearestShelter.ShelterPath>> loader, List<NearestShelter.ShelterPath> data) {
            Log.d(TAG, "onLoadFinished");
            Log.d(TAG, "data size:" + (data == null ? "null" : data.size()));
            mMarkerManager.updateNearShelterMarker(data);
        }
        @Override
        public void onLoaderReset(Loader<List<NearestShelter.ShelterPath>> loader) {

        }
    }

}