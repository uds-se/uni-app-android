package de.unisaarland.UniApp.news;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.news.model.NewsModel;
import de.unisaarland.UniApp.news.model.NewsXMLParser;
import de.unisaarland.UniApp.news.uihelper.NewsAdapter;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;


public class NewsActivity extends ActionBarActivity {

    private static final String TAG = NewsActivity.class.getSimpleName();

    private static final String URL = "http://www.uni-saarland.de/aktuelles/presse/pms.html?type=100&tx_ttnews[cat]=26";

    private NetworkRetrieveAndCache<List<NewsModel>> networkFetcher;

    /**
     * Will be called when activity created first time e.g. from scratch
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
     * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
     */
    @Override
    protected void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.newsText);
        //Enable Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.news_panel);

        if (networkFetcher == null) {
            ContentCache cache = new ContentCache(this, "news", 60 * 60 * 24 * 14);
            networkFetcher = new NetworkRetrieveAndCache<>(URL, "news", 15*60, cache,
                    new NewsXMLParser(), new NetworkDelegate(), this);
        }
        networkFetcher.loadAsynchronously();
    }

    /**
     * Called when back button is pressed either from device or navigation bar.
     */
    @Override
    public void onBackPressed() {
        if (networkFetcher != null)
            networkFetcher.cancel();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        if (networkFetcher != null) {
            networkFetcher.cancel();
            networkFetcher = null;
        }
        super.onStop();
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
            /**
             * clicking on the facebook-button will check if the facebook app is installed on the device then it will
             * open the specific page on that app otherwise it will open the page on browser.
             */
            case R.id.action_facebook: {
                if (networkFetcher != null)
                    networkFetcher.cancel();

                Uri dataUri = Uri.parse("fb://profile/120807804649363");
                Intent receiverIntent = new Intent(Intent.ACTION_VIEW, dataUri);

                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(receiverIntent, 0);

                if (activities.size() > 0) {
                    startActivity(receiverIntent);

                } else {
                    Uri webpage = Uri.parse("http://www.facebook.com/120807804649363");
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);

                    packageManager = getPackageManager();
                    activities = packageManager.queryIntentActivities(webIntent, 0);

                    if (activities.size() > 0) {
                        startActivity(webIntent);
                    }
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private final class NetworkDelegate extends NetworkRetrieveAndCache.Delegate<List<NewsModel>> {
        private boolean hasNews = false;

        @Override
        public void onUpdate(List<NewsModel> news) {
            if (news.isEmpty()) {
                onFailure(getString(R.string.noNewsText));
                return;
            }
            hasNews = true;

            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.clearAnimation();
            bar.setVisibility(View.INVISIBLE);
            populateNewsItems(news);
        }

        @Override
        public void onStartLoading() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.setVisibility(View.VISIBLE);
            bar.animate();
        }

        @Override
        public void onFailure(String message) {
            new AlertDialog.Builder(NewsActivity.this).
                    setMessage(message).
                    setCancelable(true).
                    setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                    bar.clearAnimation();
                                    bar.setVisibility(View.INVISIBLE);
                                    dialog.cancel();
                                    if (!hasNews)
                                        onBackPressed();
                                }
                            })
                    .create().show();
        }
    }

    /**
     * after downloading and parsing the news when models are built it will call the adapter and pass the
     * specified model to it so that it will display list of news items.
     */
    private void populateNewsItems(List<NewsModel> news) {
        ListView newsList = (ListView) findViewById(R.id.newsItemListView);
        newsList.setAdapter(new NewsAdapter(this, news));
    }

    //Creation Custom Actionbar with facebook link
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
