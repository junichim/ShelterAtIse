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
import android.graphics.Color;
import android.util.Log;

import com.graphhopper.ResponsePath;
import com.mori_soft.escape.R;
import com.mori_soft.escape.Util.MarkerUtils;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.model.NearestShelter;
import com.mori_soft.escape.model.ShelterManager;
import com.mori_soft.escape.model.ShelterType;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * マーカー等のLayer管理クラス.
 */
public class LayerManager {

    private static final int TOP3 = 3;

    private Context mContext;
    private MapView mMapView;

    private ShelterManager mShelterManager;
    private List<NearestShelter.ShelterPath> mShelterPaths; // 現在位置を取得できない場合もあるので、

    private Map<LayerId, Layer> mMap;
    private List<Integer> mNearShelterId;

    public LayerManager(Context context, MapView mapView) {
        mContext = context;
        mMapView = mapView;
        mShelterManager = null;
        mMap = new HashMap<LayerId, Layer>();
        mNearShelterId = new ArrayList<Integer>();
    }

    public ShelterManager getShelterManager() {
        return mShelterManager;
    }

    public LatLong getCurrentLocation() {
        LayerId key = new LayerId(LayerType.CurrentLocation);
        Marker current = (Marker)mMap.get(key);
        return current != null ? current.getLatLong() : null;
    }

    public void updateCurrentMarker(LatLong location) {
        LayerId key = new LayerId(LayerType.CurrentLocation);
        removeLayer(key);
        Marker current = MarkerUtils.createCurrentMarker(location, mContext, R.drawable.ic_my_location);
        addLayer(key, current);
    }

    /**
     * 避難所情報がセット済みか否か
     * @return
     */
    public boolean isSetShelters() {
        return  mShelterManager != null && mShelterManager.size() > 0;
    }

    /**
     * 避難所リストの設定.
     *
     * 避難所リスト設定は更新がない限り、一度実行すればよい
     * @param shelters 避難所リスト
     */
    public void setShelters(List<ShelterEntity> shelters) {
        if (shelters == null || shelters.size() == 0) {
            // 既存のマーカーを削除
            removeAllSheltersAndRelated();
            return;
        }
        mShelterManager = new ShelterManager();
        mShelterManager.setShelters(shelters);
    }

    /**
     * 避難所マーカーの更新.
     */
    public void updateShelterMarker(ShelterType shelterType) {
        if (mShelterManager == null || mShelterManager.size() == 0) {
            return;
        }

        // 既存のマーカーを削除
        removeAllSheltersAndRelated();

        // 対象か否かに応じてマーカーを表示
        for (ShelterEntity ent : mShelterManager.getValues()) {
            final boolean isSelected = isSelectedShelter(shelterType, ent);
            addShelterMarker(isSelected, ent);
        }
    }

    private boolean isSelectedShelter(ShelterType shelterType, ShelterEntity ent) {
        boolean isSelected = false;
        if (shelterType == ShelterType.TSUNAMI && ent.isTsunami) {
            isSelected = true;
        } else if (shelterType == ShelterType.DESIGNATION && ent.isShelter) {
            isSelected = true;
        }
        return isSelected;
    }

    private void addShelterMarker(boolean isSelected, ShelterEntity ent) {
        LayerType lt = LayerType.Invalid;
        int resId;
        if (isSelected) {
            lt = LayerType.SelectedShelter;
            resId = R.drawable.marker_green;
        } else {
            lt = LayerType.NonSelectedShelter;
            resId = R.drawable.marker_gray;
        }

        LayerId key = new LayerId(lt, ent.recordId);
        Marker marker = MarkerUtils.createShelterMarker(ent, mContext, resId, mMapView);
        ((MarkerWithBubble)marker).setOnLongPressListener(mLongPressListener);
        addLayer(key, marker);
    }

    public void updateNearShelterMarker(List<NearestShelter.ShelterPath> paths, ShelterType shelterType) {
        mShelterPaths = paths;

        // 既存の近傍の避難所を通常の避難所に変換
        swapAllNearShelterMarkerToShelterMarker(shelterType);

        if (paths == null || paths.size() == 0) {
            return;
        }

        // 新たに近傍となった避難所を登録
        final int numPoint = TOP3 < paths.size() ? TOP3 : paths.size();
        for(int i = 0; i < numPoint; i++) {
            // 近傍となった避難所の通常のマーカーを削除
            final int id = paths.get(i).shelter.recordId;
            LayerId mid = new LayerId(LayerType.SelectedShelter, id);
            removeLayer(mid);

            // 追加
            LayerId key;
            int resId;
            if (i == 0) {
                key = new LayerId(LayerType.NearestShelter, id);
                resId = R.drawable.marker_red;
            } else {
                key = new LayerId(LayerType.NearShelter, id);
                resId = R.drawable.marker_yellow;
            }
            Marker marker = MarkerUtils.createNearShelterMarker(paths.get(i), mContext, resId, mMapView);
            ((MarkerWithBubble)marker).setOnLongPressListener(mLongPressListener);
            addLayer(key, marker);

            mNearShelterId.add(id);
        }
    }

