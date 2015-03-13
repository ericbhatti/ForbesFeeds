package com.bezy_apps.forbesfeeds;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Eric on 3/5/2015.
 */
public class Details_Activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.news_detail_container, new Detail_News_Fragment())
                    .commit();
        }
    }
}
