package com.breezesoftware.skyrim2d.level;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;

import com.breezesoftware.skyrim2d.R;
import com.breezesoftware.skyrim2d.monster.MonsterManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 05.09.2018.
 */
public class LevelParser {

    private MonsterManager monsterManager;

    public LevelParser(MonsterManager monsterManager) {
        this.monsterManager = monsterManager;
    }

    public List<LevelEntry> parse(XmlResourceParser parser) {
        try {
            // Pass document root
            parser.next();
            parser.next();
            return readLevelPack(parser);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            parser.close();
        }

        return new ArrayList<>();
    }

    /**
     * Reads a level pack which contains levelEntries
     * @param levelPack
     * @return
     */
    private List<LevelEntry> readLevelPack(XmlPullParser levelPack) throws IOException, XmlPullParserException {
        List<LevelEntry> entries = new ArrayList<>();

        // Root tag must be LevelPack
        levelPack.require(XmlPullParser.START_TAG, null, "LevelPack");

        while (levelPack.next() != XmlPullParser.END_TAG) {
            if (levelPack.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = levelPack.getName();
            if (name.equals("Level")) {
                entries.add(readLevelEntry(levelPack));
            } else {
                skipTag(levelPack);
            }
        }


        return entries;
    }

    /**
     * Parses a levelEntry
     *
     * @param parser
     * @return
     */
    private LevelEntry readLevelEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<MonsterEntry> monsters = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, null, "Level");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();

            if (tagName.equals("Monster")) {
                monsters.add(readMonsterEntry(parser));
            } else {
                skipTag(parser);
            }
        }

        return new LevelEntry(monsters);
    }

    private MonsterEntry readMonsterEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "Monster");

        String name = parser.getAttributeValue(null, "name");
        int count = Integer.valueOf(parser.getAttributeValue(null, "value"));

        skipTag(parser);

        return new MonsterEntry(monsterManager.getFactory(name), count);
    }

    private static void skipTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;

        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
