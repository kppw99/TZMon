package org.blockinger2.game.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ScoreDataSource
{
    private SQLiteDatabase database;
    private HighscoreOpenHelper dbHelper;
    private String[] columns = {
        HighscoreOpenHelper.COLUMN_ID,
        HighscoreOpenHelper.COLUMN_SCORE,
        HighscoreOpenHelper.COLUMN_PLAYERNAME
    };

    public ScoreDataSource(Context context)
    {
        dbHelper = new HighscoreOpenHelper(context);
    }

    public void open()
    {
        database = dbHelper.getWritableDatabase();
    }

    public void close()
    {
        dbHelper.close();
    }

    //    public Score createScore(long score, String playerName)
    public void createScore(long score, String playerName)
    {
        ContentValues values = new ContentValues();
        values.put(HighscoreOpenHelper.COLUMN_SCORE, score);
        values.put(HighscoreOpenHelper.COLUMN_PLAYERNAME, playerName);
        long insertId = database.insert(HighscoreOpenHelper.TABLE_HIGHSCORES, null, values);
        Cursor cursor = database.query(HighscoreOpenHelper.TABLE_HIGHSCORES,
            columns, HighscoreOpenHelper.COLUMN_ID + " = " + insertId, null,
            null, null, HighscoreOpenHelper.COLUMN_SCORE + " DESC");
        cursor.moveToFirst();
        cursor.close();
    }

    public Cursor getCursor()
    {
        return database.query(HighscoreOpenHelper.TABLE_HIGHSCORES,
            columns, null, null, null, null, HighscoreOpenHelper.COLUMN_SCORE + " DESC");
    }
}
