package com.example.kaushal.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.media.AudioManager.STREAM_MUSIC;
import static com.example.kaushal.game.R.drawable.original;

public class MainActivity extends AppCompatActivity {
public boolean flag;

    MediaPlayer sdk;

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDSTOP = "stop";

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

    public static void alertDialog(final Activity a, String message){
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //==========================================================================================
        // Start to add source code for checking app integrity by kevin
        //==========================================================================================

        // After enable just one time to get usage access, never use this function.
        // startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 1);

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

        setContentView(R.layout.activity_main);


        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if(mAudioManager.isMusicActive()) {
            Intent i = new Intent(SERVICECMD);
            i.putExtra(CMDNAME , CMDSTOP );
            MainActivity.this.sendBroadcast(i);
        }
       sdk=MediaPlayer.create(this,R.raw.sherlock);


        flag=true;
    }


public void startGame(View view)
{
    Intent i=new Intent(this,Main2Activity.class);
    i.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(i);
}
    public void sound(View view){

        TextView a=(TextView) findViewById(R.id.myText2);

        if(flag==false)
        {
            sdk.setLooping(true);
            sdk.start();
            a.setText("Pause");
            flag=true;
        }
        else
        {
            sdk.pause();

            a.setText("Play");
            flag=false;
        }




        }


    public void quitGame(View V){

        finish();
        System.exit(0);
    }




    @Override
    protected void onStop() {
        super.onStop();
        sdk.pause();
        flag=false;
        TextView b=(TextView) findViewById(R.id.myText2);
        b.setText("Play");


    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView b=(TextView) findViewById(R.id.myText2);
        b.setText("Pause");
        if (sdk != null && !sdk.isPlaying()) {
            sdk.setLooping(true);
            sdk.start();

        }


    }
}
