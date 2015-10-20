package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.staff.uihelper.SearchResultAdapter;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;


public class SearchResultActivity extends ActionBarActivity {
    private String url = null;
    
    private NetworkRetrieveAndCache<List<SearchResult>> networkFetcher;

    // store scroll position on leave and restore on return (on first content load)
    private Parcelable listState = null;
    @Override
    protected void onPause() {
        super.onPause();
        ListView listView = (ListView)findViewById(R.id.search_result_list);
        listState = listView.onSaveInstanceState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        url = extras.getString("url");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.search_results);

        setContentView(R.layout.search_result_layout);

        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.GONE);

        if (networkFetcher == null || !url.equals(networkFetcher.getUrl())) {
            String tag = "search-" + Integer.toHexString(url.hashCode());
            ContentCache cache = Util.getContentCache(this);
            networkFetcher = new NetworkRetrieveAndCache<>(url, tag, 60*15, cache,
                    new SearchResultExtractor(url), new NetworkDelegate(), this);
        }
        networkFetcher.loadAsynchronously();
    }

    @Override
    protected void onStop() {
        if (networkFetcher != null) {
            networkFetcher.cancel();
            networkFetcher = null;
        }
        super.onStop();
    }

    private class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<List<SearchResult>> {
        @Override
        public void onUpdate(List<SearchResult> result) {
            showSearchResults(result);
        }

        @Override
        public void onStartLoading() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.animate();
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onFailure(String message) {
            new AlertDialog.Builder(SearchResultActivity.this)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    onBackPressed();
                                }
                            })
                    .create().show();
        }
    }

    private void showSearchResults(List<SearchResult> result) {
        ProgressBar pBar = (ProgressBar) findViewById(R.id.progress_bar);
        pBar.setVisibility(View.GONE);
        if (result.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_staff_member_title)
                    .setMessage(R.string.no_staff_member_found_description)
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    onBackPressed();
                                }
                            })
                    .create().show();
            return;
        }

        ListView body = (ListView) findViewById(R.id.search_result_list);

        if (listState == null)
            listState = body.onSaveInstanceState();

        SearchResultAdapter adapter = (SearchResultAdapter) body.getAdapter();
        if (adapter == null) {
            body.setAdapter(new SearchResultAdapter(this, result));
        } else {
            adapter.update(result);
            body.invalidate();
        }

        body.onRestoreInstanceState(listState);
        listState = null;
    }

    // Handling the Action Bar Buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                NavUtils.navigateUpFromSameTask(this);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}