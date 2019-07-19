package org.blockinger2.game.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HighscoreOpenHelper extends SQLiteOpenHelper
{
    static final String TABLE_HIGHSCORES = "highscores";
    static final String COLUMN_ID = "_id";

    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_PLAYERNAME = "playername";

    private static final String DATABASE_NAME = "highscores.db";
    private static final int DATABASE_VERSION = 1;

    HighscoreOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        dropTables(db);
        onCreate(db);
    }

    private void createTables(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE "
            + TABLE_HIGHSCORES + "(" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_SCORE
            + " INTEGER, " + COLUMN_PLAYERNAME
            + " TEXT);");
    }

    private void dropTables(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIGHSCORES);
    }
}