    private void swapAllNearShelterMarkerToShelterMarker(ShelterType shelterType) {

        // 既存の近傍の避難所を通常の避難所に変換
        removeAllSpecificLayers(new LayerType[] {LayerType.NearestShelter, LayerType.NearShelter});

        for(int i = 0; i < mNearShelterId.size(); i++) {
            swapNearShelterMarkerToShelterMarker(mNearShelterId.get(i), shelterType);
        }
        mNearShelterId.clear();
    }
    private void swapNearShelterMarkerToShelterMarker(int id, ShelterType shelterType) {

        for (ShelterEntity ent : mShelterManager.getValues()) {
            if (ent.recordId == id) {
                final boolean isSelected = isSelectedShelter(shelterType, ent);
                addShelterMarker(isSelected, ent);
            }
        }
    }

    public void updateNearestShelterPath() {
        if (mShelterPaths == null || mShelterPaths.size() == 0) {
            return;
        }
        NearestShelter.ShelterPath sp = mShelterPaths.get(0);
        updatePathToShelter(sp);
    }
    private void updatePathToShelter(NearestShelter.ShelterPath shelterPath) {
        LayerId key = new LayerId(LayerType.PathToShelter);
        removeLayer(key);
        if (shelterPath == null) {
            return;
        }
        if (shelterPath.dist >= 0) {
            Layer line = createPolyline(shelterPath.path, shelterPath.startPoint, new LatLong(shelterPath.shelter.lat, shelterPath.shelter.lon));
            addLayer(key, line);
        }
    }

    private Polyline createPolyline(ResponsePath response, LatLong start, LatLong end) {
        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(Color.argb(128, 0, 0xCC, 0x33));
        //paintStroke.setDashPathEffect(new float[]{25, 15});
        paintStroke.setStrokeWidth(12);

        Polyline line = new Polyline(paintStroke, AndroidGraphicFactory.INSTANCE);
        List<LatLong> geoPoints = line.getLatLongs();

        geoPoints.add(start);
        for (int i = 0; i < response.getPoints().getSize(); i++) {
            geoPoints.add(new LatLong(response.getPoints().getLatitude(i), response.getPoints().getLongitude(i)));
        }
        geoPoints.add(end);

        return line;
    }

    /**
     * 避難所及び避難所に関連するLayerを削除.
     */
    private void removeAllSheltersAndRelated() {
        removeAllSpecificLayers(new LayerType[]{
            LayerType.SelectedShelter,
            LayerType.NonSelectedShelter,
            LayerType.NearestShelter,
            LayerType.NearShelter,
            LayerType.PathToShelter,
            });
        // 近傍の李難所リストもクリア
        mNearShelterId.clear();
    }

    /**
     * 指定されたタイプのLayerを削除
     * @param markerTypes
     */
    private void removeAllSpecificLayers(LayerType[] markerTypes) {
        List<LayerId> target = new ArrayList<LayerId>();
        for (LayerId key : mMap.keySet()) {
            for (int i = 0; i < markerTypes.length; i++) {
                if (key.getMarkerType() == markerTypes[i]) {
                    Layer layer = mMap.get(key);
                    if (layer != null) {
                        mMapView.getLayerManager().getLayers().remove(layer);
                    }
                    target.add(key);
                    break;
                }
            }
        }
        for (LayerId key : target) {
            mMap.remove(key);
        }
    }

    private void removeLayer(LayerId key) {
        Layer layer = mMap.get(key);
        if (layer != null) {
            mMapView.getLayerManager().getLayers().remove(layer);
        }
        mMap.remove(key);
    }

    private void addLayer(LayerId key, Layer layer) {
        mMapView.getLayerManager().getLayers().add(layer);
        mMap.put(key, layer);
    }

    private MarkerWithBubble.OnLongPressListener mLongPressListener = new MarkerWithBubble.OnLongPressListener() {
        private final String TAG = MarkerWithBubble.OnLongPressListener.class.getSimpleName();
        @Override
        public boolean onLongPress(LatLong tapLatLong, Point layerXY, Point tapXY, int id) {
            Log.d(TAG, "onLongPress: " + id);
            for (NearestShelter.ShelterPath path : mShelterPaths) {
                if (path.shelter.recordId == id) {
                    updatePathToShelter(path);
                    return true;
                }
            }
            return false;
        }
    };

}
