package de.unisaarland.UniApp.restaurant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.SettingsActivity;
import de.unisaarland.UniApp.networkcommunicator.INetworkLoaderDelegate;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.networkcommunicator.WebFetcher;
import de.unisaarland.UniApp.restaurant.model.AusLanderCafeParser;
import de.unisaarland.UniApp.restaurant.model.IMensaResultDelegate;
import de.unisaarland.UniApp.restaurant.model.MensaItem;
import de.unisaarland.UniApp.restaurant.model.MensaXMLParser;
import de.unisaarland.UniApp.restaurant.uihelper.CircleFlowIndicator;
import de.unisaarland.UniApp.restaurant.uihelper.ViewFlow;
import de.unisaarland.UniApp.restaurant.uihelper.ViewFlowAdapter;

public class RestaurantActivity extends ActionBarActivity {

    private final String TAG = RestaurantActivity.class.getSimpleName();

    private final String RESTAURANT_FILE_NAME = "restaurant_sb.dat";
    private ProgressBar bar;
    private WebFetcher mensaFetcher = null;
    private String backText = null;

    private final String MENSA_URL_SB = "http://studentenwerk-saarland.de/_menu/actual/speiseplan-saarbruecken.xml";
    private final String MENSA_URL_HOM = "http://studentenwerk-saarland.de/_menu/actual/speiseplan-homburg.xml";
    private final String AUS_CAFE_URL = "http://www.uni-saarland.de/campus/service-und-kultur/gastronomieaufdemcampus/auslaender-cafe.html";

    private HashMap<String,ArrayList<MensaItem>> mensaItemsDictionary = null;
    private ArrayList<String> keysList = null;

    INetworkLoaderDelegate mensaDelegate = new INetworkLoaderDelegate() {
        /*
        * Will be called in case of failure e.g internet connection problem
        * Will try to load mensa information from already stored model or in case if that model is not present will show the
        * error dialog
        * */
        @Override
        public void onFailure(String message) {
            if (restaurantFileExist()){
                loadMensaItemsFromSavedFile();
                bar.clearAnimation();
                bar.setVisibility(View.INVISIBLE);
                mensaFetcher.invalidateRequest();
                setContentView(R.layout.restaurant_layout);
                populateMensaItems();
            } else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(RestaurantActivity.this);
                builder1.setMessage(message);
                builder1.setCancelable(true);
                builder1.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                bar.clearAnimation();
                                bar.setVisibility(View.INVISIBLE);
                                mensaFetcher.invalidateRequest();
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
        * call the Mensa parser to parse the resultant file and return the map of mensa models to specified call back method.
        * */
        @Override
        public void onSuccess(InputStream data) {
            MensaXMLParser mensaParser = new MensaXMLParser(mensaResultDelegate);
            try {
                mensaParser.parse(data);
            } catch (XmlPullParserException e) {
                Log.e("MyTag,", e.getMessage());
            } catch (IOException e) {
                Log.e("MyTag,", e.getMessage());
            }
        }
    };

    private IMensaResultDelegate mensaResultDelegate = new IMensaResultDelegate() {
        // will receive the map of mensa items and will call the Auslandercafe to parse its items and append
        // in the map with a specific list.
        @Override
        public void mensaItemsList(HashMap<String,ArrayList<MensaItem>> daysDictionary) {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String uni_saar = settings.getString(SettingsActivity.KEY_CAMPUS_CHOOSER, "saar");
            if (uni_saar.equals("saar"))
                new AusLanderCafeParser(auslanderResultDelegate,AUS_CAFE_URL,daysDictionary).parse();
            else
                setMensaEntries(daysDictionary);
        }
    };

    // call back method of AuslanderCafeParser will sort the list and invalidate the network request
    private IMensaResultDelegate auslanderResultDelegate = new IMensaResultDelegate() {
        @Override
        public void mensaItemsList(HashMap<String, ArrayList<MensaItem>> daysDictionary) {
            setMensaEntries(daysDictionary);

        }
    };

    private void setMensaEntries(HashMap<String,ArrayList<MensaItem>> daysDictionary){
        mensaItemsDictionary = daysDictionary;
        Set set = mensaItemsDictionary.keySet();
        keysList = new ArrayList<String>(mensaItemsDictionary.size());
        Iterator<String> iter = set.iterator();
        while (iter.hasNext()){
            keysList.add(iter.next());
        }

        Collections.sort(keysList);
        mensaFetcher.invalidateRequest();
        removeLoadingView();

    }


    // will remove the loading view and save the current maensa items in a file
    private void removeLoadingView() {
        if(bar!=null){
            bar.clearAnimation();
            bar.setVisibility(View.INVISIBLE);
        }
        setContentView(R.layout.restaurant_layout);
        boolean itemsSaved = savMensaItemsToFile();
        if (itemsSaved) {
            //Log.i("MyTag", "News are saved");
        }
        populateMensaItems();
    }

    /*
    * after downloading and parsing the mensa items when models are built it will call the adapter and pass the
    * specified model to it so that it will display list of mensa items.
    * */
    private void populateMensaItems() {

        ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        viewFlow.setAdapter(new ViewFlowAdapter(this,mensaItemsDictionary,keysList), 0);
        CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
        viewFlow.setFlowIndicator(indic);
    }

    /*
    * Will be called when activity created first time e.g. from scratch will have extras in intent if
    * it is being called from campus activity
    * */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        if(savedInstanceState!=null){
            backText = savedInstanceState.getString("back");
        }
        setActionBar();
    }

