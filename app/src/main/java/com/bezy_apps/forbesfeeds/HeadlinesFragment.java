package com.bezy_apps.forbesfeeds;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bezy_apps.forbesfeeds.data.HeadlinesContract;

public class HeadlinesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView listView;
    private NewsAdapter newsAdapter;
    private static final int HEADLINES_LOADER = 0;
    private static int FETCH_COUNT = 10;
    private int mLoadedCount = 0;
    public static final String NEWS_TITLE = "title";
    public static final String NEWS_LINK = "link";
    private static int mPosition;
    public static final String SELECTED_KEY = "selected_pos";

    public HeadlinesFragment() {
    }

    public interface ListItemSelectedCallback {
        public void onItemSelected(String url, String title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        newsAdapter = new NewsAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_headlines, container, false);
        listView = (ListView) rootView.findViewById(R.id.listView_news);
        listView.setAdapter(newsAdapter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getActivity().getString(R.string.pref_not_notified_news_key), String.valueOf(0));
        editor.commit();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = newsAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((ListItemSelectedCallback) getActivity()).onItemSelected(cursor.getString(cursor.getColumnIndex(HeadlinesContract.HeadlinesEntry.COLUMN_LINK)), cursor.getString(cursor.getColumnIndex(HeadlinesContract.HeadlinesEntry.COLUMN_TITLE)));
                }
                mPosition = position;

            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > 0) {
                    mPosition = firstVisibleItem;
                }
                int in_view = firstVisibleItem + visibleItemCount;
                if (in_view >= FETCH_COUNT) {
                    if (in_view == totalItemCount) {
                        Cursor cursor = getActivity().getContentResolver().query(HeadlinesContract.HeadlinesEntry.COUNT_URI, null, null, null, null);
                        cursor.moveToFirst();
                        int count = cursor.getInt(0);
                        cursor.close();
                        if (totalItemCount == count)
                            return;
                        Uri uri = HeadlinesContract.HeadlinesEntry.buildHeadlineWithLoadedCount(String.valueOf(totalItemCount), String.valueOf(FETCH_COUNT));
                        Cursor more_news = getActivity().getContentResolver().query(uri, null, null, null, null);
                        Cursor initial = newsAdapter.getCursor();
                        Cursor[] newsCursors = {initial, more_news};
                        Cursor extendedCursor = new MergeCursor(newsCursors);
                        newsAdapter.swapCursor(extendedCursor);
                        listView.setSelection(firstVisibleItem + Math.round(visibleItemCount / 2) - 1);
                    }
                }
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(HEADLINES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri uri = HeadlinesContract.HeadlinesEntry.buildHeadlineWithLoadedCount(String.valueOf(mLoadedCount), String.valueOf(FETCH_COUNT));
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        newsAdapter.swapCursor(cursor);
        if (mPosition != 1) {
            listView.setSelection(mPosition);
        } else {
            listView.setSelection(0);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        newsAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        FETCH_COUNT = ((mPosition / 10) + 1) * 10;
        getLoaderManager().restartLoader(HEADLINES_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

}
