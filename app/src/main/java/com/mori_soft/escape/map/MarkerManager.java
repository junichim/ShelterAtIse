package com.mori_soft.escape.map;

import android.content.Context;
import android.location.Location;

import com.mori_soft.escape.R;
import com.mori_soft.escape.Util.MarkerUtils;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.model.NearestShelter;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        Nearpath
    }
    private static final class MarkerId {
        static final int INVALID = -1;

        private final MarkerType mType;
        private final int mNum;

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

    private Map<MarkerId, Marker> mMap;
    private List<Integer> mNearShelterId;

    public MarkerManager(Context context, MapView mapView) {
        mContext = context;
        mMapView = mapView;
        mMap = new HashMap<MarkerId, Marker>();
        mNearShelterId = new ArrayList<Integer>();
    }

    public LatLong getCurrentLocation() {
        MarkerId key = new MarkerId(MarkerType.CurrentLocation);
        Marker current = (Marker)mMap.get(key);
        return current != null ? current.getLatLong() : null;
    }

    public void updateCurrentMarker(LatLong location) {
        MarkerId key = new MarkerId(MarkerType.CurrentLocation);
        removeMarker(key);
        Marker current = MarkerUtils.createCurrentMarker(location, mContext, R.drawable.ic_my_location);
        addMarker(key, current);

//        Log.d(TAG, "location: " + DateFormat.format("yyyy/MM/dd kk:mm:ss", new Date(mCurrentLocation.getTime())) + ", lat lon : " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
    }

    public void updateShelterMarker(List<ShelterEntity> shelters) {
        if (shelters == null || shelters.size() == 0) {
            // TODO 既存のマーカーどうする？
            return;
        }
        for (ShelterEntity ent : shelters) {
            MarkerId key = new MarkerId(MarkerType.Shelter, ent.recordId);
            removeMarker(key);
            Marker marker = MarkerUtils.createShelterMarker(ent, mContext, R.drawable.marker_green, mMapView);
            addMarker(key, marker);
        }
    }

    public void updateNearShelterMarker(List<NearestShelter.ShelterPath> paths) {
        if (paths == null || paths.size() == 0) {
            // 既存の近傍の避難所を通常の避難所に変換
            swapAllNearShelterMarkerToShelterMarker();
            return;
        }

        // 既存の近傍の避難所を通常の避難所に変換
        swapAllNearShelterMarkerToShelterMarker();

        // 新たに近傍となった避難所を登録
        final int numPoint = TOP3 < paths.size() ? TOP3 : paths.size();
        for(int i = 0; i < numPoint; i++) {
            // 近傍となった避難所の通常のマーカーを削除
            final int id = paths.get(i).shelter.recordId;
            MarkerId mid = new MarkerId(MarkerType.Shelter, id);
            removeMarker(mid);

            // 追加
            MarkerId key = new MarkerId(MarkerType.NearShelter, id);
            Marker marker = MarkerUtils.createNearShelterMarker(paths.get(i), mContext, R.drawable.marker_red, mMapView);
            addMarker(key, marker);

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
        Marker near = mMap.get(keyNear);

        // 通常の避難所マーカーの生成
        Marker marker = MarkerUtils.createShelterMarker(near.getLatLong(), MarkerUtils.getNormalInfoText(((MarkerWithBubble)near).getText()), mContext, R.drawable.marker_green, mMapView);
        MarkerId keyGeneric = new MarkerId(MarkerType.Shelter, id);
        addMarker(keyGeneric, marker);

        // 既存の近傍の避難所の削除
        removeMarker(keyNear);
    }

    private void removeAllSpecificMarker(MarkerType markerType) {
        for (MarkerId key : mMap.keySet()) {
            if (key.mType == markerType) {
                removeMarker(key);
            }
        }
    }

    private void removeMarker(MarkerId key) {
        Marker marker = mMap.get(key);
        if (marker != null) {
            mMapView.getLayerManager().getLayers().remove(marker);
            mMap.remove(key);
        }
    }

    private void addMarker(MarkerId key, Marker marker) {
        mMapView.getLayerManager().getLayers().add(marker);
        mMap.put(key, marker);
    }

}
