package com.example.kaushal.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;


public class Main2Activity extends Activity {
    MediaPlayer sdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Global.display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        sdk = MediaPlayer.create(this, R.raw.hell);

        Global.context = getApplicationContext();

    }

    public void race(View v) {
				/* Start Game */
        Intent intent = new Intent(Main2Activity.this, GameActivity2.class);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        Main2Activity.this.startActivity(intent);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Main2Activity.this,MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        Main2Activity.this.startActivity(intent);
        finish();

    }

    @Override
    protected void onStop() {
        super.onStop();
        sdk.pause();

    }

    @Override
    protected void onResume() {
        if (sdk != null && !sdk.isPlaying()) {
            sdk.setLooping(true);
            sdk.start();

        }
        super.onResume();

    }

}

