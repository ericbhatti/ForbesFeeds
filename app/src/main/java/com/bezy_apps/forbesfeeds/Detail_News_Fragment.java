package com.bezy_apps.forbesfeeds;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Eric on 3/5/2015.
 */
public class Detail_News_Fragment extends Fragment {

    public Detail_News_Fragment() {
        setHasOptionsMenu(true);
    }

    private static final String FORBES_SHARE_HASHTAG = "\n#Forbes";
    private String mshareStr;
    WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_news, container, false);
        webView = (WebView) rootView.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return false;
            }
        });
        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(HeadlinesFragment.NEWS_LINK) && intent.hasExtra(HeadlinesFragment.NEWS_TITLE)) {
            String title = getActivity().getIntent().getStringExtra(HeadlinesFragment.NEWS_TITLE);
            String url = getActivity().getIntent().getStringExtra(HeadlinesFragment.NEWS_LINK);
            mshareStr = title + "\n" + url + FORBES_SHARE_HASHTAG;
            webView.loadUrl(url);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);

    }


    private Intent createShareNewsIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mshareStr);

        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_news_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareNewsIntent());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(HeadlinesFragment.NEWS_TITLE) && arguments.containsKey(HeadlinesFragment.NEWS_LINK)) {
            webView.loadUrl(arguments.getString(HeadlinesFragment.NEWS_LINK));
            mshareStr = arguments.getString(HeadlinesFragment.NEWS_TITLE) + "\n" + arguments.getString(HeadlinesFragment.NEWS_LINK) + FORBES_SHARE_HASHTAG;
        }
    }
}
