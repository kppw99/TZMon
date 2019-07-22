package org.blockinger2.game.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;

import org.blockinger2.game.R;
import org.blockinger2.game.components.Controls;
import org.blockinger2.game.components.Display;
import org.blockinger2.game.components.GameState;
import org.blockinger2.game.engine.Sound;
import org.blockinger2.game.engine.WorkThread;
import org.blockinger2.game.fragments.DefeatDialogFragment;
import org.blockinger2.game.views.BlockBoardView;

import java.util.List;

public class GameActivity extends AppCompatActivity
{
    public Sound sound;
    public Controls controls;
    public Display display;
    public GameState game;
    private WorkThread mainThread;
    private DefeatDialogFragment defeatDialog;
    private boolean leftHandedMode;

    public static final int NEW_GAME = 0;
    public static final int RESUME_GAME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_left_handed_mode", false)) {
            setContentView(R.layout.activity_game_alt);
            leftHandedMode = true;
        } else {
            setContentView(R.layout.activity_game);
            leftHandedMode = false;
        }

        // Read starting Arguments
        Bundle bundle = getIntent().getExtras();

//        ActivityManager actMgr = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> processList = actMgr.getRunningAppProcesses();
//
//        Log.d("[LOGD] processList.size() ", String.valueOf(processList.size()));
//        for (int i = 0; i < processList.size(); i++) {
//            Log.d("[LOGD] Running Process: ", processList.get(i).processName);
//        }

        // Create components
        game = (GameState) getLastCustomNonConfigurationInstance();

        if (game == null) {
            // Check if resuming the game or not
            int value = NEW_GAME;

            if (bundle != null) {
                value = bundle.getInt("mode");
            }

            if ((value == NEW_GAME)) {
                game = GameState.getNewInstance(this);
                if (bundle != null) {
                    game.setLevel(bundle.getInt("level"));
                }
            } else {
                game = GameState.getInstance(this);
            }
        }

//        String appPath = null;
//        PackageInfo packageInfo = null;
//        try {
//            packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        appPath = packageInfo.applicationInfo.sourceDir;
//        game.getJNIAppHash(appPath);

        game.reconnect(this);
        defeatDialog = new DefeatDialogFragment();
        controls = new Controls(this);
        display = new Display(this);
        sound = new Sound(this);

        // Initialise components
        if (game.isResumable()) {
            sound.startMusic(Sound.GAME_MUSIC, game.getSongtime());
        }

        sound.loadEffects();

        if (bundle != null) {
            if (bundle.getString("playername") != null) {
                game.setPlayerName(bundle.getString("playername"));
            }
        } else {
            game.setPlayerName(getResources().getString(R.string.anonymous));
        }

        defeatDialog.setCancelable(false);

        if (!game.isResumable()) {
            gameOver(game.getScore(), game.getTimeString(), game.getAPM());
        }

        // Register button callback methods
        findViewById(R.id.button_pause).setOnClickListener(view -> GameActivity.this.finish());

        findViewById(R.id.button_right).setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.rightButtonPressed();
                findViewById(R.id.button_right).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.rightButtonReleased();
                findViewById(R.id.button_right).setPressed(false);
            }

            return true;
        });

        findViewById(R.id.button_left).setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.leftButtonPressed();
                findViewById(R.id.button_left).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.leftButtonReleased();
                findViewById(R.id.button_left).setPressed(false);
            }

            return true;
        });

        findViewById(R.id.button_soft_drop).setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.downButtonPressed();
                (findViewById(R.id.button_soft_drop)).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.downButtonReleased();
                (findViewById(R.id.button_soft_drop)).setPressed(false);
            }

            return true;
        });

        (findViewById(R.id.button_hard_drop)).setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.dropButtonPressed();
                (findViewById(R.id.button_hard_drop)).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                (findViewById(R.id.button_hard_drop)).setPressed(false);
            }

            return true;
        });

        ImageButton buttonRotateRight = findViewById(R.id.button_rotate_right);
        if (buttonRotateRight != null) {
            (findViewById(R.id.button_rotate_right)).setOnTouchListener((view, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.rotateRightPressed();
                    (findViewById(R.id.button_rotate_right)).setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    (findViewById(R.id.button_rotate_right)).setPressed(false);
                }

                return true;
            });
        }

        ImageButton buttonRotateLeft = findViewById(R.id.button_rotate_left);
        if (buttonRotateLeft != null) {
            (findViewById(R.id.button_rotate_left)).setOnTouchListener((view, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controls.rotateLeftPressed();
                    (findViewById(R.id.button_rotate_left)).setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    (findViewById(R.id.button_rotate_left)).setPressed(false);
                }

                return true;
            });
        }

        ((BlockBoardView) findViewById(R.id.activity_game_view_board)).init();
        ((BlockBoardView) findViewById(R.id.activity_game_view_board)).setHost(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        sound.pause();
        sound.setInactive(true);
        game.setRunning(false);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        sound.pause();
        sound.setInactive(true);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        game.setSongtime(sound.getSongtime());
        sound.release();
        sound = null;
        game.disconnect();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        sound.resume();
        sound.setInactive(false);

        // Check for changed layout
        boolean tempswap = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("pref_left_handed_mode", false);

        if (leftHandedMode != tempswap) {
            leftHandedMode = tempswap;

            if (leftHandedMode) {
                setContentView(R.layout.activity_game_alt);
            } else {
                setContentView(R.layout.activity_game);
            }
        }

        game.setRunning(true);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance()
    {
        return game;
    }

    /*
     * Called by BlockBoardView upon completed creation.
     */
    public void startGame(BlockBoardView caller)
    {
        mainThread = new WorkThread(this, caller.getHolder());
        mainThread.setFirstTime(false);
        game.setRunning(true);
        mainThread.setRunning(true);
        mainThread.start();
    }

    /*
     * Called by GameState upon defeat.
     */
    public void putScore(long score)
    {
        String playerName = game.getPlayerName();

        if (playerName == null || playerName.equals("")) {
            playerName = getResources().getString(R.string.anonymous);
        }

        Intent intent = new Intent();
        intent.putExtra(getResources().getString(R.string.playername_key), playerName);
        intent.putExtra(getResources().getString(R.string.score_key), score);
        setResult(MainActivity.RESULT_OK, intent);

        finish();
    }

    /*
     * Show the defeat dialog upon game over.
     */
    public void gameOver(long score, String gameTime, int apm)
    {
        defeatDialog.setData(score, gameTime, apm);
        defeatDialog.show(getSupportFragmentManager(), "hamster");
    }

    /*
     * Called by BlockBoardView upon destruction.
     */
    public void destroyWorkThread()
    {
        boolean retry = true;
        mainThread.setRunning(false);

        while (retry) {
            try {
                mainThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //
            }
        }
    }
}
