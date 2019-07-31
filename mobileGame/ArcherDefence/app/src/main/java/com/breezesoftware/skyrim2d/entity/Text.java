package com.breezesoftware.skyrim2d.entity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.lang.reflect.Type;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 04.09.2018.
 */
public class Text extends Actor {

    Paint paint;

    Text(Context context, float xPos, float yPos, String text, Paint paint) {
        super(context, xPos, yPos, text, 0);

        if (paint == null) {
            this.paint = getDefaultPaint();
        } else {
            this.paint = paint;
        }
    }

    static Paint getDefaultPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(30.0f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        return paint;
    }

    @Override
    public void draw(Canvas canvas) {
        // No super draw
        canvas.drawText(name, x, y, paint);
    }
}
