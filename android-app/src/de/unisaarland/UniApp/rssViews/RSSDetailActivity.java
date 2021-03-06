package de.unisaarland.UniApp.rssViews;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.rssViews.model.RSSArticle;
import de.unisaarland.UniApp.rssViews.model.RSSArticleExtractor;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;
import de.unisaarland.UniApp.utils.Util;


/**
 * Activity for showing events or news.
 */
public class RSSDetailActivity extends UpNavigationActionBarActivity {

    private NetworkRetrieveAndCache<RSSArticle> fetcher = null;

    /**
     * Will be called when activity created as this activity is being created from scratch every time when user
     * wants to view a new event details so all work is being done in onCreate no need to separate the work
     * in onResume.
     * It gets the event model object from intent which is saved with name model.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        String url = (String) Util.getExtra("url", savedInstanceState, extras, null);
        int titleId = extras == null ? -1 : extras.getInt("titleId", -1);

        if (url == null)
            throw new AssertionError("url should be passed via intent or from saved state");

        // sets the custom navigation bar according to each activity.
        ActionBar actionBar = getSupportActionBar();
        if (titleId != -1)
            actionBar.setTitle(titleId);

        setContentView(R.layout.rss_detail);
        if (fetcher == null) {
            String tag = "rss-"+Integer.toHexString(url.hashCode());
            fetcher = new NetworkRetrieveAndCache<>(url, tag,
                    Util.getContentCache(this),
                    new RSSArticleExtractor(url),
                    new NetworkDelegate(), this);
        }
        fetcher.loadAsynchronously(15 * 60);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", fetcher.getUrl());
    }

    @Override
    protected void onStop() {
        if (fetcher != null)
            fetcher.cancel();
        super.onStop();
    }

    private class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<RSSArticle> {
        private boolean hasItem = false;

        @Override
        public void onUpdate(RSSArticle rss, boolean fromCache) {
            hasItem = true;
            showRSSItem(rss);
        }

        @Override
        public void onStartLoading() {
            ProgressBar pBar = (ProgressBar) findViewById(R.id.progress_bar);
            pBar.animate();
            pBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onFailure(String message) {
            ProgressBar pBar = (ProgressBar) findViewById(R.id.progress_bar);
            pBar.setVisibility(View.GONE);
            new AlertDialog.Builder(RSSDetailActivity.this).
                    setMessage(message).
                    setCancelable(true).
                    setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    if (!hasItem)
                                        onBackPressed();
                                }
                            })
                    .create().show();
        }

        @Override
        public String checkValidity(RSSArticle result) {
            return null;
        }
    }

    /**
     * load the downloaded description of a event and show it as html after setting the necessary html tags.
     */
    private void showRSSItem(RSSArticle item) {
        WebView bodyView = (WebView) findViewById(R.id.body);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head></head><body><h5><center>").append(item.getDate());
        sb.append("</center></h5><h3><center><font color=\"#034A78\">").append(item.getHead());
        sb.append("</font></center></h3>");
        if (item.getSubTitle() != null) {
            sb.append("<h5><center>").append(item.getSubTitle()).append("</center></h5>");
        }
        sb.append("<div style=\"padding-left:10px; padding-right:10px\">");
        sb.append(item.getBody()).append("</div></body>");
        bodyView.loadDataWithBaseURL(item.getBaseHref(), sb.toString(), "text/html", "utf-8", null);
        bodyView.getSettings().setJavaScriptEnabled(true);
        bodyView.setVisibility(View.VISIBLE);
        ProgressBar pBar = (ProgressBar) findViewById(R.id.progress_bar);
        pBar.setVisibility(View.GONE);
    }
}