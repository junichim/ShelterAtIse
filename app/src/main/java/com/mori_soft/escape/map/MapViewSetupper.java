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
package com.mori_soft.escape.map;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.mori_soft.escape.Util.AssetFileUtils;

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

/**
 * MapView設定クラス.
 */

public class MapViewSetupper {

    private static final String TAG = MapViewSetupper.class.getSimpleName();

    private static final int MIN_ZOOM_LEVEL = 1;
    private static final int MAX_ZOOM_LEVEL = 22;
    private static final int DEFAULT_ZOOM_LEVEL = 17;
    private static final double INIT_LAT = 34.491297; // 伊勢市駅
    private static final double INIT_LON = 136.709685;

    private final static String MAP_FILE = "ise.map";

    public static void setupMapView(Context context, MapView mapView) {
        setMapView(mapView);
        displayMap(context, mapView);
    }

    private static void setMapView(MapView mapView) {
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setZoomLevelMin((byte)MIN_ZOOM_LEVEL);
        mapView.setZoomLevelMax((byte)MAX_ZOOM_LEVEL);
    }

    private static void displayMap(Context context, MapView mMapView) {
        Log.d(TAG, "displayMap");
        TileCache tileCache = AndroidUtil.createTileCache(context, "mapcache", mMapView.getModel().displayModel.getTileSize(), 1f, mMapView.getModel().frameBufferModel.getOverdrawFactor() );

        File file = new File(getOfflineMapFile(context));
        if (! file.exists()) {
            Log.e(TAG, "file not found: " + file.getAbsolutePath());
            Toast.makeText(context, "オフライン地図ファイルがありません", Toast.LENGTH_LONG).show();
            return;
        }

        MapDataStore mds = new MapFile(file);
        TileRendererLayer trl = new TileRendererLayer(tileCache, mds, mMapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        trl.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

        mMapView.getLayerManager().getLayers().add(trl);

        mMapView.setCenter(new LatLong(INIT_LAT, INIT_LON));
        mMapView.setZoomLevel((byte)DEFAULT_ZOOM_LEVEL);
    }

    private static String getOfflineMapFile(Context context) {
        return new File(context.getExternalFilesDir(null),  "/" + MAP_FILE).getAbsolutePath();
    }

    public static boolean prepareOfflineMapFile(Context context) {
        File mf = new File(getOfflineMapFile(context));

        // フォルダ名 or フォルダ名.ghz の存在確認
        if (mf.exists() && mf.isFile()) {
            // 準備完了
            return true;
        }

        return AssetFileUtils.copyFromAsset(context, MAP_FILE, mf.getAbsolutePath());
    }

}
