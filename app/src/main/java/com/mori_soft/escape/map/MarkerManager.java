package com.mori_soft.escape.map;

import android.content.Context;
import android.graphics.Color;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.PointList;
import com.mori_soft.escape.R;
import com.mori_soft.escape.Util.MarkerUtils;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.model.NearestShelter;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
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
 * Created by mor on 2017/06/29.
 */

public class MarkerManager {

    private static final int TOP3 = 3;

    private static enum MarkerType {
        Invalid,
        CurrentLocation,
        Shelter,
        NearShelter,
        NearPath
    }
    private static final class MarkerId {
        static final int INVALID = -1;

        private final MarkerType mType;
        private final int mNum; // Shelter, NearShelter の場合に recordId を与える

        public MarkerId(MarkerType type) {
            mType = type;
            mNum = INVALID;
        }
        public MarkerId(MarkerType type, int num) {
            mType = type;
            mNum = num;
        }
        public MarkerType getMarkerType() {
            return mType;
        }
        public int getNum() {
            return mNum;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MarkerId) {
                MarkerId mid = (MarkerId)obj;
                return mType == mid.mType &&
                        mNum == mid.mNum;
            }
            return false;
        }
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + mType.hashCode();
            result = 31 * result + mNum;
            return result;
        }
    }

    private Context mContext;
    private MapView mMapView;

    private Map<MarkerId, Layer> mMap;
    private List<Integer> mNearShelterId;

    public MarkerManager(Context context, MapView mapView) {
        mContext = context;
        mMapView = mapView;
        mMap = new HashMap<MarkerId, Layer>();
        mNearShelterId = new ArrayList<Integer>();
    }

    public LatLong getCurrentLocation() {
        MarkerId key = new MarkerId(MarkerType.CurrentLocation);
        Marker current = (Marker)mMap.get(key);
        return current != null ? current.getLatLong() : null;
    }

    public void updateCurrentMarker(LatLong location) {
        MarkerId key = new MarkerId(MarkerType.CurrentLocation);
        removeLayer(key);
        Marker current = MarkerUtils.createCurrentMarker(location, mContext, R.drawable.ic_my_location);
        addLayer(key, current);

//        Log.d(TAG, "location: " + DateFormat.format("yyyy/MM/dd kk:mm:ss", new Date(mCurrentLocation.getTime())) + ", lat lon : " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
    }

    public void updateShelterMarker(List<ShelterEntity> shelters) {
        if (shelters == null || shelters.size() == 0) {
            // TODO 既存のマーカーどうする？
            return;
        }
        for (ShelterEntity ent : shelters) {
            MarkerId key = new MarkerId(MarkerType.Shelter, ent.recordId);
            removeLayer(key);
            Marker marker = MarkerUtils.createShelterMarker(ent, mContext, R.drawable.marker_green, mMapView);
            addLayer(key, marker);
        }
    }

    public void updateNearShelterMarker(List<NearestShelter.ShelterPath> paths) {
        // 既存の近傍の避難所を通常の避難所に変換
        swapAllNearShelterMarkerToShelterMarker();

        if (paths == null || paths.size() == 0) {
            return;
        }

        // 新たに近傍となった避難所を登録
        final int numPoint = TOP3 < paths.size() ? TOP3 : paths.size();
        for(int i = 0; i < numPoint; i++) {
            // 近傍となった避難所の通常のマーカーを削除
            final int id = paths.get(i).shelter.recordId;
            MarkerId mid = new MarkerId(MarkerType.Shelter, id);
            removeLayer(mid);

            // 追加
            MarkerId key = new MarkerId(MarkerType.NearShelter, id);
            Marker marker = MarkerUtils.createNearShelterMarker(paths.get(i), mContext, R.drawable.marker_red, mMapView);
            addLayer(key, marker);

            mNearShelterId.add(id);
        }
    }


    private void swapAllNearShelterMarkerToShelterMarker() {
        // 既存の近傍の避難所を通常の避難所に変換
        for(int i = 0; i < mNearShelterId.size(); i++) {
            swapNearShelterMarkerToShelterMarker(mNearShelterId.get(i));
        }
        mNearShelterId.clear();
    }
    private void swapNearShelterMarkerToShelterMarker(int id) {
        // 既存の近傍の避難所
        MarkerId keyNear = new MarkerId(MarkerType.NearShelter, id);
        Marker near = (Marker)mMap.get(keyNear);

        // 通常の避難所マーカーの生成
        Marker marker = MarkerUtils.createShelterMarker(near.getLatLong(), MarkerUtils.getNormalInfoText(((MarkerWithBubble)near).getText()), mContext, R.drawable.marker_green, mMapView);
        MarkerId keyGeneric = new MarkerId(MarkerType.Shelter, id);
        addLayer(keyGeneric, marker);

        // 既存の近傍の避難所の削除
        removeLayer(keyNear);
    }

    public void updateNearShelterPath(List<NearestShelter.ShelterPath> paths) {
        MarkerId key = new MarkerId(MarkerType.NearPath, 1);
        removeLayer(key);
        if (paths == null || paths.size() == 0) {
            return;
        }
        Layer line = createPolyline(paths.get(0).path, paths.get(0).startPoint, new LatLong(paths.get(0).shelter.lat, paths.get(0).shelter.lon));
        addLayer(key, line);
    }

    private Polyline createPolyline(PathWrapper response, LatLong start, LatLong end) {
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


    private void removeAllSpecificLayer(MarkerType markerType) {
        for (MarkerId key : mMap.keySet()) {
            if (key.mType == markerType) {
                removeLayer(key);
            }
        }
    }

    private void removeLayer(MarkerId key) {
        Layer layer = mMap.get(key);
        if (layer != null) {
            mMapView.getLayerManager().getLayers().remove(layer);
            mMap.remove(key);
        }
    }

    private void addLayer(MarkerId key, Layer layer) {
        mMapView.getLayerManager().getLayers().add(layer);
        mMap.put(key, layer);
    }

}
