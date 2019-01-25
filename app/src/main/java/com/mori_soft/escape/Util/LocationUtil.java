package com.mori_soft.escape.Util;

import org.mapsforge.core.model.LatLong;

/**
 * 位置情報関係のユーティリティクラス
 */

public class LocationUtil {

    // オフラインマップおよび経路情報が存在する領域
    private static final LatLong TARGET_TOP_LEFT = new LatLong(34.585, 136.517);
    private static final LatLong TARGET_BOTTOM_RIGHT = new LatLong(34.377, 136.928);

    public static boolean isInTargetArea(LatLong loc) {
        if (loc == null) {
            return false;
        }
        return isInTargetLat(loc.latitude) && isInTargetLong(loc.longitude);
    }

    private static boolean isInTargetLat(final double lat) {
        return TARGET_BOTTOM_RIGHT.latitude <= lat && lat <= TARGET_TOP_LEFT.latitude;
    }
    private static boolean isInTargetLong(final double lon) {
        return TARGET_TOP_LEFT.longitude <= lon && lon <= TARGET_BOTTOM_RIGHT.longitude;
    }
}
