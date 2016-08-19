package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.staff.uihelper.SearchResultAdapter;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;
import de.unisaarland.UniApp.utils.Util;


public class SearchResultActivity extends UpNavigationActionBarActivity {
    
    private NetworkRetrieveAndCache<List<SearchResult>> networkFetcher;
    private NetworkRetrieveAndCache<List<SearchResult>> juniorNetworkFetcher;
    private List<SearchResult> storedResults;
    private String url;
    private String juniorUrl;

    // store scroll position on leave and restore on return (on first content load)
    private Parcelable listState = null;
    private String searchType = "";
    private int failCount = 0;
    @Override
    protected void onPause() {
        super.onPause();
        ListView listView = (ListView)findViewById(R.id.search_result_list);
        listState = listView.onSaveInstanceState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        url = intent.getStringExtra("url");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storedResults = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        String url = (String) Util.getExtra("url", savedInstanceState, extras, null);
        searchType = (String) Util.getExtra("searchType", savedInstanceState, extras, null);
        if (url == null)
            throw new AssertionError("url should be passed via intent or from saved state");

        if (searchType == null)
            searchType = "all";
        setContentView(R.layout.search_result_layout);
    }

    // onResume gets called after onCreate or onNewIntent
    @Override
    protected void onResume() {
        super.onResume();

        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.GONE);

        NetworkDelegate networkDelegate = new NetworkDelegate();
        if (networkFetcher == null || !url.equals(networkFetcher.getUrl())) {
            String tag = "search-" + Integer.toHexString(url.hashCode());
            ContentCache cache = Util.getContentCache(this);
            networkFetcher = new NetworkRetrieveAndCache<>(url, tag, 60*15, cache,
                    new SearchResultExtractor(url), networkDelegate, this);
        }
        if(searchType.equalsIgnoreCase("prof")) {
            String juniorProfURL = (String) Util.getExtra("juniorProfURL", savedInstanceState, extras, null);
            if (juniorNetworkFetcher == null || !juniorProfURL.equals(juniorNetworkFetcher.getUrl())) {
                String tag = "search-" + Integer.toHexString(juniorProfURL.hashCode());
                ContentCache cache = Util.getContentCache(this);
                juniorNetworkFetcher = new NetworkRetrieveAndCache<>(juniorProfURL, tag, 60 * 15, cache,
                        new SearchResultExtractor(juniorProfURL), new NetworkDelegate(), this);
            }
        }
        else
            juniorNetworkFetcher = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        storedResults = new ArrayList<>();
        networkFetcher.loadAsynchronously();
        if(juniorNetworkFetcher != null)
            juniorNetworkFetcher.loadAsynchronously();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", networkFetcher.getUrl());
        if(juniorNetworkFetcher != null)
            outState.putString("juniorProfURL", juniorNetworkFetcher.getUrl());
    }

    @Override
    protected void onStop() {
        if (networkFetcher != null)
            networkFetcher.cancel();
        if(juniorNetworkFetcher != null)
            juniorNetworkFetcher.cancel();
        super.onStop();
    }

    private class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<List<SearchResult>> {
        private boolean hasResult = false;

        @Override
        public void onUpdate(List<SearchResult> result, boolean fromCache) {
            hasResult = true;
            if(result == null || result.isEmpty())
                showSearchResults(result);
            else {
                storedResults.addAll(result);
                showSearchResults(storedResults);
            }

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
        public String checkValidity(List<SearchResult> result) {
            // here, empty result is allowed
            return null;
        }
    }

    private void showSearchResults(List<SearchResult> result) {
        ProgressBar pBar = (ProgressBar) findViewById(R.id.progress_bar);
        pBar.setVisibility(View.GONE);
        if (result == null) {
            failCount++;
            if ((searchType.equalsIgnoreCase("prof") && failCount == 2) || searchType.equalsIgnoreCase("all")) {
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
                //return;
            }
            return;
        }
        if (result.isEmpty()) {
            failCount++;
            Log.i ("info", String.valueOf(failCount));
            if ((searchType.equalsIgnoreCase("prof") && failCount == 2) || searchType.equalsIgnoreCase("all")) {
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
                //return;
            }
            return;
        }

        ListView body = (ListView) findViewById(R.id.search_result_list);

        if (listState == null)
            listState = body.onSaveInstanceState();

        SearchResultAdapter adapter = (SearchResultAdapter) body.getAdapter();
        if (adapter == null) {
            Log.i ("info", "Found NULL");
            body.setAdapter(new SearchResultAdapter(this, result));
        } else {
            adapter.update(result);
            body.invalidate();
        }
        failCount = 0;
        body.onRestoreInstanceState(listState);
        listState = null;
    }
}
