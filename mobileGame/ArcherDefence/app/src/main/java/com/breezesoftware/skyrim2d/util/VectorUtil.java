package com.breezesoftware.skyrim2d.util;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 31.08.2018.
 */
public class VectorUtil {
    public static float getDistanceBetween(PointF p1, PointF p2) {
        float x1 = p1.x;
        float y1 = p1.y;
        float x2 = p2.x;
        float y2 = p2.y;

        return (float) Math.sqrt(Math.pow(x2 - x1, 2) - Math.pow(y2 - y1, 2));
    }

    public static PointF getVectorBetween(PointF source, PointF dest) {
        return new PointF(dest.x - source.x, dest.y - source.y);
    }

    public static float getVectorLength(PointF vector) {
        return (float) Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2));
    }

    public static PointF normalize(PointF vector) {
        float length = getVectorLength(vector);
        return new PointF(vector.x / length, vector.y / length);
    }

    public static PointF multiplyVector(PointF vector, int multiplier) {
        return new PointF(vector.x * multiplier, vector.y * multiplier);
    }

    public static PointF translateByVector(PointF position, PointF vector) {
        return new PointF(position.x + vector.x, position.y + vector.y);
    }

    public static boolean isPointInRect(PointF point, Rect rect) {
        return point.x >= rect.left && point.x <= rect.right &&
                point.y >= rect.top && point.y <= rect.bottom;
    }

}
