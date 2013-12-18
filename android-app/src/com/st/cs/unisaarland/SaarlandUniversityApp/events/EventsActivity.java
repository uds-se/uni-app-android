package com.st.cs.unisaarland.SaarlandUniversityApp.events;

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
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.events.model.EventsModel;
import com.st.cs.unisaarland.SaarlandUniversityApp.events.model.EventsXMLParser;
import com.st.cs.unisaarland.SaarlandUniversityApp.events.model.IEventsResultDelegate;
import com.st.cs.unisaarland.SaarlandUniversityApp.events.uihelper.EventsAdapter;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.INetworkLoaderDelegate;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.NetworkHandler;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.Util;
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


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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

    @Override
    public void onBackPressed() {
        if(networkHandler != null){
            networkHandler.invalidateRequest();
        }
        super.onBackPressed();
    }

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
        setContentView(R.layout.loading_layout);
        bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.animate();
        networkHandler = new NetworkHandler(delegate);
        networkHandler.connect(URL, this);
    }

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
        @Override
        public void onFailure(String message) {
            if (eventsFileExist()){
                loadEventsFromSavedFile();
                bar.clearAnimation();
                bar.setVisibility(View.INVISIBLE);
                setContentView(R.layout.news_panel);
                populateEventItems();
            } else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(EventsActivity.this);
                builder1.setMessage(message);
                builder1.setCancelable(true);
                builder1.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                bar.clearAnimation();
                                bar.setVisibility(View.INVISIBLE);
                                dialog.cancel();
                                onBackPressed();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }

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

    private IEventsResultDelegate eventsResultDelegate = new IEventsResultDelegate() {
        @Override
        public void eventsList(ArrayList<EventsModel> eventsModels) {
            eventModelsArray = eventsModels;
            Collections.reverse(eventModelsArray);
            removeLoadingView();
        }
    };

    private void removeLoadingView() {
        bar.clearAnimation();
        bar.setVisibility(View.INVISIBLE);
        setContentView(R.layout.news_panel);
        boolean itemsSaved = saveCurrentEventItemsToFile();
        if(itemsSaved){
            Log.i("MyTag","Events are saved");
        }
        populateEventItems();
    }

    private void populateEventItems() {
        ListView eventsList = (ListView) findViewById(R.id.newsItemListView);
        eventsList.setAdapter(new EventsAdapter(this,eventModelsArray));
    }

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

    private void loadEventsFromSavedFile() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(getFilesDir().getAbsolutePath()+ EVENTS_FILE_NAME)));
            eventModelsArray = (ArrayList<EventsModel>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean eventsFileExist() {
        File f = new File(getFilesDir().getAbsolutePath()+ EVENTS_FILE_NAME);
        if(f.exists()) {
            return true;
        }
        return false;
    }

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