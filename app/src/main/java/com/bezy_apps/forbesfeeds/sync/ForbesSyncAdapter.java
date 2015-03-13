package com.bezy_apps.forbesfeeds.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.bezy_apps.forbesfeeds.HeadlinesActivity;
import com.bezy_apps.forbesfeeds.R;
import com.bezy_apps.forbesfeeds.data.HeadlinesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Eric on 3/9/2015.
 */
public class ForbesSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final int SYNC_INTERVAL = 2 * 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 4;
    private final String LOG_TAG = ForbesSyncAdapter.class.getSimpleName();
    private static final int FETCH_COUNT = 10;
    final String BASE_URL = "http://ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=" + FETCH_COUNT + "&q=";
    final String HEADLINES_URL = "http://www.forbes.com/real-time/feed2/";
    final int MAX_ROWS = 100;
    final int NOTIFICATION_COUNT_LIMIT = 5;
    final String FORBES_DEFAULT_IMAGE_NAME = "forbes_200x200.jpg";
    final String CACHE_FOLDER_NAME = "/bezyapps_forbes/";

    private int New_Stories_Count;
    private final int HEADLINES_NOTIFICATION_ID = 40050;

    public ForbesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    private void notifyNewHeadlines() {
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        boolean notify = prefs.getBoolean(
                context.getString(R.string.pref_enable_notifications_key),
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
        if (notify) {
            int previous_count = Integer.parseInt(prefs.getString(
                    context.getString(R.string.pref_not_notified_news_key),
                    context.getString(R.string.pref_not_notified_news_default)));
            int total_stories = previous_count + New_Stories_Count;
            if (total_stories > NOTIFICATION_COUNT_LIMIT) {

                String content = String.format(context.getString(R.string.format_notification), String.valueOf(total_stories));
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(context.getString(R.string.app_name))
                                .setContentText(content);
                Intent resultIntent = new Intent(context, HeadlinesActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);
                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(HEADLINES_NOTIFICATION_ID, mBuilder.build());
                editor.putString(context.getString(R.string.pref_not_notified_news_key), String.valueOf(0));

            } else {
                editor.putString(context.getString(R.string.pref_not_notified_news_key), String.valueOf(total_stories));

            }
            editor.commit();
        }
    }

    private void putNewsInDB(String newsJsonString)
            throws JSONException {
        final String FORBES_RESPONSE = "responseData";
        final String FORBES_FEED = "feed";
        final String FORBES_ENTRIES = "entries";
        final String FORBES_TITLE = "title";
        final String FORBES_LINK = "link";
        final String FORBES_PUBLISHED_DATE = "publishedDate";
        final String FORBES_CONTENT = "content";
        final String FORBES_MEDIAGROUP = "mediaGroups";
        final String FORBES_MEDIA_CONTENTS = "contents";
        final String FORBES_MEDIA_URL = "url";
        final String FORBES_THUMBNAIL = "thumbnails";
        JSONObject response = new JSONObject(newsJsonString)
                .getJSONObject(FORBES_RESPONSE);
        JSONArray feedsJsonArray = response.getJSONObject(FORBES_FEED)
                .getJSONArray(FORBES_ENTRIES);

        New_Stories_Count = 0;
        for (int i = 0; i < feedsJsonArray.length(); i++) {
            JSONObject jsonNews = feedsJsonArray.getJSONObject(i);
            String title = jsonNews.getString(FORBES_TITLE);
            String link = jsonNews.getString(FORBES_LINK);
            String pubDate = jsonNews.getString(FORBES_PUBLISHED_DATE);
            String content = jsonNews.getString(FORBES_CONTENT);
            String imageUrl = jsonNews.getJSONArray(FORBES_MEDIAGROUP)
                    .getJSONObject(0).getJSONArray(FORBES_MEDIA_CONTENTS)
                    .getJSONObject(0).getJSONArray(FORBES_THUMBNAIL)
                    .getJSONObject(0).getString(FORBES_MEDIA_URL);


            String imagePath = null;
            ContentValues headlineValues = new ContentValues();
            headlineValues.put(HeadlinesContract.HeadlinesEntry.COLUMN_TITLE, title);
            headlineValues.put(HeadlinesContract.HeadlinesEntry.COLUMN_DESC, content);
            headlineValues.put(HeadlinesContract.HeadlinesEntry.COLUMN_LINK, link);
            headlineValues.put(HeadlinesContract.HeadlinesEntry.COLUMN_IMAGE_URL, imageUrl);
            headlineValues.putNull(HeadlinesContract.HeadlinesEntry.COLUMN_IMAGE_PATH);
            headlineValues.put(HeadlinesContract.HeadlinesEntry.COLUMN_PUB_DATE, HeadlinesContract.getDbDateString(pubDate));
            Uri rowUri = getContext().getContentResolver().insert(HeadlinesContract.HeadlinesEntry.CONTENT_URI, headlineValues);
            File image = null;
            if (rowUri != null) {
                long _id = ContentUris.parseId(rowUri);
                New_Stories_Count++;
                if (!imageUrl.contains(FORBES_DEFAULT_IMAGE_NAME)) {
                    try {
                        URL url = new URL(imageUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                        File sdcard = Environment.getExternalStorageDirectory();
                        File folder = new File(sdcard.getAbsoluteFile() + CACHE_FOLDER_NAME);
                        if (!folder.isDirectory()) {
                            folder.mkdir();
                        }
                        String format = imageUrl.substring(imageUrl.length() - 4, imageUrl.length());
                        imagePath = "/forbes_" + System.currentTimeMillis() + i + format;
                        image = new File(folder.getAbsoluteFile() + imagePath);
                        FileOutputStream outputStream = new FileOutputStream(image);
                        if (format.equals(".png")) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                        } else {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                        }
                        outputStream.flush();
                        outputStream.close();
                        headlineValues.put(HeadlinesContract.HeadlinesEntry.COLUMN_IMAGE_PATH, imagePath);
                        getContext().getContentResolver().update(HeadlinesContract.HeadlinesEntry.CONTENT_URI, headlineValues, HeadlinesContract.HeadlinesEntry._ID + " = ?", new String[]{String.valueOf(_id)});
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (image != null) {
                            if (image.exists()) {
                                image.delete();
                            }
                        }
                    }
                    Log.d(LOG_TAG, "URL DONE: " + i);
                }
            }
        }

        Cursor cursor = getContext().getContentResolver().query(HeadlinesContract.HeadlinesEntry.COUNT_URI, null, null, null, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        if (count > MAX_ROWS) {
            Uri uri = HeadlinesContract.HeadlinesEntry.buildHeadlineWithLoadedCount(String.valueOf(MAX_ROWS - 1), "1");
            cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String oldest_date = cursor.getString(cursor.getColumnIndex(HeadlinesContract.HeadlinesEntry.COLUMN_PUB_DATE));
            cursor.close();
            cursor = getContext().getContentResolver().query(HeadlinesContract.HeadlinesEntry.CONTENT_URI, new String[]{HeadlinesContract.HeadlinesEntry.COLUMN_IMAGE_PATH}, HeadlinesContract.HeadlinesEntry.COLUMN_PUB_DATE + " < ? ", new String[]{oldest_date}, null);
            File myDir = Environment.getExternalStorageDirectory();
            File mediaImage = null;
            while (cursor.moveToNext()) {
                mediaImage = new File(myDir.getPath() + CACHE_FOLDER_NAME + cursor.getString(0));
                if (mediaImage.exists()) {
                    mediaImage.delete();
                }
            }
            cursor.close();
            int delete = getContext().getContentResolver().delete(HeadlinesContract.HeadlinesEntry.CONTENT_URI, HeadlinesContract.HeadlinesEntry.COLUMN_PUB_DATE + " < ? ", new String[]{oldest_date});
            Log.d(LOG_TAG, "Delete Count: " + String.valueOf(delete));
        }

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        URL feed_url = null;
        BufferedReader bufferedReader = null;
        HttpURLConnection connection = null;
        String newsJsonString = null;
        try {
            feed_url = new URL(BASE_URL + HEADLINES_URL);
            if (feed_url != null) {
                connection = (HttpURLConnection) feed_url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(
                        inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return;
                }
                newsJsonString = buffer.toString();
                try {
                    putNewsInDB(newsJsonString);
                    notifyNewHeadlines();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            e.printStackTrace();
            return;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream", e);
                }
            }
        }
    }

    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }

    }


    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    private static void onAccountCreated(Account account, Context context) {

        ForbesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(account, context.getString(R.string.content_authority), true);
        syncImmediately(context);

    }


    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account account = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        if (accountManager.getPassword(account) == null) {
            if (!accountManager.addAccountExplicitly(account, "", null)) {
                return null;
            }
            onAccountCreated(account, context);
        }

        return account;
    }


    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


}
