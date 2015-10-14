package de.unisaarland.UniApp.events;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.events.model.EventModel;
import de.unisaarland.UniApp.events.model.EventsXMLParser;
import de.unisaarland.UniApp.events.uihelper.EventsAdapter;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkXMLRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;
import de.unisaarland.UniApp.utils.WebXMLFetcher;
import de.unisaarland.UniApp.utils.XMLFetcherDelegate;

public class EventsActivity extends ActionBarActivity {

    private static final String TAG = EventsActivity.class.getSimpleName();

    private static final String URL = "http://www.uni-saarland.de/aktuelles/veranstaltungen/alle-veranstaltungen/rss.xml";

    private NetworkXMLRetrieveAndCache<List<EventModel>> networkFetcher;

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
        actionBar.setTitle(R.string.eventsText);
        //Enable Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.news_panel);

        if (networkFetcher == null) {
            ContentCache cache = new ContentCache(this, "events", 60 * 60 * 24 * 14);
            networkFetcher = new NetworkXMLRetrieveAndCache<>(URL, "events", 15*60, cache,
                    new EventsXMLParser(), new NetworkDelegate(), this);
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

    private final class NetworkDelegate extends NetworkXMLRetrieveAndCache.Delegate<List<EventModel>> {
        private boolean hasEvents = false;

        @Override
        public void onUpdate(List<EventModel> events) {
            if (events.isEmpty()) {
                onFailure(getString(R.string.noEventsText));
                return;
            }
            hasEvents = true;

            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.clearAnimation();
            bar.setVisibility(View.INVISIBLE);
            populateEventItems(events);
        }

        @Override
        public void onStartLoading() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.setVisibility(View.VISIBLE);
            bar.animate();
        }

        @Override
        public void onFailure(String message) {
            new AlertDialog.Builder(EventsActivity.this).
                    setMessage(message).
                    setCancelable(true).
                    setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                    bar.clearAnimation();
                                    bar.setVisibility(View.INVISIBLE);
                                    dialog.cancel();
                                    if (!hasEvents)
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
    private void populateEventItems(List<EventModel> events) {
        ListView eventsList = (ListView) findViewById(R.id.newsItemListView);
        List<EventModel> filtered = sortAndFilterEvents(events);
        eventsList.setAdapter(new EventsAdapter(this, filtered));
    }

    private List<EventModel> sortAndFilterEvents(List<EventModel> events) {
        List<EventModel> filtered = new ArrayList<EventModel>();
        Date today = Util.getStartOfDay();
        for (EventModel m : events)
            if (!m.getPublicationDate().before(today))
                filtered.add(m);
        Collections.sort(filtered, new Comparator<EventModel>() {
            @Override
            public int compare(EventModel lhs, EventModel rhs) {
                return lhs.getPublicationDate().compareTo(rhs.getPublicationDate());
            }
        });
        return filtered;
    }

}