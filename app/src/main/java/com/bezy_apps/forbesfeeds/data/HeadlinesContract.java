package com.bezy_apps.forbesfeeds.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Eric on 3/1/2015.
 */
public class HeadlinesContract {

    public static final String CONTENT_AUTHORITY = "com.bezy_apps.forbesfeeds";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_HEADLINES = "headlines";
    public static final String PATH_COUNT = "count";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String SERVER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

    public static String getDbDateString(String serverDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SERVER_DATE_FORMAT);
        try {
            Date date = simpleDateFormat.parse(serverDate);
            return getDbDateString(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getDbDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final class HeadlinesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HEADLINES).build();

        public static final Uri COUNT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COUNT).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_HEADLINES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_HEADLINES;
        public static final String TABLE_NAME = "headlines";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESC = "description";
        public static final String COLUMN_PUB_DATE = "pubDate";
        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_IMAGE_URL = "imageUrl";
        public static final String COLUMN_IMAGE_PATH = "imagePath";


        public static Uri buildHeadlineUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildHeadlineWithLoadedCount(String loadedCount, String limit) {
            return CONTENT_URI.buildUpon().appendPath(loadedCount).appendPath(limit).build();
        }

        public static String getLoadedCountFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getLimitFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }
}
