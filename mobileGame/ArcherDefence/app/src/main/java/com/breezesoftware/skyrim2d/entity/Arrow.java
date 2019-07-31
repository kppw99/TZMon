package com.breezesoftware.skyrim2d.entity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;

import com.breezesoftware.skyrim2d.R;
import com.breezesoftware.skyrim2d.util.VectorUtil;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 31.08.2018.
 */
public class Arrow extends Actor {
    private static final int ARROW_DRAWABLE = R.drawable.arrow;

    private PointF destination;
    private PointF vector;

    private Context context;

    private int speed = 20;
    private int damage = 1;

    private boolean isStatic = false;

    Arrow(Context context, PointF firePoint, PointF destination) {
        super(context, firePoint.x, firePoint.y, "Arrow", ARROW_DRAWABLE);

        this.context = context;
        this.destination = destination;

        // Fire vector
        this.vector = VectorUtil.getVectorBetween(firePoint, destination);
        this.vector = VectorUtil.normalize(this.vector);
    }

    @Override
    public void update() {
        super.update();

        // Static arrow does not move
        if (isStatic) {
            return;
        }

        PointF currentPos = this.getPosition();
        currentPos = VectorUtil.translateByVector(currentPos,
                VectorUtil.multiplyVector(vector, speed));

        this.goTo(currentPos.x, currentPos.y);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDamage() {
        return damage;
    }

    void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }
}
