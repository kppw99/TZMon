package com.breezesoftware.skyrim2d.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.breezesoftware.skyrim2d.MainActivity;
import com.breezesoftware.skyrim2d.R;
import com.breezesoftware.skyrim2d.util.VectorUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 31.08.2018.
 */
public class Player extends Actor {
    private static final int PLAYER_DRAWABLE = R.drawable.archer;

    private List<Arrow> arrows = new Vector<>(10);

    private Date lastFireTime;

    private int level = 1;
    private int gold = 0;

    // Player stats
    private int strength        = 1;
    private int agility         = 1;
    private int dexterity       = 1;
    private int luck            = 1;
    private int intelligence    = 1;

    private float weaponDamage = 1.0f;

    private Random rand = new Random(new Date().getTime());

    public Player(Context context, Point position) {
        super(context, position.x, position.y, "Player", PLAYER_DRAWABLE);

        Bitmap playerDrawable = this.getBitmap();
        int w = playerDrawable.getWidth();
        int h = playerDrawable.getHeight();

        PointF arrowOffset = new PointF(w / 2, h / 2.75f);

        // Create an arrow on bow
        Arrow arrow = new Arrow(context, arrowOffset , this.getPosition());
        // No moving, just on bow arrow
        arrow.setStatic(true);

        this.addChild(arrow);

        this.lastFireTime = new Date();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        for (int i = 0; i < arrows.size(); i++) {
            arrows.get(i).draw(canvas);
        }
    }

    @Override
    public void update() {
        super.update();

        for (int i = 0; i < arrows.size(); i++) {
            Arrow arrow = arrows.get(i);

            arrow.update();

            // Remove arrows that are outside of screen
            if (!VectorUtil.isPointInRect(arrow.getPosition(), new Rect(
                    0, 0, MainActivity.SCREEN_SIZE.x, MainActivity.SCREEN_SIZE.y)))
            {
                Log.d("Player", "Arrow deleted");
                arrows.remove(i);
            }
        }
    }

    /**
     * Fire an arrow to the destination point
     *
     * @param destination Arrow destination point
     */
    public void fire(PointF destination) {
        Date now = new Date();

        if (now.getTime() - lastFireTime.getTime() < getFireDelay()) {
            return;
        }

        Arrow arrow = new Arrow(getContext(), getPosition(), destination);
        arrow.setDamage(Math.round(generateDamage()));
        arrows.add(arrow);

        Log.d("PlayerArcher", "Generated arrow with damage " + arrow.getDamage());

        this.lastFireTime = now;
    }

    public List<Arrow> getArrows() {
        return arrows;
    }

    public void removeArrow(Arrow arrow) {
        arrows.remove(arrow);
    }

    /**
     * Returns the player fire speed
     *
     * @return Number of arrow player can fire every second
     */
    private float getFireSpeed() {
        return 0.5f * agility * 2.0f + strength * 1.5f;
    }

    /**
     * Returns player damage dispersion. Dispersion based on player dexterity
     *
     * @return Damage dispersion
     */
    private float getDamageDispersion() {
        return Math.max(0, getDamage() / 2.0f - (dexterity * 0.02f) * getDamage() / 2.0f);
    }

    /**
     * Returns a damage of a player.
     *
     * @return Damage of a player.
     */
    private float getDamage() {
        return weaponDamage + strength * 0.5f;
    }

    /**
     * Generates random number using damage and damage dispersion
     *
     * @return Some random damage number
     */
    private float generateDamage() {
        float from = -getDamageDispersion();
        float to = -from;

        Log.d("PlayerArcher", "Dispersion: " + getDamageDispersion());
        float dispersion = from + (rand.nextFloat() % Math.abs(to - from));

        return getDamage() + dispersion;
    }

    /**
     * Returns the player fire delay in milliseconds
     * @return Fire delay
     */
    private float getFireDelay() {
        return 1000.0f / getFireSpeed();
    }

    public Date getLastFireTime() {
        return lastFireTime;
    }

    public int getStrength() {
        return strength;
    }

    public int getAgility() {
        return agility;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getLuck() {
        return luck;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void addGold(int gold) {
        this.gold += gold;
    }

    public void removeGold(int gold) {
        this.gold = Math.max(0, this.gold - gold);
    }
}
