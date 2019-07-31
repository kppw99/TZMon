package com.breezesoftware.skyrim2d;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.breezesoftware.skyrim2d.entity.Actor;
import com.breezesoftware.skyrim2d.entity.Enemy;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 30.08.2018.
 */
public class HealthBar extends Actor {
    private Enemy actor;

    private Rect fullRect;
    private Rect emptyRect;

    private Paint healthBarEmptyPaint;
    private Paint healthBarFullPaint;

    public HealthBar(Context context, Enemy actor, int height) {
        super(context, 0, 0, actor.getName() + "_" + "HealthBar", 0);

        this.actor = actor;

        Bitmap currentBitmap = actor.getCurrentCostume();
        int h = currentBitmap.getHeight();
        int w = currentBitmap.getWidth();

        fullRect = new Rect(- 5,
                h + 5,
                w + 5,
                h + 5 + height);

        emptyRect = new Rect(fullRect);

        healthBarEmptyPaint = new Paint();
        healthBarEmptyPaint.setColor(Color.RED);

        healthBarFullPaint = new Paint();
        healthBarFullPaint.setColor(Color.GREEN);
    }

    public void update() {
        float healthPercent = this.actor.getHealth() / (float) this.actor.getMaxHealth();
        int healthBarWidth = this.emptyRect.right - this.emptyRect.left;
        healthBarWidth *= healthPercent;

        fullRect.right = fullRect.left + healthBarWidth;
    }

    @Override
    public void draw(Canvas canvas) {
        // No parent draw method intentionally
        // thus we pass the null drawable to it

        canvas.drawRect(emptyRect, this.healthBarEmptyPaint);
        canvas.drawRect(fullRect, this.healthBarFullPaint);
    }
}
