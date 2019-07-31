package com.breezesoftware.skyrim2d.monster;

import android.content.Context;
import android.graphics.Bitmap;

import com.breezesoftware.skyrim2d.MainActivity;
import com.breezesoftware.skyrim2d.entity.Enemy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 05.09.2018.
 */
public class MonsterFactory {
    private Context context;

    private String monsterName = "Undefined";
    private int healthMin = 1;
    private int healthMax = 1;
    private List<Integer> drawables = new ArrayList<>();
    private int speedMin = 1;
    private int speedMax = 1;
    private int goldMin = 1;
    private int goldMax = 1;
    private float scale = 1.0f;

    private Random rand = new Random(new Date().getTime());

    public MonsterFactory(Context context, String monsterName) {
        this.context = context;
        this.monsterName = monsterName;
    }

    public Enemy create() {
        Enemy enemy = new Enemy(context, 0, 0, monsterName,
                drawables.get(0), getSpeed(), getHealth(), scale);

        enemy.setGold(getGold());

        for (int i = 1; i < drawables.size(); i++) {
            enemy.addCostume(drawables.get(i));
        }

        setRandomPosition(enemy);

        return enemy;
    }

    private int getGold() {
        return Math.abs(rand.nextInt()) % goldMax + goldMin;
    }

    private int getSpeed() {
        return Math.abs(rand.nextInt()) % speedMax + speedMin;
    }

    private int getHealth() {
        return Math.abs(rand.nextInt()) % healthMax + healthMin;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    public void setHealthMin(int healthMin) {
        this.healthMin = healthMin;
    }

    public void setHealthMax(int healthMax) {
        this.healthMax = healthMax;
    }

    public void setHealth(int healthMin, int healthMax) {
        this.healthMin = healthMin;
        this.healthMax = healthMax;
    }

    public void setSpeedMin(int speedMin) {
        this.speedMin = speedMin;
    }

    public void setSpeedMax(int speedMax) {
        this.speedMax = speedMax;
    }

    public void setSpeed(int speedMin, int speedMax) {
        this.speedMin = speedMin;
        this.speedMax = speedMax;
    }

    public void setGoldMin(int goldMin) {
        this.goldMin = goldMin;
    }

    public void setGoldMax(int goldMax) {
        this.goldMax = goldMax;
    }

    public void setGold(int goldMin, int goldMax) {
        this.goldMin = goldMin;
        this.goldMax = goldMax;
    }

    public void addDrawable(int drawable) {
        this.drawables.add(drawable);
    }

    private void setRandomPosition(Enemy enemy) {
        Random rand = new Random(new Date().getTime());

        int xOffset = MainActivity.SCREEN_SIZE.x + Math.abs(rand.nextInt() % 200);
        int yOffset = 100 + Math.abs(rand.nextInt()) % (MainActivity.SCREEN_SIZE.y - 300);

        enemy.goTo(xOffset, yOffset);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
