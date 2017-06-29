package com.mori_soft.escape.Util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.mori_soft.escape.entity.ShelterEntity;
import com.mori_soft.escape.map.MarkerWithBubble;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Created by mor on 2017/05/30.
 */

public class MarkerUtils {

    public static Bitmap viewToBitmap(Context c, View view) {
        view.measure(View.MeasureSpec.getSize(view.getMeasuredWidth()),
                View.MeasureSpec.getSize(view.getMeasuredHeight()));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Drawable drawable = new BitmapDrawable(c.getResources(),
                android.graphics.Bitmap.createBitmap(view.getDrawingCache()));
        view.setDrawingCacheEnabled(false);
        return AndroidGraphicFactory.convertToBitmap(drawable);
    }

    public static Marker createCurrentMarker(LatLong latlong, Context context, int resource) {
        Drawable drawable = context.getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new Marker(latlong, bitmap, 0, -bitmap.getHeight() / 2);
    }
    public static Marker createShelterMarker(ShelterEntity ent, Context context, int resource, MapView mapView) {
        Drawable drawable = context.getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new MarkerWithBubble(new LatLong(ent.lat, ent.lon), bitmap, 0, -bitmap.getHeight() / 2, mapView, getMarkerInfoText(ent));
    }

    private static String getMarkerInfoText(ShelterEntity ent) {
        return "名称: " + ent.shelterName + "\n\n" +
                "住所: " + ent.address + "\n" +
                "詳細: " + ent.detail + "\n" +
                "TEL: " + ent.tel + "\n\n" +
                "安全度ランク: " + ent.ranking.getRankingStar() + "\n" +
                "指定避難所: " + getBooleanString(ent.isShelter) + "\n" +
                "津波緊急避難所: " + getBooleanString(ent.isTsunami) + "\n" +
                "避難生活施設: " + getBooleanString(ent.isLiving) + "\n" +
                "備考: " + ent.memo;
    }
    private static String getBooleanString(boolean flg) {
        return flg ? "○" : "";
    }
}
