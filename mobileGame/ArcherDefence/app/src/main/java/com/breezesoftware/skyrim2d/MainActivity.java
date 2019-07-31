package com.breezesoftware.skyrim2d;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int FRAME_RATE = 50;
    public static Point SCREEN_SIZE;

    private GameView gameView;
    private Button playAgainButton;

    private MediaPlayer backgroundMusic;

    class GameThread extends Thread {
        private boolean isRunning = false;

        public void setRunning(boolean running) {
            this.isRunning = running;
        }

        @Override
        public void run() {
            while (isRunning) {
                // Update
                if (gameView.isGameOver()) {
                    pauseGame();
                }

                gameView.update();
                gameView.postInvalidate();

                try {
                    Thread.sleep(1000 / FRAME_RATE);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    };

    // Game thread
    private GameThread gameThread;

    private void startNewGame() {
        gameView.startGame();
        gameThread = new GameThread();
    }

    private void runGame() {
        backgroundMusic.start();
        gameThread.setRunning(true);
        gameThread.start();
    }

    // Trying to pause game thread
    private void pauseGame() {
        boolean retry = true;
        gameThread.setRunning(false);
        backgroundMusic.pause();
        backgroundMusic.seekTo(0);
    }

    //==============================================================================================
    // Start to add source code for using TZMON JNI Library by kevin
    //==============================================================================================

    public static final boolean tzmonUse = true;

    static {
        System.loadLibrary("tzMonJNI");
    }

    public native void tzmonHello();
    public native boolean tzmonInitKeyNFlag();
    public native boolean tzmonCheckAppHash();
    public native boolean tzmonSecureUpdate();
    public native boolean tzmonAbusingDetection();
    public native boolean tzmonSyncTimer();

    public native boolean tzmonHidingSetup();
    public native int tzmonGetHKey(String data);

    public static void alertDialog(Activity a, String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(a);
        alert.setCancelable(false);
        alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                a.moveTaskToBack(true);
                a.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        alert.setMessage(message);
        alert.create().show();
    }

    //==============================================================================================
    // End to add source code for using TZMON JNI Library by kevin
    //==============================================================================================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gameView.getPlayer().fire(new PointF(event.getX(), event.getY()));
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //==========================================================================================
        // Start to add source code for checking app integrity by kevin
        //==========================================================================================

        // After enable just one time to get usage access, never use this function.
        // startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 1);

        if (tzmonUse) {
            tzmonHello();

            Boolean res;
            res = tzmonInitKeyNFlag();
            Log.d("[LOGD] Init Key and Flag ", String.valueOf(res));
            if (!res) {
                alertDialog(this, "tzMon 초기화에 실패하였습니다.");
            }

            res = tzmonCheckAppHash();
            Log.d("[LOGD] App Integrity checking ", String.valueOf(res));
            if (!res) {
                alertDialog(this, "APP 위변조가 탐지되었습니다!");
            }

            res = tzmonSecureUpdate();
            Log.d("[LOGD] Secure Update ", String.valueOf(res));
            if (!res) {
                alertDialog(this, "Secure Update에 실패하였습니다.");
            }

            res = tzmonAbusingDetection();
            Log.d("[LOGD] Abusing Detection ", String.valueOf(res));
            if (!res) {
                alertDialog(this, "Abusing package가 발견되었습니다.");
            }

            res = tzmonSyncTimer();
            Log.d("[LOGD] Sync Timer ", String.valueOf(res));
            if (!res) {
                alertDialog(this, "Timer Sync에 실패하였습니다.");
            }

            res = tzmonHidingSetup();
            Log.d("[LOGD] Hiding Setup ", String.valueOf(res));
            if (!res) {
                alertDialog(this, "Hiding Setup에 실패하였습니다.");
            }
        } else {
            Boolean res;
            res = tzmonInitKeyNFlag();
            Log.d("[LOGD] Init Key and Flag ", String.valueOf(res));
            if (!res) {
                alertDialog(this, "tzMon 초기화에 실패하였습니다.");
            }
        }
        //==========================================================================================
        // Enc to add source code for checking app integrity by kevin
        //==========================================================================================

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        gameView = this.findViewById(R.id.game_view);
        gameView.setGameOverOverlay((ConstraintLayout) findViewById(R.id.fader));

        playAgainButton = this.findViewById(R.id.againButton);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.againButton) {
                    Log.d("mainActivity", "AGAIN");
                    startNewGame();
                    runGame();
                }
            }
        };

        playAgainButton.setOnClickListener(listener);

        Display display = getWindowManager().getDefaultDisplay();
        SCREEN_SIZE = new Point();
        display.getSize(SCREEN_SIZE);

        gameView.setLevelLabel(findViewById(R.id.levelLabel));
        gameView.setMonstersLabel(findViewById(R.id.monstersLabel));
        gameView.setGoldLabel(findViewById(R.id.goldLabel));

        gameView.initLevelManager();

        // Resets all game state
        startNewGame();

        gameView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                runGame();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // Nothing now
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                pauseGame();
            }
        });

        loadBackgroundMusic();
    }

    private void loadBackgroundMusic() {
        backgroundMusic = MediaPlayer.create(this, R.raw.bgm_hard);
        backgroundMusic.setLooping(true);
    }

}
