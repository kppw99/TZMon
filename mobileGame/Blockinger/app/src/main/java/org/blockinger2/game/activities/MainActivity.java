package org.blockinger2.game.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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
import android.widget.Toast;

import org.blockinger2.game.R;
import org.blockinger2.game.components.GameState;
import org.blockinger2.game.database.HighscoreOpenHelper;
import org.blockinger2.game.database.ScoreDataSource;
import org.blockinger2.game.engine.Sound;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;

import static android.os.StrictMode.setThreadPolicy;
import static java.util.Objects.*;

public class MainActivity extends AppCompatActivity
{
    private final int SCORE_REQUEST = 0x0;
    private ScoreDataSource datasource;
    private SimpleCursorAdapter adapter;
    private int startLevel;
    private int hKey;
    private Sound sound;

    static {
        System.loadLibrary("MainJNI");
    }

    public native boolean jniapphashtest();
    public native int jniHidingKey(String data);
    public native void jniSocket();

    public String getAppPath() {
        PackageInfo packageInfo = null;

        try {
            packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return requireNonNull(packageInfo).applicationInfo.sourceDir;
    }

    private boolean isAppRunning(Context context, String name){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++){
            if(procInfos.get(i).processName.equals(name)){
                return true;
            }
        }

        return false;
    }

    private String getForegroundPackageName(Context context) {
        String packageName = null;
        UsageStatsManager usageStatsManager = (UsageStatsManager)getSystemService(context.USAGE_STATS_SERVICE);
        final long endTime = System.currentTimeMillis();
        final long beginTime = endTime - (1000 * 1000);
        final UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                packageName = event.getPackageName();

                Log.d("[LOGD] packageName is ", packageName);
            }
        }
        return packageName;
    }

    private Socket socket;
    private OutputStream out;

    public void setSocket(String ip, int port)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        setThreadPolicy(policy);

        try{
            socket = new Socket(ip, port);
            out = socket.getOutputStream();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public void closeSocket() throws IOException {
        String msg = "quit";
        out.write(msg.getBytes());
        socket.close();
    }

    public void writeMsg(String msg) throws IOException {
        out.write(msg.getBytes());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, true);

        //==========================================================================================
        // Start to add source code for checking app integrity by kevin
        //==========================================================================================
        // startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 1);

//        try {
//            setSocket("163.152.127.108", 9999);
//            writeMsg("kevin");
//            closeSocket();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        jniSocket();
//
//        getSignature();

//        UsageStatsManager usm = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
//        long time = System.currentTimeMillis();
//        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 10000*10000, time);
//
//        Log.d("[LOGD] processList.size() ", String.valueOf(appList.size()));
//        for (int i = 0; i < appList.size(); i++) {
//            Log.d("[LOGD] Running app: ", appList.get(i).getPackageName());
//        }

//        final Runtime rt = Runtime.getRuntime();
//        try
//        {
//            rt.exec("su");
//        }
//
//        catch(IOException e)
//        {
//            e.printStackTrace();
//        }

//        ActivityManager actMgr = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> processList = actMgr.getRunningAppProcesses();
//
//        Log.d("[LOGD] processList.size() ", String.valueOf(processList.size()));
//        for (int i = 0; i < processList.size(); i++) {
//            Log.d("[LOGD] Running Process: ", processList.get(i).processName);
//        }
//
//        //org.scoutant.blokish
//        //String appName = "org.scoutant.blokish";
//        //String appName = "com.bubbleshooter";
//        String appName = "org.blockinger2.game";
//        if (isAppRunning(this, appName) == true) {
//            Log.d("[LOGD] isAppRunning ", appName + " is running");
//        } else {
//            Log.d("[LOGD] isAppRunning ", appName + " is not running");
//        }
//
//        getForegroundPackageName(this);


//        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
//
//        Log.d("[LOGD] list.size() ", String.valueOf(list.size()));
//        for(int i = 0; i < list.size(); i++){
//            Toast.makeText(getApplicationContext(), list.get(i).processName, Toast.LENGTH_SHORT).show();
//        }

//        String packageName = actMgr.getRunningTasks(100).get(1).topActivity.getPackageName();
//        Log.d("[LOGD] ", packageName);
//
//        List<ActivityManager.RunningTaskInfo> taskList = actMgr.getRunningTasks(100);
//        Log.d("[LOGD] taskList.size() ", String.valueOf(taskList.size()));
//        for (int i = 0; i < taskList.size(); i++) {
//            Log.d("[LOGD] Running Task: ", taskList.get(i).topActivity.getPackageName());
//        }

//        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        final List pkgAppsList = this.getPackageManager().queryIntentActivities( mainIntent, 0);
//
//        int size = pkgAppsList.size();
//        Log.d("[LOGD] packgeAppList.size() ", String.valueOf(size));
//        for (int i = 0; i < size; i++) {
//            Log.d("[LOGD] Running Package ", pkgAppsList.get(i).toString());
//        }

//        try {
//            getAppHash();
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }

        //String appPath = getAppPath();
        //Log.d("[LOGD] new app path: ", appPath);

        boolean res = jniapphashtest();
        if (res == false) {
            Log.d("[LOGD] App Integrity checking ", String.valueOf(res));

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setCancelable(false);
            alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            alert.setMessage("APP 위변조가 탐지되었습니다!");
            alert.show();

        } else {
            Log.d("[LOGD] App Integrity checking ", String.valueOf(res));
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
//        startLevel = 1;
        this.hKey = jniHidingKey("startLevel");
        setStartLevel(1);
        AlertDialog.Builder dialogStartLevel = new AlertDialog.Builder(this);
        dialogStartLevel.setTitle(R.string.dialog_start_level_title);
        dialogStartLevel.setCancelable(false);
        dialogStartLevel.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogStartLevel.setPositiveButton(R.string.start, (dialog, which) -> {
            persistPlayerName();
            Intent intent = new Intent(this, GameActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("mode", GameActivity.NEW_GAME); // Your id
//            bundle.putInt("level", startLevel); // Your id
            bundle.putInt("level", getStartLevel());
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
//                    startLevel = progress;
                    setStartLevel(progress);
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

//            viewStartLevelSeekbar.setProgress(startLevel);
//            viewStartLevelTextview.setText(String.valueOf(startLevel));
            viewStartLevelSeekbar.setProgress(getStartLevel());
            viewStartLevelTextview.setText(String.valueOf(getStartLevel()));
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

    public String getSignature()
    {
        Context context = getApplicationContext();
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        String sign = null;

        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature certSignature = packageInfo.signatures[0];
            MessageDigest msgDigest = MessageDigest.getInstance("SHA1");
            msgDigest.update(certSignature.toByteArray());
            sign = Base64.encodeToString(msgDigest.digest(), Base64.DEFAULT);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Log.d("sign", sign.toString());

        return sign;
    }

    public String getAppHash() throws PackageManager.NameNotFoundException, IOException, NoSuchAlgorithmException {
        String appPath = null;
        String appHash = null;
        int numBytes = 0;

        PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA);
        appPath = packageInfo.applicationInfo.sourceDir;

        FileInputStream is = new FileInputStream(appPath);

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();

        byte[] bytes = new byte[2048];
        while ((numBytes = is.read(bytes)) != -1) {
            sha256.update(bytes, 0, numBytes);
        }

        byte[] digest = sha256.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }

        appHash = sb.toString();

        Log.d("APP_HASH", appHash);

        return appHash;
    }

    public void setStartLevel(int level)
    {
        this.startLevel = (level ^ this.hKey);
    }

    public int getStartLevel()
    {
        return (this.startLevel ^ this.hKey);
    }
}
