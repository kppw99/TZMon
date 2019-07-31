package com.breezesoftware.skyrim2d.util;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 21.08.2018.
 */
public class BitmapUtil {

    public static Bitmap getBitmapWithTransparentBG(Bitmap src, int bgColor) {
        Bitmap res = src.copy(Bitmap.Config.ARGB_8888, true);
        int width = res.getWidth();
        int height = res.getHeight();

        res.setHasAlpha(true);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixelColor = res.getPixel(i, j);
                if (pixelColor == bgColor) {
                    res.setPixel(i, j, Color.TRANSPARENT);
                }
            }
        }

        return res;
    }
}
