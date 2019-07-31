package com.breezesoftware.skyrim2d.util;

import android.media.MediaPlayer;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 26.08.2018.
 */
public class SoundUtil {
    private static final Random rand = new Random(new Date().getTime());

    public static void playRandomSound(List<MediaPlayer> sounds) {
        if (sounds.isEmpty()) {
            return;
        }

        if (sounds.size() == 1) {
            sounds.get(0).start();
            return;
        }

        int soundIndex = Math.abs(rand.nextInt(sounds.size() - 1));
        sounds.get(soundIndex).start();
    }
}
