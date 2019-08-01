package com.example.kaushal.game;

/**
 * Created by Kaushal on 1/23/2017.
 */

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GameView extends GLSurfaceView {

    private GameRenderer renderer;

    public GameView(Context context) {
        super(context);

        renderer = new GameRenderer();

        this.setRenderer(renderer);
    }

}

