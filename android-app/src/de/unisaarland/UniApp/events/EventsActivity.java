package de.unisaarland.UniApp.events;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import java.io.InputStream;
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
import de.unisaarland.UniApp.events.model.IEventsResultDelegate;
import de.unisaarland.UniApp.events.uihelper.EventsAdapter;
import de.unisaarland.UniApp.utils.INetworkLoaderDelegate;
import de.unisaarland.UniApp.utils.Util;
import de.unisaarland.UniApp.utils.WebFetcher;

public class EventsActivity extends ActionBarActivity {

    private static final String TAG = EventsActivity.class.getSimpleName();

    private static final String URL = "http://www.uni-saarland.de/aktuelles/veranstaltungen/alle-veranstaltungen/rss.xml";
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
        long lastLoadMillis = settings.getLong(Util.EVENTS_LOAD_MILLIS, 0);
        // We first load the cached events (and show then), then update the list asynchronously if
        // last fetch was more than 15 minutes ago
        List<EventModel> cachedEvents = loadEventsFromCache();
        if (cachedEvents != null) {
            setContentView(R.layout.news_panel);
            populateEventItems(cachedEvents);
        }
        long cachedEventAgeMillis = Math.abs(lastLoadMillis - System.currentTimeMillis());
        // Reload after 15 minutes
        if (cachedEvents == null || cachedEventAgeMillis >= 1000*60*15) {
            // only use cached data if it is younger than 3 days
            boolean hasCached = cachedEvents != null && cachedEventAgeMillis < 1000*60*60*24*3;
            startLoading(hasCached);
        }
    }

    /**
     * Called when back button is pressed either from device or navigation bar.
     */
    @Override
    public void onBackPressed() {
        // will invalidate the connection establishment request if it is not being completed yet and free the resources
        WebFetcher webFetcher = lastWebFetcher;
        if (webFetcher != null)
            webFetcher.invalidateRequest();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        lastWebFetcher = null;
        super.onStop();
    }

    /**
     * sets the custom navigation bar according to each activity.
     */
    private void setActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.eventsText);
        //Enable Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
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

    private void startLoading(boolean hasCached) {
        if (!hasCached) {
            //displays the loading view and download and parse the event items from internet
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

    private final class NetworkDelegate implements INetworkLoaderDelegate {
        private final boolean hasCached;

        public NetworkDelegate(boolean hasCached) {
            this.hasCached = hasCached;
        }

        /**
         * Will be called in case of failure e.g internet connection problem
         * Will try to load events from already stored model or in case if that model is not present will show the
         * error dialog
         */
        @Override
        public void onFailure(String message) {
            new AlertDialog.Builder(EventsActivity.this).
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
        * Will be called in case of success if connection is successfully established and parser is ready
        * call the Events parser to parse the resultant file and return the list of event models to specified call back method.
        * */
        @Override
        public void onSuccess(InputStream data) {
            new EventsXMLParser().startParsing(data, new EventsResultDelegate(),
                    EventsActivity.this);
        }
    }

    /*
    * Call back method of EventResultDelegate will be called when all events are parsed and Model list is generated
    * */
    private final class EventsResultDelegate implements IEventsResultDelegate {
        @Override
        public void eventsList(List<EventModel> events) {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            if (bar != null) {
                bar.clearAnimation();
                bar.setVisibility(View.INVISIBLE);
            }
            setContentView(R.layout.news_panel);
            populateEventItems(events);

            boolean itemsSaved = saveEventItemsToCache(events);
            if (itemsSaved)
                Log.i(TAG, "Saved events to cache");
            SharedPreferences.Editor editor = getSharedPreferences(Util.PREFS_NAME, 0).edit();
            editor.putLong(Util.EVENTS_LOAD_MILLIS, System.currentTimeMillis());
            editor.commit();
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

    /*
    * after downloading and parsing the events when models are built it will call the adapter and pass the
    * specified model to it so that it will display list of event items.
    * */
    private void populateEventItems(List<EventModel> events) {
        ListView eventsList = (ListView) findViewById(R.id.newsItemListView);
        List<EventModel> filtered = sortAndFilterEvents(events);
        eventsList.setAdapter(new EventsAdapter(this, filtered));
    }

    private List<EventModel> sortAndFilterEvents(List<EventModel> events) {
        List<EventModel> filtered = new ArrayList<EventModel>();
        GregorianCalendar now = new GregorianCalendar();
        Date today = new GregorianCalendar(now.get(Calendar.YEAR),
                now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).getTime();
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

    /*
    * Save current event model to file (temporary) so that these will be used later in case if user don't have internet connection
    * and also if user is coming back from seeing a detailed event.
    * */
    private boolean saveEventItemsToCache(List<EventModel> events) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                    new File(getCacheDir()+ Util.EVENTS_FILE_NAME)));
            oos.writeInt(events.size());
            for (EventModel m : events)
                oos.writeObject(m);
            oos.close();
        } catch (IOException e) {
            Log.w(TAG, "Cannot save events to cache file", e);
            return false;
        }
        return true;
    }

    /*
    * Load events from already saved model if internet is not available or coming back from event detail
    * */
    private List<EventModel> loadEventsFromCache() {
        File file = new File(getCacheDir()+ Util.EVENTS_FILE_NAME);
        if (!file.exists()) {
            Log.w(TAG, "Event cache does not exist");
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            int numEvents = ois.readInt();
            List<EventModel> events = new ArrayList<EventModel>();
            for (int i = 0; i < numEvents; ++i)
                events.add((EventModel) ois.readObject());
            ois.close();
            return events;
        } catch (IOException e) {
            Log.w(TAG, "Cannot load events from cache file", e);
            return null;
        } catch (ClassNotFoundException | ClassCastException e) {
            Log.e(TAG, "Weird class error when loading events from cache file", e);
            return null;
        }
    }
}