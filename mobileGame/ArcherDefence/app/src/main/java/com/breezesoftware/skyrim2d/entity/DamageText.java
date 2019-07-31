package com.breezesoftware.skyrim2d.entity;

import android.content.Context;
import android.graphics.Paint;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 04.09.2018.
 */
public class DamageText extends Text {

    private static final int UP_SPEED = 2;
    private static final int ALPHA_SPEED = 5;

    DamageText(Context context, float xPos, float yPos, String text) {
        super(context, xPos, yPos, text, null);
    }

    @Override
    public void update() {
        super.update();

        // Moving upwards
        this.y -= UP_SPEED;

        // Become invisible
        this.paint.setAlpha(Math.max(0, this.paint.getAlpha() - ALPHA_SPEED));

        if (this.paint.getAlpha() == 0) {
            this.postForDelete();
        }
    }
}
