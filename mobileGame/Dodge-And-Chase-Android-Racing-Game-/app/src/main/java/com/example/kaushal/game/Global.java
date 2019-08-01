package com.example.kaushal.game;

/**
 * Created by Kaushal on 1/23/2017.
 */
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Display;

public class Global {
    // Constants

    public static final int GAME_THREAD_FPS_SLEEP = (1000 / 60);
    public static final int CONTROL_RELEASED = 0;
    public static final int ACCELERATOR_PRESSED = 1;
    public static final int BREAKS_PRESSED = 2;
public static  int score=0;
    // Variables
    public static Context context;

    public static int ROAD2 = R.drawable.besty;
    public static int ROAD3 = R.drawable.best;
    public static int CAR = R.drawable.cars;
    public static int CAR2 = R.drawable.carss;
    public static int CAR3 = R.drawable.car;
    public static int TRAFFIC=R.drawable.traffic;
    public static int TRAFIC=R.drawable.trafic;
    public static int TRAFICC=R.drawable.traficc;
    public static int EVIL=R.drawable.evil;
    public static int ACCELERATOR = R.drawable.accelerator;
    public static int BREAKS = R.drawable.breaks;
    public static int GAME_SCREEN_WIDTH = 0;
    public static int GAME_SCREEN_HEIGHT = 0;
    public static int PLAYER_ACTION = 0;
    public static int TRAFFIC_ACTION = 0;
    public static double SENSORE_ACCELEROMETER_X = 0.0;
    public static Display display;


    public static float getProportionateHeight(float width) {

        float ratio = (float) GAME_SCREEN_WIDTH / GAME_SCREEN_HEIGHT;
        float height = ratio * width;
        return height;
    }
}

