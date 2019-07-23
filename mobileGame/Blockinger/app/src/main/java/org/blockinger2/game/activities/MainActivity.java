package org.blockinger2.game.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.blockinger2.game.R;
import org.blockinger2.game.components.GameState;
import org.blockinger2.game.database.HighscoreOpenHelper;
import org.blockinger2.game.database.ScoreDataSource;
import org.blockinger2.game.engine.Sound;

public class MainActivity extends AppCompatActivity
{
    private final int SCORE_REQUEST = 0x0;
    private ScoreDataSource datasource;
    private SimpleCursorAdapter adapter;
    private int startLevel;
    private Sound sound;

    //==============================================================================================
    // Start to add source code for using TZMON JNI Library by kevin
    //==============================================================================================

    public static final boolean tzmonUse = true;

    static {
        System.loadLibrary("tzMonJNI");
    }

    public native boolean tzmonInitKeyNFlag();
    public native boolean tzmonCheckAppHash();
    public native boolean tzmonSecureUpdate();
    public native boolean tzmonAbusingDetection();
    public native boolean tzmonSyncTimer();

    public native boolean tzmonHidingSetup();
    public native int tzmonGetHKey(String data);

    private int hKey;

    private void setStartLevel(int level) { this.startLevel = (level ^ this.hKey); }
    private int getStartLevel() { return (this.startLevel ^ this.hKey); }

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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, true);

        //==========================================================================================
        // Start to add source code for checking app integrity by kevin
        //==========================================================================================
        if (tzmonUse) {
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

        // Music
        sound = new Sound(this);
        sound.startMusic(Sound.MENU_MUSIC, 0);

        // Database Management
        datasource = new ScoreDataSource(this);
        datasource.open();

        // Use the SimpleCursorAdapter to show the elements in a ListView
        Cursor cursor = datasource.getCursor();
        adapter = new SimpleCursorAdapter(
            this,
            R.layout.list_item_blockinger,
            cursor,
            new String[]{HighscoreOpenHelper.COLUMN_SCORE, HighscoreOpenHelper.COLUMN_PLAYERNAME},
            new int[]{R.id.textview_highscores_score, R.id.textview_highscores_nickname},
            SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        ((ListView) findViewById(R.id.activity_main_listview_highscores)).setAdapter(adapter);

        findViewById(R.id.activity_main_button_resume_game).setOnClickListener((view) -> {
            Intent intent = new Intent(this, GameActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("mode", GameActivity.RESUME_GAME); // Your id
            bundle.putString("playername", ((TextView) findViewById(R.id.activity_main_edittext_player_name)).getText().toString()); // Your id
            intent.putExtras(bundle); // Put your id to your next Intent
            startActivityForResult(intent, SCORE_REQUEST);
        });

        // Create Startlevel dialog
        if (tzmonUse) {
            this.hKey = tzmonGetHKey("startLevel");
            if (hKey == 0x00) {
                alertDialog(this, "잘못된 접근입니다.");
            }
            setStartLevel(1);
        } else {
            startLevel = 1;
        }

        AlertDialog.Builder dialogStartLevel = new AlertDialog.Builder(this);
        dialogStartLevel.setTitle(R.string.dialog_start_level_title);
        dialogStartLevel.setCancelable(false);
        dialogStartLevel.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogStartLevel.setPositiveButton(R.string.start, (dialog, which) -> {
            persistPlayerName();
            Intent intent = new Intent(this, GameActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("mode", GameActivity.NEW_GAME); // Your id
            if (tzmonUse) {
                bundle.putInt("level", getStartLevel());
            } else {
                bundle.putInt("level", startLevel); // Your id
            }
            bundle.putString("playername", ((TextView) findViewById(R.id.activity_main_edittext_player_name)).getText().toString()); // Your id
            intent.putExtras(bundle); // Put your id to your next Intent
            startActivityForResult(intent, SCORE_REQUEST);
        });

        findViewById(R.id.activity_main_button_new_game).setOnClickListener((view) -> {
            View viewStartLevelSelector = getLayoutInflater().inflate(R.layout.view_start_level_selector, null);
            TextView viewStartLevelTextview = viewStartLevelSelector.findViewById(R.id.view_start_level_textview);
            SeekBar viewStartLevelSeekbar = viewStartLevelSelector.findViewById(R.id.view_start_level_seekbar);
            viewStartLevelSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    viewStartLevelTextview.setText(String.valueOf(progress));
                    if (tzmonUse) {
                        setStartLevel(progress);
                    } else {
                        startLevel = progress;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                    //
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                    //
                }
            });

            if (tzmonUse) {
                viewStartLevelSeekbar.setProgress(getStartLevel());
                viewStartLevelTextview.setText(String.valueOf(getStartLevel()));
            } else {
                viewStartLevelSeekbar.setProgress(startLevel);
                viewStartLevelTextview.setText(String.valueOf(startLevel));
            }

            dialogStartLevel.setView(viewStartLevelSelector);
            dialogStartLevel.show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                break;

            case R.id.action_exit:
                GameState.destroy();
                finish();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != SCORE_REQUEST || resultCode != RESULT_OK) {
            return;
        }

        String playerName = data.getStringExtra(getResources().getString(R.string.playername_key));
        long score = data.getLongExtra(getResources().getString(R.string.score_key), 0);

        datasource.open();
        datasource.createScore(score, playerName);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        persistPlayerName();

        sound.pause();
        sound.setInactive(true);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        sound.pause();
        sound.setInactive(true);
        datasource.close();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        sound.release();
        sound = null;
        datasource.close();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        restorePlayerName();

        sound.setInactive(false);
        sound.resume();

        datasource.open();
        Cursor cursor = datasource.getCursor();
        adapter.changeCursor(cursor);

        if (!GameState.isFinished()) {
            findViewById(R.id.activity_main_button_resume_game).setEnabled(true);
            ((Button) findViewById(R.id.activity_main_button_resume_game)).setTextColor(getResources().getColor(R.color.square_error));
        } else {
            findViewById(R.id.activity_main_button_resume_game).setEnabled(false);
            ((Button) findViewById(R.id.activity_main_button_resume_game)).setTextColor(getResources().getColor(R.color.holo_grey));
        }
    }

    private void persistPlayerName()
    {
        String playerName = ((EditText) findViewById(R.id.activity_main_edittext_player_name)).getText().toString();

        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString(getResources().getString(R.string.playername_key), playerName).apply();
    }

    private void restorePlayerName()
    {
        String playerName = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getResources().getString(R.string.playername_key), null);

        ((EditText) findViewById(R.id.activity_main_edittext_player_name)).setText(playerName);
    }
}
