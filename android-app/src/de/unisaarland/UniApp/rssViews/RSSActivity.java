package de.unisaarland.UniApp.rssViews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.rssViews.model.RSSItem;
import de.unisaarland.UniApp.rssViews.model.RSSItemParser;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;

public class RSSActivity extends ActionBarActivity {

    public enum Category {
        News(R.string.newsText, R.string.noNewsText, Util.NEWS_URL, "news") {
            @Override
            protected View getView(RSSItem item, View convertView, Context context) {
                if(convertView == null){
                    convertView = View.inflate(context, R.layout.news_item, null);
                }
                TextView newsTitle = (TextView) convertView.findViewById(R.id.number);
                Date date = item.getPublicationDate();
                SimpleDateFormat parserSDF = new SimpleDateFormat("d. MMMM yyyy");
                String datestring = parserSDF.format(date);
                newsTitle.setText(datestring);
                TextView newsDescription = (TextView) convertView.findViewById(R.id.label);
                newsDescription.setGravity(Gravity.CENTER_VERTICAL);
                newsDescription.setText(item.getTitle());
                return convertView;
            }
        },
        Events(R.string.eventsText, R.string.noEventsText, Util.EVENTS_URL, "events") {
            @Override
            public List<RSSItem> filterItems(List<RSSItem> items) {
                List<RSSItem> filtered = new ArrayList<>();
                Date today = Util.getStartOfDay();
                for (RSSItem m : items)
                    if (!m.getPublicationDate().before(today))
                        filtered.add(m);
                Collections.sort(filtered, new Comparator<RSSItem>() {
                    @Override
                    public int compare(RSSItem lhs, RSSItem rhs) {
                        return lhs.getPublicationDate().compareTo(rhs.getPublicationDate());
                    }
                });
                return filtered;
            }

            @Override
            protected View getView(RSSItem item, View convertView, Context context) {
                if (convertView == null) {
                    convertView = View.inflate(context, R.layout.event_item, null);
                }
                Date date = item.getPublicationDate();
                //Set month in locale language
                SimpleDateFormat SDF = new SimpleDateFormat("MMM");
                TextView eventMonth = (TextView) convertView.findViewById(R.id.month_text);
                eventMonth.setText(SDF.format(date));
                //Set day in local language
                SDF = new SimpleDateFormat("d");
                TextView eventDate = (TextView) convertView.findViewById(R.id.day_text);
                eventDate.setText(SDF.format(date));

                TextView eventDescription = (TextView) convertView.findViewById(R.id.event_description);
                eventDescription.setText(item.getTitle());
                return convertView;

            }
        };

        private final int title;
        private final int noElementsText;
        private final String url;
        private final String cacheTag;

        Category(int title, int noElementsText, String url, String cacheTag) {
            this.title = title;
            this.noElementsText = noElementsText;
            this.url = url;
            this.cacheTag = cacheTag;
        }

        protected List<RSSItem> filterItems(List<RSSItem> items) {
            return items;
        }

        protected abstract View getView(RSSItem item, View convertView, Context context);
    }

    private static final String TAG = RSSActivity.class.getSimpleName();

    private Category cat;

    private NetworkRetrieveAndCache<List<RSSItem>> networkFetcher;

    /**
     * Will be called when activity created first time e.g. from scratch
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // store scroll position on leave and restore on return (on first content load)
    private Parcelable listState = null;
    @Override
    protected void onPause() {
        super.onPause();
        ListView listView = (ListView)findViewById(R.id.newsItemListView);
        listState = listView.onSaveInstanceState();
    }

    /**
     * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
     * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
     */
    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        cat = (Category) extras.getSerializable("category");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(cat.title);
        //Enable Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.news_panel);

        if (networkFetcher == null) {
            ContentCache cache = Util.getContentCache(this);
            networkFetcher = new NetworkRetrieveAndCache<>(cat.url, cat.cacheTag,
                    15*60, cache,
                    new RSSItemParser(), new NetworkDelegate(), this);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private final class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<List<RSSItem>> {
        private boolean hasItems = false;

        @Override
        public void onUpdate(List<RSSItem> events) {
            if (events.isEmpty()) {
                onFailure(getString(cat.noElementsText));
                return;
            }
            hasItems = true;

            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.clearAnimation();
            bar.setVisibility(View.INVISIBLE);
            populateItems(events);
        }

        @Override
        public void onStartLoading() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.setVisibility(View.VISIBLE);
            bar.animate();
        }

        @Override
        public void onFailure(String message) {
            new AlertDialog.Builder(RSSActivity.this).
                    setMessage(message).
                    setCancelable(true).
                    setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                bar.clearAnimation();
                                bar.setVisibility(View.INVISIBLE);
                                dialog.cancel();
                                if (!hasItems)
                                    onBackPressed();
                            }
                        })
                    .create().show();
        }
    }

    /**
     * after downloading and parsing the events when models are built it will call the adapter and pass the
     * specified model to it so that it will display list of event items.
     */
    private void populateItems(List<RSSItem> items) {
        ListView itemsList = (ListView) findViewById(R.id.newsItemListView);

        if (listState == null)
            listState = itemsList.onSaveInstanceState();

        List<RSSItem> filtered = cat.filterItems(items);
        RSSAdapter adapter = (RSSAdapter) itemsList.getAdapter();
        if (adapter == null) {
            itemsList.setAdapter(new RSSAdapter(this, filtered, cat));
        } else {
            adapter.update(filtered);
            itemsList.invalidate();
        }

        itemsList.onRestoreInstanceState(listState);
        listState = null;
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