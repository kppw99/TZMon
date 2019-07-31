package com.breezesoftware.skyrim2d.level;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Xml;

import com.breezesoftware.skyrim2d.MainActivity;
import com.breezesoftware.skyrim2d.R;
import com.breezesoftware.skyrim2d.entity.Enemy;
import com.breezesoftware.skyrim2d.monster.MonsterManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 22.08.2018.
 */
public class LevelManager {
    private MonsterManager monsterManager;

    private List<LevelEntry> levels;
    private List<Enemy> enemies;

    private int currentLevel = 0;

    private Context context;

    public LevelManager(Context context) {
        this.context = context;

        monsterManager = new MonsterManager(context);
        monsterManager.init();

        this.loadLevels();
        this.loadCurrentLevel();
    }

    private void loadCurrentLevel() {
        if (currentLevel < this.levels.size()) {
            this.enemies = this.levels.get(currentLevel).getEnemies();
        }
    }

    /**
     * Load game levels from resource files
     */
    private void loadLevels() {
        LevelParser levelParser = new LevelParser(monsterManager);
        this.levels = levelParser.parse(context.getResources().getXml(R.xml.levels));
    }

    public int getCurrentLevel() {
        return this.currentLevel + 1;
    }

    public int getCurrentLevelMonsterCount() {
        return this.enemies.size();
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
        loadCurrentLevel();
    }

    public List<Enemy> getEnemies() {
        return this.enemies;
    }

    public void nextLevel() {
        if (currentLevel < this.levels.size()) {
            currentLevel++;
            loadCurrentLevel();
        }
    }

    public boolean isLastLevel() {
        return currentLevel == this.levels.size();
    }
}
