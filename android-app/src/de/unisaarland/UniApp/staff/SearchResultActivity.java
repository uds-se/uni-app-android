package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.staff.uihelper.SearchResultAdapter;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;
import de.unisaarland.UniApp.utils.Util;


public class SearchResultActivity extends UpNavigationActionBarActivity {

    private static final SearchResult[] FAILED = new SearchResult[0];

    private NetworkRetrieveAndCache<SearchResult[]>[] networkFetchers;
    protected SearchResult[][] storedResults;
    private String[] urls;

    // store scroll position on leave and restore on return (on first content load)
    private Parcelable listState = null;

    @Override
    protected void onPause() {
        super.onPause();
        ListView listView = (ListView)findViewById(R.id.search_result_list);
        listState = listView.onSaveInstanceState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String[] newUrls = intent.getStringArrayExtra("urls");
        if (newUrls != null)
            urls = newUrls;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        urls = (String[]) Util.getExtra("urls", savedInstanceState, extras, null);

        setContentView(R.layout.search_result_layout);
    }

    // onResume gets called after onCreate or onNewIntent
    @Override
    protected void onResume() {
        super.onResume();

        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.GONE);

        if (urls == null)
            throw new AssertionError("urls should be passed via intent or from saved state");

        if (networkFetchers == null || networkFetchers.length != urls.length) {
            if (networkFetchers != null)
                for (NetworkRetrieveAndCache<SearchResult[]> fetcher : networkFetchers)
                    fetcher.cancel();
            networkFetchers = (NetworkRetrieveAndCache<SearchResult[]>[])
                    new NetworkRetrieveAndCache<?>[urls.length];
        }
        for (int i = 0; i < urls.length; ++i) {
            if (networkFetchers[i] != null && !networkFetchers[i].getUrl().equals(urls[i]))
                continue;
            if (networkFetchers[i] != null)
                networkFetchers[i].cancel();
            String tag = "search-" + Integer.toHexString(urls[i].hashCode());
            ContentCache cache = Util.getContentCache(this);
            networkFetchers[i] = new NetworkRetrieveAndCache<>(urls[i], tag, cache,
                    new SearchResultExtractor(urls[i]), new NetworkDelegate(i), this);
        }

        storedResults = new SearchResult[urls.length][];

        for (NetworkRetrieveAndCache<SearchResult[]> fetcher : networkFetchers)
            fetcher.loadAsynchronously(60 * 15);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("urls", urls);
    }

    @Override
    protected void onStop() {
        for (NetworkRetrieveAndCache<SearchResult[]> fetcher : networkFetchers)
            fetcher.cancel();
        super.onStop();
    }

    private class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<SearchResult[]> {
        private boolean hasResult = false;
        private final int idx;

        public NetworkDelegate(int idx) {
            this.idx = idx;
        }

        @Override
        public void onUpdate(SearchResult[] result, boolean fromCache) {
            hasResult = true;
            storedResults[idx] = result == null ? FAILED : result;
            showSearchResults();
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
                                    ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                    bar.setVisibility(View.GONE);
                                    dialog.dismiss();
                                    if (!hasResult)
                                        onBackPressed();
                                }
                            })
                    .create().show();
        }

        @Override
        public String checkValidity(SearchResult[] result) {
            // here, empty result is allowed
            return null;
        }
    }

    private void showSearchResults() {
        // wait until all fetcher got results
        for (SearchResult[] res : storedResults)
            if (res == null)
                return;

        ProgressBar pBar = (ProgressBar) findViewById(R.id.progress_bar);
        pBar.setVisibility(View.GONE);

        boolean failed = false;
        for (SearchResult[] res : storedResults) {
            if (res == FAILED)
                failed = true;
        }

        if (failed) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.too_few_search_terms_title)
                    .setMessage(R.string.too_few_search_terms_description)
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    onBackPressed();
                                }
                            })
                    .create().show();
            return;
        }

        List<SearchResult> allResults = new ArrayList<SearchResult>();
        for (SearchResult[] res : storedResults)
            allResults.addAll(Arrays.asList(res));

        if (allResults.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_staff_member_title)
                    .setMessage(R.string.no_staff_member_found_description)
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
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
            body.setAdapter(new SearchResultAdapter(this, allResults));
        } else {
            adapter.update(allResults);
            body.invalidate();
        }
        body.onRestoreInstanceState(listState);
        listState = null;
    }
}
