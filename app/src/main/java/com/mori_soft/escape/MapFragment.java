package com.mori_soft.escape;

import android.Manifest;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.graphhopper.GraphHopper;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.model.NearestShelter;
import com.mori_soft.escape.provider.ShelterContract;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mor on 2017/06/27.
 */

public class MapFragment extends Fragment {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final int SHELTER_LOADER_ID = 1;

    private final static String MAP_FILE = "japan_multi.map";
    //    private final static String MAP_FILE = "ise_shima.map";

    private MapView mMapView;
    private GraphHopper mGraphHopper;

    private Location mCurrentLocation;
    private Marker mCurrent;

    private List<NearestShelter.ShelterPath> mNearest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AndroidGraphicFactory.createInstance(this.getActivity().getApplication());

    }

    @Override
    public void onResume() {
        super.onResume();

        // 地図データにアクセスできる時だけ表示
        if (PermissionUtil.checkPermissionGranted(this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            onGrantedMapDraw();
        }
    }

    @Override
    public void onDestroyView() {
        mMapView.destroyAll();;
        AndroidGraphicFactory.clearResourceMemoryCache();

        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void onGrantedMapDraw() {
        // マップ
        setMapView();
        displayMap();

        // 経路探索準備
        prepareGraphHopper();

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

    public void updateCurrentLocation(Location loc) {
        mCurrentLocation = loc;

//        if (mIsLocationAvailable) {
        if (true) { // TODO for test
            if (mCurrent != null) {
                mMapView.getLayerManager().getLayers().remove(mCurrent);
            }
            mCurrent = createMarker(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), R.drawable.marker_red);
            mMapView.getLayerManager().getLayers().add(mCurrent);
        }

        Log.d(TAG, "location: " + DateFormat.format("yyyy/MM/dd kk:mm:ss", new Date(mCurrentLocation.getTime())) + ", lat lon : " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
    }

    private Marker createMarker(LatLong latlong, int resource) {
        Drawable drawable = getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new Marker(latlong, bitmap, 0, -bitmap.getHeight() / 2);
    }

    private void updateShelterMarker(List<NearestShelter.ShelterPath> paths) {
        if (paths == null || paths.size() == 0) {
            mMapView.getLayerManager().getLayers().clear(); // TODO 現在位置とタイルは大丈夫か？
            return;
        }

        for (NearestShelter.ShelterPath path : paths) {
            Marker marker = createMarker(new LatLong(path.shelter.lat, path.shelter.lon), R.drawable.marker_green);
            mMapView.getLayerManager().getLayers().add(marker);
        }

    }


    private void prepareGraphHopper() {

        AsyncTask<Void, Void, GraphHopper> task = new AsyncTask<Void, Void, GraphHopper>() {
            private boolean mHasError = false;

            @Override
            protected GraphHopper doInBackground(Void... params) {
                try {
                    GraphHopper tmp = new GraphHopper().forMobile();
                    tmp.load(getGraphHopperFolder());
                    return tmp;
                } catch (Exception e) {
                    Log.e(TAG, "Exception occured" , e);
                    mHasError = true;
                }
                return null;
            }

            @Override
            protected void onPostExecute(GraphHopper graphHopper) {
                super.onPostExecute(graphHopper);
                if (mHasError) {
                    // TODO エラー処理
                } else {
                    mGraphHopper = graphHopper;
                }
                // TODO もし何かするなら
            }
        }.execute();
    }

    private String getGraphHopperFolder() {
        // TODO 暫定
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/com_mori_soft_escape/gh").getAbsolutePath();
    }


    private class ShelterLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
                ent.isTsunami   = data.getInt(data.getColumnIndex(ShelterContract.Shelter.IS_SHELTER)) == ShelterContract.DB_INT_TRUE;
                ent.ranking     = data.getInt(data.getColumnIndex(ShelterContract.Shelter.RANK));
                ent.isLiving    = data.getInt(data.getColumnIndex(ShelterContract.Shelter.IS_LIVING)) == ShelterContract.DB_INT_TRUE;
                ent.lat          = data.getDouble(data.getColumnIndex(ShelterContract.Shelter.LAT));
                ent.lon          = data.getDouble(data.getColumnIndex(ShelterContract.Shelter.LON));
            } while (data.moveToNext());

            // 最寄りの避難所検索, 非同期に求める
            if (null != mCurrentLocation) {
                AsyncTask<List<ShelterEntity>, Void, List<NearestShelter.ShelterPath>> task = new AsyncTask<List<ShelterEntity>, Void, List<NearestShelter.ShelterPath>>() {
                    @Override
                    protected List<NearestShelter.ShelterPath> doInBackground(List<ShelterEntity>... params) {
                        NearestShelter ns = new NearestShelter(mGraphHopper);
                        List<NearestShelter.ShelterPath> paths = ns.sortNearestShelter(params[0], mCurrentLocation);
                        return paths;
                    }

                    @Override
                    protected void onPostExecute(List<NearestShelter.ShelterPath> shelterPaths) {
                        super.onPostExecute(shelterPaths);
                        MapFragment.this.updateShelterMarker(shelterPaths);
                    }
                }.execute(shelters);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }


}
