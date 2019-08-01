package com.example.kaushal.game;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.view.MotionEvent;



/**
 * Created by Kaushal on 1/23/2017.
 */

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.kaushal.game.GameView;
import com.example.kaushal.game.Global;

import java.util.concurrent.atomic.AtomicInteger;

import javax.microedition.khronos.opengles.GL10;

import static com.example.kaushal.game.Global.context;

public class GameActivity2 extends Activity implements SensorEventListener {

    private SensorManager sensorManager;

    GameView gameView; String mm;

    MediaPlayer sdk,pk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        gameView = new GameView(this);

        setContentView(gameView);
        sdk= MediaPlayer.create(this,R.raw.animal);
        pk= MediaPlayer.create(this,R.raw.count);
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }



    @Override
    protected void onResume() {
        super.onResume();
        gameView.onResume();
        if (sdk != null && !sdk.isPlaying()) {
            sdk.setLooping(true);
            sdk.start();


        }




    }

    @Override
    protected void onStop() {
        super.onStop();
        sdk.pause();

    }
    @Override
    protected void onPause() {
        super.onPause();
        gameView.onPause();
        sdk.pause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        int height = Math.round(Global.display.getHeight() * Global.getProportionateHeight(0.25f));
        int excludedArea = Global.display.getHeight() - height;
        if(y > excludedArea){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(x < Global.display.getWidth() / 3){
                        Global.PLAYER_ACTION =Global.BREAKS_PRESSED;
                    }else if(x > (Global.display.getWidth() / 3) * 2){
                        Global.PLAYER_ACTION = Global.ACCELERATOR_PRESSED;
                    }
                    Global.TRAFFIC_ACTION = Global.ACCELERATOR_PRESSED;
                    break;
                case MotionEvent.ACTION_UP:
                    Global.PLAYER_ACTION = Global.CONTROL_RELEASED;
                    Global.TRAFFIC_ACTION = Global.ACCELERATOR_PRESSED;
                    break;
            }
        }
        return false;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            Global.SENSORE_ACCELEROMETER_X = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}


