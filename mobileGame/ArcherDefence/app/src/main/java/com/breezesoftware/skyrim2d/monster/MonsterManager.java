package com.breezesoftware.skyrim2d.monster;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.breezesoftware.skyrim2d.R;

import java.util.HashMap;
import java.util.Map;

public class MonsterManager  {
    private Context context;
    private Map<String, MonsterFactory> monsterFactories = new HashMap<>();

    public MonsterManager(Context context) {
        this.context = context;
    }

    // Load all monsters
    public void init() {
        initOrc();
        initHighOrc();
        initBabyOrc();
    }

    public MonsterFactory getFactory(String name) {
        if (monsterFactories.get(name) == null) {
            throw new NotFoundException();
        }

        return monsterFactories.get(name);
    }


    // INIT MONSTERS

    /**
     * Orc monster
     */
    private void initOrc() {
        MonsterFactory orcFactory = new MonsterFactory(context, "orc");
        orcFactory.setGold(0, 1);
        orcFactory.setHealth(1, 3);
        orcFactory.setSpeed(1, 3);
        orcFactory.addDrawable(R.drawable.monster);
        orcFactory.addDrawable(R.drawable.monster_dead);

        monsterFactories.put("orc", orcFactory);
    }

    private void initBabyOrc() {
        MonsterFactory factory = new MonsterFactory(context, "babyOrc");
        factory.setGold(0, 2);
        factory.setHealth(1, 1);
        factory.setSpeed(3, 5);
        factory.addDrawable(R.drawable.monster);
        factory.addDrawable(R.drawable.monster_dead);
        factory.setScale(0.5f);

        monsterFactories.put("babyOrc", factory);
    }

    private void initHighOrc() {
        MonsterFactory factory = new MonsterFactory(context, "highOrc");
        factory.setGold(0, 10);
        factory.setHealth(5, 10);
        factory.setSpeed(1, 2);
        factory.addDrawable(R.drawable.monster);
        factory.addDrawable(R.drawable.monster_dead);
        factory.setScale(2.0f);

        monsterFactories.put("highOrc", factory);
    }
}
