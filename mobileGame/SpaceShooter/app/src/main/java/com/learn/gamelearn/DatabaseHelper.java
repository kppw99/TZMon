package com.learn.gamelearn;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "galaxy.db";
	public final String ALIEN_X = "alien_x";
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE gamedata(alien_x INTEGER, alien_y INTEGER, alien_speed INTEGER, collision TEXT, current_score INTEGER);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		android.util.Log.v("gamedata", "Table Update : Destroying old data and creating new table !");
		onCreate(db);
	}

}
