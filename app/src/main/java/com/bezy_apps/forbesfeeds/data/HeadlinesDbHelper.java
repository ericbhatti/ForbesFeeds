package com.bezy_apps.forbesfeeds.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.bezy_apps.forbesfeeds.data.HeadlinesContract.HeadlinesEntry;

/**
 * Created by Eric on 3/1/2015.
 */
public class HeadlinesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;

    public HeadlinesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_HEADLINES_TABLE = "CREATE TABLE " + HeadlinesEntry.TABLE_NAME + " ( " +
                HeadlinesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HeadlinesEntry.COLUMN_TITLE  + " TEXT NOT NULL, " +
                HeadlinesEntry.COLUMN_DESC + " TEXT NOT NULL, " +
                HeadlinesEntry.COLUMN_PUB_DATE + " TEXT NOT NULL, " +
                HeadlinesEntry.COLUMN_LINK + " TEXT NOT NULL, " +
                HeadlinesEntry.COLUMN_IMAGE_URL + " TEXT, " +
                HeadlinesEntry.COLUMN_IMAGE_PATH + " TEXT, " +

                "UNIQUE (" + HeadlinesEntry.COLUMN_LINK + ") ON CONFLICT IGNORE);";
        db.execSQL(SQL_CREATE_HEADLINES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HeadlinesEntry.TABLE_NAME);
        onCreate(db);
    }
}
