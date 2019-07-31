/**
 * Filename: Main.java
 * Purpose: Entry point activity for the app on the phone. 
 * Handles pause, resume, and saving the game state of the game.
 */

package com.learn.gamelearn;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class Main extends Activity {
	/** Called when the activity is first created. */
	//private variables used for various purposes. 
	GameView view;
	GameLoopThread loop;
	DatabaseHelper dbh;
	SQLiteDatabase db;
	ContentValues values;
	Editor editor;
	int initialX = 50;
	int x = 0;
	int y = 0;
	int speed = 0;
	String serializedCollision = "";
	boolean isBackPressed = false;
	int score = 0;
	private AudioManager manager;
	private int mediaVolume;
	ArrayList<Integer> highScore = new ArrayList<Integer>();

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

	private int hKey;

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

	//onCreate method or the entry point of the app.
	@Override
	public void onCreate(Bundle savedInstanceState) {
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

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		view = new GameView(this);
		setContentView(view);

		dbh = new DatabaseHelper(this);
		/*db = dbh.getReadableDatabase();
		Cursor c = db.query("gamedata", new String[] {"alien_x", "alien_Y", "alien_speed"}, null, null, null, null, null);
		if (c != null){
			while(c.moveToNext()){
				x = c.getInt(0);
				y = c.getInt(1);
				speed = c.getInt(2);
			}
		}*/
		manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		mediaVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		loop = new GameLoopThread(view);
	}

	//onPause method
	@Override
	public void onPause(){
		super.onPause();
		finish();
		if (!isBackPressed){
			db = dbh.getWritableDatabase();
			values = new ContentValues();
			//get the game state from gameView class
			x = view.getInitialX();
			y = view.getInitialY();
			values.put("alien_x", x);
			values.put("alien_y", y);
			speed = view.getSpeed();
			//store the game state in the database
			values.put("alien_speed", speed);
			values.put("collision", view.getSerializedCollision());
			values.put("current_score", view.getCurrentScore());

			db.insert("gamedata", null, values);
			db.close();
		}

	}

	//overriden onResume method
	@Override
	public void onResume(){
		super.onResume();
		db = dbh.getReadableDatabase();
		//define a cursor for the database
		Cursor c = db.query("gamedata", 
				new String[] {"alien_x", "alien_y", "alien_speed","collision","current_score"}, 
				null, null, null, null, null);
		//get the last saved game state from the database table
		if (c != null){
			while(c.moveToNext()){
				x = c.getInt(0);
				y = c.getInt(1);
				speed = c.getInt(2);
				serializedCollision = c.getString(3);
				score = c.getInt(4);
			}
		}

		//update the game state from the retrieved saved state.
		view.putX(x);
		view.putY(y);
		view.putSpeed(speed);
		if(serializedCollision != "")
			view.setSerializedCollision(serializedCollision);
		view.setScore(score);
	}

	//if back button is pressed, exits the game
	@Override
	public void onBackPressed(){


		finish();
		isBackPressed = true;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("game", e.getMessage());
		}
		db = dbh.getWritableDatabase();
		db.delete("gamedata", null, null);
	}
	
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			if(mediaVolume > 0){
				mediaVolume--;
				return true;
			}
		}
		else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			if(mediaVolume < manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)){
				mediaVolume++;
				return true;
			}
		}
		return false;
	}
*/
}