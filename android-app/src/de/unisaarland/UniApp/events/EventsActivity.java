package de.unisaarland.UniApp.events;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.events.model.EventsModel;
import de.unisaarland.UniApp.events.model.EventsXMLParser;
import de.unisaarland.UniApp.events.model.IEventsResultDelegate;
import de.unisaarland.UniApp.events.uihelper.EventsAdapter;
import de.unisaarland.UniApp.networkcommunicator.INetworkLoaderDelegate;
import de.unisaarland.UniApp.networkcommunicator.NetworkHandler;
import de.unisaarland.UniApp.networkcommunicator.Util;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/1/13
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventsActivity extends Activity {

    private ProgressBar bar;
    private ArrayList<EventsModel> eventModelsArray;
    private final String URL = "http://www.uni-saarland.de/aktuelles/veranstaltungen.html?type=100&tx_ttnews[cat]=30";
    private NetworkHandler networkHandler = null;
    private final String EVENTS_FILE_NAME = "events.dat";

    /*
    * Will be called when activity created first time e.g. from scratch
    * */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
    * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
    * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
    * */
    @Override
    protected void onResume() {
        super.onResume();
        setActionBar();

        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        boolean isCopied = settings.getBoolean(Util.EVENTS_LOADED,false);
        if(!isCopied){
            addLoadingView();
        }else{
            loadEventsFromSavedFile();
            setContentView(R.layout.news_panel);
            populateEventItems();
        }
    }

    /*
    * Called when back button is pressed either from device or navigation bar.
    * */
    @Override
    public void onBackPressed() {
        // will invalidate the connection establishment request if it is not being completed yet and free the resources
        if(networkHandler != null){
            networkHandler.invalidateRequest();
        }
        super.onBackPressed();
    }

    /**
     * sets the custom navigation bar according to each activity.
     */
    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.eventsText);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.homeText);
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        ImageButton backButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.back_icon);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new BackButtonClickListener(this));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    }

    private void addLoadingView() {
        //displays the loading view and download and parse the event items from internet
        setContentView(R.layout.loading_layout);
        // safety check in case user press the back button then bar will be null
        if(bar != null){
            bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.animate();
        }
        /**
         * Calls the custom class to connect and download the specific XML and pass the delegate method which will be called
         * in case of success and failure
         */
        networkHandler = new NetworkHandler(delegate);
        networkHandler.connect(URL, this);
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

    INetworkLoaderDelegate delegate = new INetworkLoaderDelegate() {
        /*
        * Will be called in case of failure e.g internet connection problem
        * Will try to load events from already stored model or in case if that model is not present will show the
        * error dialog
        * */
        @Override
        public void onFailure(String message) {
            if (eventsFileExist()){
                loadEventsFromSavedFile();
                if(bar!=null){
                    bar.clearAnimation();
                    bar.setVisibility(View.INVISIBLE);
                }
                setContentView(R.layout.news_panel);
                populateEventItems();
            } else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(EventsActivity.this);
                builder1.setMessage(message);
                builder1.setCancelable(true);
                builder1.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(bar!=null){
                                    bar.clearAnimation();
                                    bar.setVisibility(View.INVISIBLE);
                                }
                                dialog.cancel();
                                onBackPressed();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }

        /*
        * Will be called in case of success if connection is successfully established and parser is ready
        * call the Events parser to parse the resultant file and return the list of event models to specified call back method.
        * */
        @Override
        public void onSuccess(XmlPullParser parser) {
            EventsXMLParser eventsParser = new EventsXMLParser(eventsResultDelegate);
            try {
                eventsParser.parse(parser);
            } catch (XmlPullParserException e) {
                Log.e("MyTag,", e.getMessage());
            } catch (IOException e) {
                Log.e("MyTag,", e.getMessage());
            }
        }
    };

    /*
    * Call back method of EventResultDelegate will be called when all events are parsed and Model list is generated
    * */
    private IEventsResultDelegate eventsResultDelegate = new IEventsResultDelegate() {
        @Override
        public void eventsList(ArrayList<EventsModel> eventsModels) {
            eventModelsArray = eventsModels;
            Collections.reverse(eventModelsArray);
            removeLoadingView();
        }
    };

    /*
    * Will remove the loading view as events are being downloaded and parsed also the models are built
    * Will save the current events model
    * and show the event list.
    * */
    private void removeLoadingView() {
        if(bar!=null){
            bar.clearAnimation();
            bar.setVisibility(View.INVISIBLE);
        }
        setContentView(R.layout.news_panel);
        boolean itemsSaved = saveCurrentEventItemsToFile();
        if(itemsSaved){
            Log.i("MyTag","Events are saved");
        }
        populateEventItems();
    }

    /*
    * after downloading and parsing the events when models are built it will call the adapter and pass the
    * specified model to it so that it will display list of event items.
    * */
    private void populateEventItems() {
        ListView eventsList = (ListView) findViewById(R.id.newsItemListView);
        eventsList.setAdapter(new EventsAdapter(this,eventModelsArray));
    }

    /*
    * Save current event model to file (temporary) so that these will be used later in case if user don't have internet connection
    * and also if user is coming back from seeing a detailed event.
    * */
    private boolean saveCurrentEventItemsToFile() {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir().getAbsolutePath()+ EVENTS_FILE_NAME)));
            oos.writeObject(eventModelsArray);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
    * Load events from already saved model if internet is not available or coming back from event detail
    * */
    private void loadEventsFromSavedFile() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(getFilesDir().getAbsolutePath()+ EVENTS_FILE_NAME)));
            eventModelsArray = (ArrayList<EventsModel>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Check if event file already exist.
    * */
    private boolean eventsFileExist() {
        File f = new File(getFilesDir().getAbsolutePath()+ EVENTS_FILE_NAME);
        if(f.exists()) {
            return true;
        }
        return false;
    }

    // release the resources.
    @Override
    protected void onStop() {
        if(eventModelsArray != null){
            eventModelsArray.clear();
        }
        bar = null;
        networkHandler = null;
        eventModelsArray = null;
        super.onStop();
    }
}