package de.unisaarland.UniApp.rssViews;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.rssViews.model.RSSArticle;
import de.unisaarland.UniApp.rssViews.model.RSSArticleExtractor;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;


/**
 * Activity for showing events or news.
 */
public class RSSDetailActivity extends ActionBarActivity {

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

        String url;
        int titleId = -1;

        if (extras != null) {
            url = extras.getString("url");
            titleId = extras.getInt("titleId");
        } else {
            url = savedInstanceState.getString("url");
        }

        // sets the custom navigation bar according to each activity.
        ActionBar actionBar = getSupportActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (titleId != -1)
            actionBar.setTitle(titleId);

        setContentView(R.layout.rss_detail);
        if (fetcher == null) {
            if (url == null)
                throw new NullPointerException("url must be supplied by intent or saved state");
            String tag = "rss-"+Integer.toHexString(url.hashCode());
            fetcher = new NetworkRetrieveAndCache<>(url, tag, 15 * 60,
                    Util.getContentCache(this),
                    new RSSArticleExtractor(url),
                    new NetworkDelegate(), this);
        }
        fetcher.loadAsynchronously();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (fetcher != null)
            outState.putString("url", fetcher.getUrl());
    }

    @Override
    protected void onStop() {
        if (fetcher != null) {
            fetcher.cancel();
            fetcher = null;
        }
        super.onStop();
    }

    private class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<RSSArticle> {
        private boolean hasItem = false;

        @Override
        public void onUpdate(RSSArticle rss) {
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