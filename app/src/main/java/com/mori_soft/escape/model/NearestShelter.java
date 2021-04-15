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

import android.location.Location;
import android.util.Log;
import android.util.Pair;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Parameters;
import com.mori_soft.escape.Util.LocationUtil;
import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.provider.ShelterContract;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 近傍の避難所検索クラス.
 */
public class NearestShelter {

    private static final String TAG = NearestShelter.class.getSimpleName();

    private GraphHopper mGraphHopper;

    private static class PathComparator implements Comparator<ShelterPath> {
        @Override
        public int compare(ShelterPath o1, ShelterPath o2) {
            if (o1.dist < 0) {
                if (o2.dist >= 0) {
                    return 1; // o1 が負
                } else {
                    return 0; // 共に負
                }
            } else {
                if (o2.dist < 0) {
                    return -1; // o2 のみ負
                }
            }
            return (int)(o1.dist - o2.dist);
        }
    }

    public static class ShelterPath {
        public static final int INVALID_DIST = -1;
        public ShelterEntity shelter;
        public ResponsePath path;
        public double dist;
        public LatLong startPoint;

        public ShelterPath(ShelterEntity shlt, ResponsePath responsePath, double distance, LatLong start) {
            shelter = shlt;
            path = responsePath;
            dist = distance;
            startPoint = start;
        }
    }

    public NearestShelter(GraphHopper graphHopper) {
        mGraphHopper = graphHopper;
    }

    /**
     * 近傍の避難所の検索.
     *
     * @param shelters     避難所一覧
     * @param current      現在位置
     * @param shelterType  避難所種別
     * @return 対象の避難所, 対象外の避難所 が現在位置からの距離順に並ぶ
     */
    public List<ShelterPath> sortNearestShelter(Collection<ShelterEntity> shelters, LatLong current, ShelterType shelterType) {
        List<ShelterPath> list = findPathToShelter(shelters, current, shelterType);
        return list;
    }

    private List<ShelterPath> findPathToShelter(Collection<ShelterEntity> shelters, LatLong current, ShelterType shelterType) {
        List<ShelterPath> paths = new ArrayList<ShelterPath>();

        if (current == null || ! LocationUtil.isInTargetArea(current)) {
            for (ShelterEntity shlt : shelters) {
                paths.add(new ShelterPath(shlt, null, ShelterPath.INVALID_DIST, current));
            }
            return paths;
        }

        List<ShelterPath> pathsTarget = new ArrayList<ShelterPath>();
        List<ShelterPath> pathsNonTarget = new ArrayList<ShelterPath>();

        for (ShelterEntity shlt : shelters) {
            ResponsePath path = calcPath(current.getLatitude(), current.getLongitude(), shlt.lat, shlt.lon);
            if (shelterType == ShelterType.TSUNAMI && shlt.isTsunami || shelterType == ShelterType.DESIGNATION && shlt.isShelter) {
                addPathResult(pathsTarget, shlt, path, current);
            } else {
                addPathResult(pathsNonTarget, shlt, path, current);
            }
        }
        Collections.sort(pathsTarget, new PathComparator());
        Collections.sort(pathsNonTarget, new PathComparator());

        paths.addAll(pathsTarget);
        paths.addAll(pathsNonTarget);
        return paths;
    }

    private ResponsePath calcPath(double lat1, double lon1, double lat2, double lon2) {

        GHRequest req = new GHRequest(lat1, lon1, lat2, lon2).setProfile(GraphHopperWrapper.GH_PROFILE);
        req.getHints().putObject(Parameters.Routing.INSTRUCTIONS, "false");

        if (null == mGraphHopper) {
            return null;
        }

        GHResponse res = mGraphHopper.route(req);
        if (res.hasErrors()) {
            Log.w(TAG, "route calculation has error: ");
            for (Throwable th : res.getErrors())
                Log.w(TAG, "error: ", th);
            return null;
        }
        ResponsePath path = res.getBest();
        if (path != null) {
            if (!path.hasErrors()) {
                return path;
            }
            Log.w(TAG, "route calculation has error: ");
            for (Throwable th : path.getErrors())
                Log.w(TAG, "error: ", th);
        }
        return null;
    }

    private void addPathResult(List<ShelterPath> lst, ShelterEntity shlt, ResponsePath responsePath, LatLong current) {
        if (responsePath == null) {
            lst.add(new ShelterPath(shlt, null, ShelterPath.INVALID_DIST, current));
        } else {
            lst.add(new ShelterPath(shlt, responsePath, responsePath.getDistance(), current));
        }

    }
}