    @Override
    protected void onStart() {
        /*
        * Checks if news are already loaded and models are built then no need to download the from internet again
        * e.g. if activity is just changed to see the details of any specific news and comes back to the news list
        * otherwise if it is being loaded from main activity page and internet is available it will be downloaded from internet
        * again.
        * */
        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        boolean isCopied = settings.getBoolean(Util.MENSA_ITEMS_LOADED,false);
        if(!isCopied){
            addLoadingView();
        }else{
            loadMensaItemsFromSavedFile();
            setContentView(R.layout.restaurant_layout);
            populateMensaItems();
        }
        super.onStart();
    }

    //displays the loading view and download and parse the mensa items from internet
    private void addLoadingView() {
        setContentView(R.layout.loading_layout);
        bar = (ProgressBar) findViewById(R.id.progress_bar);
        // safety check in case user press the back button then bar will be null
        if(bar!=null){
            bar.animate();
        }
        /**
         * Calls the custom class to connect and download the specific XML and pass the delegate method which will be called
         * in case of success and failure
         */
        mensaFetcher = new WebFetcher(mensaDelegate);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String uni_saar = settings.getString(SettingsActivity.KEY_CAMPUS_CHOOSER, "saar");
        String MENSA_URL = uni_saar.equals("saar") ? MENSA_URL_SB : MENSA_URL_HOM;
        mensaFetcher.startFetchingAsynchronously(MENSA_URL, this);
    }

    /*
    * Save current mensa model to file (temporary) so that these will be used later in case if user don't have internet connection
    * */
    private boolean savMensaItemsToFile(){
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(getCacheDir()+ RESTAURANT_FILE_NAME)));
            oos.writeObject(mensaItemsDictionary);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean restaurantFileExist() {
        File f = new File(getCacheDir()+ RESTAURANT_FILE_NAME);
        return f.exists();
    }
    private void removeOldDataFromFile() {
            boolean changed = false;
            Set set = mensaItemsDictionary.keySet();
            ArrayList<String> temp = new ArrayList<String>(mensaItemsDictionary.size());
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()){
                String tempDate = iter.next();
                long date = Long.parseLong(tempDate)*1000;
                Date now = new Date();
                Date tagDate = new Date(date);
                now.setHours(0);
                now.setMinutes(0);
                now.setSeconds(0);
                if(now.before(tagDate) ||
                        (now.getDate() == tagDate.getDate() && now.getMonth() == tagDate.getMonth())) {

                }else{
                    temp.add(tempDate);
                    changed = true;
                }
            }

            if(changed){
                for (int i=0;i<temp.size();i++){
                    mensaItemsDictionary.remove(temp.get(i));
                }
                savMensaItemsToFile();
            }
    }

    private void loadMensaItemsFromSavedFile() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(getCacheDir()+ RESTAURANT_FILE_NAME)));
            mensaItemsDictionary = (HashMap<String, ArrayList<MensaItem>>) ois.readObject();
            ois.close();
            removeOldDataFromFile();
            Set set = mensaItemsDictionary.keySet();
            keysList = new ArrayList<String>(mensaItemsDictionary.size());
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()){
                keysList.add(iter.next());
            }

            Collections.sort(keysList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // set custom navigation bar
    private void setActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //Enable Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.mensa_text);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.restaurant_activity_icons, menu);
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
            case R.id.action_opening_hours:
                //Open opening hours actions when button is pressed
                Intent myIntent = new Intent(RestaurantActivity.this, OpeningHoursActivity.class);
                RestaurantActivity.this.startActivity(myIntent);

                return true;
        }
        return super.onOptionsItemSelected(item);
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

    /*
   * Called when back button is pressed either from device or navigation bar.
   * */
    @Override
    public void onBackPressed() {
        backText = null;
        bar = null;
        super.onBackPressed();
    }
}