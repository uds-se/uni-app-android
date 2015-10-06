package de.unisaarland.UniApp.news;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.networkcommunicator.INetworkLoaderDelegate;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.networkcommunicator.WebFetcher;
import de.unisaarland.UniApp.news.model.INewsResultDelegate;
import de.unisaarland.UniApp.news.model.NewsModel;
import de.unisaarland.UniApp.news.model.NewsXMLParser;
import de.unisaarland.UniApp.news.uihelper.NewsAdapter;


public class NewsActivity extends ActionBarActivity {

    private static final String TAG = NewsActivity.class.getSimpleName();

    private static final String URL = "http://www.uni-saarland.de/aktuelles/presse/pms.html?type=100&tx_ttnews[cat]=26";
    private volatile WebFetcher lastWebFetcher = null;

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

        setActionBar();

        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        long lastLoadMillis = settings.getLong(Util.NEWS_LOAD_MILLIS, 0);
        // We first load the cached news (and show then), then update the list asynchronously if
        // last fetch was more than 15 minutes ago
        List<NewsModel> cachedNews = loadNewsFromCache();
        if (cachedNews != null) {
            setContentView(R.layout.news_panel);
            populateNewsItems(cachedNews);
        }
        long cachedNewsAgeMillis = Math.abs(lastLoadMillis - System.currentTimeMillis());
        // Reload after 15 minutes
        if (cachedNews == null || cachedNewsAgeMillis >= 1000*60*15) {
            // only use cached data if it is younger than 3 days
            boolean hasCached = cachedNews != null && cachedNewsAgeMillis < 1000*60*60*24*3;
            startLoading(hasCached);
        }
    }

    /**
     * Called when back button is pressed either from device or navigation bar.
     */
    @Override
    public void onBackPressed() {
        // will invalidate the connection establishment request if it is not being completed yet and free the resources
        WebFetcher fetcher = lastWebFetcher;
        if (fetcher != null)
            fetcher.invalidateRequest();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        lastWebFetcher = null;
        super.onStop();
    }


    private class NetworkDelegate implements INetworkLoaderDelegate {

        private final boolean hasCached;

        public NetworkDelegate(boolean hasCached) {
            this.hasCached = hasCached;
        }

        /**
         * Will be called in case of failure e.g internet connection problem
         * Will try to load news from already stored model or in case if that model is not present will show the
         * error dialog
         */
        @Override
        public void onFailure(String message) {
            new AlertDialog.Builder(NewsActivity.this).
                    setMessage(message).
                    setCancelable(true).
                    setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                    if (bar != null) {
                                        bar.clearAnimation();
                                        bar.setVisibility(View.INVISIBLE);
                                    }
                                    dialog.cancel();
                                    if (!hasCached)
                                        onBackPressed();
                                }
                            }).
                    create().show();
        }
        /*
        * Will be called in case of success if connection is successfully established.
        * Call the News parser to parse the resultant file and return the list of news models to specified call back method.
        * */
        @Override
        public void onSuccess(InputStream data) {
            new NewsXMLParser().startParsing(data, new NewsResultDelegate(),
                    NewsActivity.this);
        }
    }

    /*
    * Load news from already saved model if internet is not available or coming back from news detail
    * */
    private List<NewsModel> loadNewsFromCache() {
        File file = new File(getCacheDir() + Util.NEWS_FILE_NAME);
        if (!file.exists()) {
            Log.w(TAG, "News cache does not exist");
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            int numNews = ois.readInt();
            List<NewsModel> news = new ArrayList<NewsModel>();
            for (int i = 0; i < numNews; ++i)
                news.add((NewsModel) ois.readObject());
            ois.close();
            return news;
        } catch (IOException e) {
            Log.w(TAG, "Cannot load news from cache file", e);
            return null;
        } catch (ClassNotFoundException | ClassCastException e) {
            Log.e(TAG, "Weird class error when loading news from cache file", e);
            return null;
        }
    }

    /*
     * Call back method of NewsResult will be called when all news are parsed and Model list is generated
     */
    private class NewsResultDelegate implements INewsResultDelegate {
        @Override
        public void newsList(List<NewsModel> news) {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            if (bar != null) {
                bar.clearAnimation();
                bar.setVisibility(View.INVISIBLE);
            }
            setContentView(R.layout.news_panel);
            populateNewsItems(news);

            boolean itemsSaved = saveNewItemsToCache(news);
            if (itemsSaved)
                Log.i(TAG, "Saved news to cache");
            SharedPreferences.Editor editor = getSharedPreferences(Util.PREFS_NAME, 0).edit();
            editor.putLong(Util.NEWS_LOAD_MILLIS, System.currentTimeMillis());
            editor.commit();
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
                                    if (bar != null) {
                                        bar.clearAnimation();
                                        bar.setVisibility(View.INVISIBLE);
                                    }
                                    dialog.cancel();
                                    onBackPressed();
                                }
                            }).
                    create().show();
        }
    }

    /**
     * Save current news model to file (temporary) so that these will be used later in case if user don't have internet connection
     * and also if user is coming back from seeing a detailed news.
     */
    private boolean saveNewItemsToCache(List<NewsModel> news) {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(getCacheDir()+ Util.NEWS_FILE_NAME)));
            oos.writeInt(news.size());
            for (NewsModel n : news)
                oos.writeObject(n);
            oos.close();
        } catch (IOException e) {
            Log.w(TAG, "Cannot save news to cache file", e);
            return false;
        }
        return true;
    }

    private void startLoading(boolean hasCached) {
        if (!hasCached) {
            //displays the loading view and download and parse the news items from internet
            setContentView(R.layout.loading_layout);
        }
        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.VISIBLE);
        bar.animate();

        /**
         * Calls the custom class to connect and download the specific XML and pass the delegate method which will be called
         * in case of success and failure
         */

        WebFetcher fetcher = new WebFetcher(new NetworkDelegate(hasCached));
        fetcher.startFetchingAsynchronously(URL, this);
        lastWebFetcher = fetcher;
    }

    /*
    * after downloading and parsing the news when models are built it will call the adapter and pass the
    * specified model to it so that it will display list of news items.
    * */
    private void populateNewsItems(List<NewsModel> news) {
        ListView newsList = (ListView) findViewById(R.id.newsItemListView);
        newsList.setAdapter(new NewsAdapter(this, news));
    }

    /**
     * sets the custom navigation bar according to each activity.
     */

    private void setActionBar() {
        //Enable Up-Navigation
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.newsText);
    }

    //Creation Custom Actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
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
                WebFetcher fetcher = lastWebFetcher;
                if (fetcher != null)
                    fetcher.invalidateRequest();

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

    // custom class to show the back button action using navigation bar and will call the onBack method of activity
    class BackButtonClickListener implements View.OnClickListener{
        final Activity activity;
        public BackButtonClickListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            activity.onBackPressed();

        }
    }
}
