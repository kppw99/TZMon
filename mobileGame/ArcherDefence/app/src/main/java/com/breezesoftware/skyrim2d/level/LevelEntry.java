package com.breezesoftware.skyrim2d.level;

import com.breezesoftware.skyrim2d.entity.Enemy;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 05.09.2018.
 */
public class LevelEntry {
    private List<MonsterEntry> monsters;

    public LevelEntry(List<MonsterEntry> monsters) {
        this.monsters = monsters;
    }

    /**
     * Returns monsters for a level
     * @return
     */
    public List<Enemy> getEnemies() {
        List<Enemy> enemies = new ArrayList<>();

        for (MonsterEntry entry : monsters) {
            for (int i = 0; i < entry.getCount(); i++) {
                enemies.add(entry.getFactory().create());
            }
        }

        return enemies;
    }
}
