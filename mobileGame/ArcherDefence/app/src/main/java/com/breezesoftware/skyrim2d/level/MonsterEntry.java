package com.breezesoftware.skyrim2d.level;

import com.breezesoftware.skyrim2d.monster.MonsterFactory;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 05.09.2018.
 */
public class MonsterEntry {
    private MonsterFactory factory;
    private int count;

    MonsterEntry(MonsterFactory factory, int count) {
        this.factory = factory;
        this.count = count;
    }

    public MonsterFactory getFactory() {
        return factory;
    }

    public int getCount() {
        return count;
    }
}
