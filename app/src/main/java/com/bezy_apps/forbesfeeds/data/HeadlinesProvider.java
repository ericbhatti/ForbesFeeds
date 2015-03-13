package com.bezy_apps.forbesfeeds.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.bezy_apps.forbesfeeds.data.HeadlinesContract.HeadlinesEntry;

/**
 * Created by Eric on 3/1/2015.
 */
public class HeadlinesProvider extends ContentProvider {

    private HeadlinesDbHelper headlinesDbHelper;
    private static final int HEADLINES = 100;
    private static final int HEADLINES_WITH_LOADED_COUNT = 101;
    private static final int HEADLINES_COUNT = 102;
    private static final int HEADLINES_WITH_DATE = 103;

    private static UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = HeadlinesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, HeadlinesContract.PATH_HEADLINES, HEADLINES);
        matcher.addURI(authority, HeadlinesContract.PATH_COUNT, HEADLINES_COUNT);
        matcher.addURI(authority, HeadlinesContract.PATH_HEADLINES + "/*/*", HEADLINES_WITH_LOADED_COUNT);

        matcher.addURI(authority, HeadlinesContract.PATH_HEADLINES + "/#", HEADLINES_WITH_DATE);

        return matcher;
    }

    private Cursor getHeadlinesWithLoadedCount(Uri uri) {
        String loadedCount = HeadlinesEntry.getLoadedCountFromUri(uri);
        String limit = HeadlinesEntry.getLimitFromUri(uri);
        return headlinesDbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + HeadlinesEntry.TABLE_NAME + " ORDER BY " + HeadlinesEntry.COLUMN_PUB_DATE + " DESC LIMIT " + loadedCount + " , " + limit, null);
    }

    @Override
    public boolean onCreate() {
        headlinesDbHelper = new HeadlinesDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case HEADLINES_WITH_LOADED_COUNT: {
                retCursor = getHeadlinesWithLoadedCount(uri);
                break;
            }
            case HEADLINES_COUNT: {
                retCursor = headlinesDbHelper.getReadableDatabase().rawQuery("SELECT COUNT(*) AS [" + HeadlinesEntry._COUNT + "] FROM  " + HeadlinesEntry.TABLE_NAME, null);
                break;
            }
            case HEADLINES: {
                retCursor = headlinesDbHelper.getReadableDatabase().query(HeadlinesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HEADLINES:
                return HeadlinesEntry.CONTENT_TYPE;
            case HEADLINES_WITH_LOADED_COUNT:
                return HeadlinesEntry.CONTENT_TYPE;
            case HEADLINES_COUNT:
                return HeadlinesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = headlinesDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case HEADLINES: {
                long _id = db.insert(HeadlinesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = HeadlinesEntry.buildHeadlineUri(_id);
                else
                    return null;
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = headlinesDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (match) {
            case HEADLINES: {
                rowsDeleted = db.delete(HeadlinesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (selection == null || 0 != rowsDeleted) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase database = headlinesDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case HEADLINES: {
                rowsUpdated = database.update(HeadlinesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
