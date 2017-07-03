package com.mori_soft.escape.model;

import android.location.Location;
import android.util.Log;
import android.util.Pair;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Parameters;
import com.mori_soft.escape.entity.ShelterEntity;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mor on 2017/06/23.
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
        public PathWrapper path;
        public double dist;
        public LatLong startPoint;

        public ShelterPath(ShelterEntity shlt, PathWrapper pathWrapper, double distance, LatLong start) {
            shelter = shlt;
            path = pathWrapper;
            dist = distance;
            startPoint = start;
        }
    }

    public NearestShelter(GraphHopper graphHopper) {
        mGraphHopper = graphHopper;
    }

    public List<ShelterPath> sortNearestShelter(List<ShelterEntity> shelters, LatLong current, ShelterType shelterType) {
        List<ShelterPath> list = findPathToShelter(shelters, current, shelterType);
        Collections.sort(list, new PathComparator());
        return list;
    }

    private List<ShelterPath> findPathToShelter(List<ShelterEntity> shelters, LatLong current, ShelterType shelterType) {
        List<ShelterPath> paths = new ArrayList<ShelterPath>();

        if (current == null) {
            for (ShelterEntity shlt : shelters) {
                paths.add(new ShelterPath(shlt, null, ShelterPath.INVALID_DIST, current));
            }
            return paths;
        }

        for (ShelterEntity shlt : shelters) {
            if (shelterType == ShelterType.TSUNAMI && shlt.isTsunami || shelterType == ShelterType.DESIGNATION && shlt.isShelter) {
                PathWrapper path = calcPath(current.getLatitude(), current.getLongitude(), shlt.lat, shlt.lon);
                if (path == null) {
                    paths.add(new ShelterPath(shlt, null, ShelterPath.INVALID_DIST, current));
                } else {
                    paths.add(new ShelterPath(shlt, path, path.getDistance(), current));
                }
            } else {
                paths.add(new ShelterPath(shlt, null, ShelterPath.INVALID_DIST, current));
            }
        }
        return paths;
    }

    private PathWrapper calcPath(double lat1, double lon1, double lat2, double lon2) {

        GHRequest req = new GHRequest(lat1, lon1, lat2, lon2).setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI); // TODO アルゴリズム
        req.getHints().put(Parameters.Routing.INSTRUCTIONS, "false");

        GHResponse res = mGraphHopper.route(req);
        PathWrapper path = res.getBest();
        if (path != null) {
            if (!path.hasErrors()) {
                return path;
            }
            Log.d(TAG, "route calculation has error: ");
            for (Throwable th : path.getErrors())
                Log.d(TAG, "error: ", th);
        }
        return null;
    }
}
