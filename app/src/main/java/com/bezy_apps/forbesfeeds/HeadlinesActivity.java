package com.bezy_apps.forbesfeeds;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bezy_apps.forbesfeeds.data.HeadlinesContract;
import com.bezy_apps.forbesfeeds.sync.ForbesSyncAdapter;


public class HeadlinesActivity extends ActionBarActivity implements HeadlinesFragment.ListItemSelectedCallback {


    public static boolean misTwoPane;

    @Override
    public void onItemSelected(String url, String title) {

        if (misTwoPane) {
            Bundle args = new Bundle();
            args.putString(HeadlinesFragment.NEWS_TITLE, title);
            args.putString(HeadlinesFragment.NEWS_LINK, url);
            Detail_News_Fragment fragment = new Detail_News_Fragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.news_detail_container, fragment).commit();

        } else {
            Intent i = new Intent(this, Details_Activity.class);
            i.putExtra(HeadlinesFragment.NEWS_LINK, url);
            i.putExtra(HeadlinesFragment.NEWS_TITLE, title);
            startActivity(i);
        }
        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_headlines);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (findViewById(R.id.news_detail_container) != null) {
            misTwoPane = true;
            if (savedInstanceState == null) {
                Bundle args = new Bundle();
                Uri uri = HeadlinesContract.HeadlinesEntry.buildHeadlineWithLoadedCount(String.valueOf(0), String.valueOf(1));
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    args.putString(HeadlinesFragment.NEWS_TITLE, cursor.getString(cursor.getColumnIndex(HeadlinesContract.HeadlinesEntry.COLUMN_TITLE)));
                    args.putString(HeadlinesFragment.NEWS_LINK, cursor.getString(cursor.getColumnIndex(HeadlinesContract.HeadlinesEntry.COLUMN_LINK)));
                } else {
                    args.putString(HeadlinesFragment.NEWS_TITLE, "Information for the World's Business Leaders");
                    args.putString(HeadlinesFragment.NEWS_LINK, "http://www.forbes.com/");
                }
                Detail_News_Fragment fragment = new Detail_News_Fragment();
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.news_detail_container, fragment).commit();

            }
        } else {
            misTwoPane = false;
        }

        ForbesSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_headlines, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
