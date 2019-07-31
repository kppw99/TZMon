package com.breezesoftware.skyrim2d.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.breezesoftware.skyrim2d.Composite;
import com.breezesoftware.skyrim2d.util.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 21.08.2018.
 */
public class Actor extends Composite<Actor> {
    protected Context context;

    float x;
    float y;

    protected String name;

    private int costume;
    private int currentCostume;
    private float scale;
    private List<Bitmap> graphic = new ArrayList<>(10);

    public Actor(Context context, float xPos, float yPos, String name, int outfit) {
        this.x = xPos;
        this.y = yPos;
        this.context = context;
        this.name = name;
        this.costume = outfit;
        this.currentCostume = 0;
        this.scale = 1.0f;

        if (outfit != 0) {
            graphic.add(loadCostume(costume));
        }
    }

    public void goTo(float posX, float posY) {
        this.x = posX;
        this.y = posY;
    }

    public void addCostume(int costume) {
        Bitmap bmp = loadCostume(costume);
        bmp = Bitmap.createScaledBitmap(
                bmp,
                (int) (bmp.getWidth() * this.scale),
                (int) (bmp.getHeight() * this.scale),
                false);

        graphic.add(bmp);
    }

    void setScale(float scale) {
        this.scale = scale;
        updateBitmapsScale();
    }

    private Bitmap loadCostume(int costume) {
        BitmapDrawable drawable = (BitmapDrawable) this.context.getResources().getDrawable(costume);
        return BitmapUtil.getBitmapWithTransparentBG(drawable.getBitmap(), Color.WHITE);
    }

    private void updateBitmapsScale() {
        List<Bitmap> scaled = new ArrayList<>(this.graphic.size());

        for (Bitmap bmp : graphic) {
            bmp = Bitmap.createScaledBitmap(
                    bmp,
                    (int) (bmp.getWidth() * this.scale),
                    (int) (bmp.getHeight() * this.scale),
                    false);

            scaled.add(bmp);
        }

        this.graphic = scaled;
    }

    void setCurrentCostume(int index) {
        currentCostume = index;
    }

    public float getX() {
        return x;
    }

    float getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    Bitmap getBitmap() {
        return this.graphic.get(currentCostume);
    }

    public Context getContext() {
        return context;
    }

    public void update() {
        for (int i = 0; i < this.getChildren().size(); i++) {
            Actor actor = this.getChildren().get(i);
            actor.update();

            if (actor.isCanDelete()) {
                Log.d("SActor", "Removed child " + actor.getName());
                this.removeChild(actor);
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.getBitmap(), this.getX(), this.getY(), null);

        // Translate canvas to make origin on parent Actor position
        canvas.translate(this.x, this.y);
        for (Actor actor : this.getChildren()) {
            actor.draw(canvas);
        }
        canvas.translate(-this.x, -this.y);
    }

    public Bitmap getCurrentCostume() {
        return this.graphic.get(currentCostume);
    }

    PointF getPosition() {
        return new PointF(this.x, this.y);
    }

    private RectF getBoundingRect() {
        Bitmap bitmap = this.getBitmap();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        return new RectF(this.x, this.y, this.x + w, this.y + h);
    }

    public boolean intersectsWith(Actor another) {
        return getBoundingRect().intersect(another.getBoundingRect());
    }
}
