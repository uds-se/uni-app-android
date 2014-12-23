package de.unisaarland.UniApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import de.unisaarland.UniApp.about.AboutActicvity;
import de.unisaarland.UniApp.bus.BusActivity;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.events.EventsActivity;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.news.NewsActivity;
import de.unisaarland.UniApp.restaurant.RestaurantActivity;
import de.unisaarland.UniApp.staff.SearchStaffActivity;

/**
 * Launcher Activity of the application this Activity will be displayed when application is launched from the launcher
 * */
public class MainActivity extends ActionBarActivity {

    //Buttons to perform actions
    private Button newsButton = null;
    private Button restaurantButton = null;
    private Button campusButton = null;
    private Button eventsButton = null;
    private Button busButton = null;
    private Button staffSearchButton = null;
    private TextView campusText = null;

    /*
    * Will be called when activity created first time e.g. from scratch
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    /*
    * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
    * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
    * */
    @Override
    protected void onResume() {
        // sets the custom navigation bar according to each activity.
        //setActionBar();
        setContentView(R.layout.main);
        // set Listeners for the main screen to launch specific activity
        setButtonListeners();
        // Set Text on the Mainscreen
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String campus = settings.getString(SettingsActivity.KEY_CAMPUS_CHOOSER, "saar");
        campusText = (TextView) findViewById(R.id.campusText);
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            if (campus.equals("saar"))
                campusText.setText(getResources().getString(R.string.c_saarbruecken));
            else
                campusText.setText(getResources().getString(R.string.c_homburg));
        }
        setPreferences();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
                /*
        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        Boolean uni_saar = settings.getBoolean(Util.CAMPUS_SAAR,true);
        String campus = uni_saar? getResources().getString(R.string.c_saarbruecken): getResources().getString(R.string.c_homburg);
        menu.findItem(R.id.action_campus_chooser).setTitle(campus);
        */
        return super.onCreateOptionsMenu(menu);
    }

    // Handling the Action Bar Buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_campus_chooser:
                showSettings();
                return true;
            case R.id.action_about:
                Intent myIntent = new Intent(MainActivity.this, AboutActicvity.class);
                MainActivity.this.startActivity(myIntent);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettings(){
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(myIntent);


    }


    /*
    * set the preference for events, news and staff search so that in case if activity is
    * loaded again (from main activity) then these items should be downloaded from internet again
    * otherwise these items are already loaded and models are already built so they will be displayed from there.
    * */
    private void setPreferences() {
        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Util.NEWS_LOADED, false);
        editor.putBoolean(Util.EVENTS_LOADED, false);
        editor.putBoolean(Util.MENSA_ITEMS_LOADED, false);
        editor.commit();
        File f = new File(getFilesDir().getAbsolutePath()+ Util.TEMP_STAFF_SEARCH_FILE);
        if(f.exists()) {
            f.delete();
        }

        //If App is used for the first time...
        if (settings.getBoolean(Util.FIRST_TIME, true)) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            showSettings();
            settings.edit().putBoolean(Util.FIRST_TIME, false).commit();
        }
    }

    private void setButtonListeners() {

        newsButton = (Button) findViewById(R.id.newsBtn);
        newsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent myIntent = new Intent(MainActivity.this, NewsActivity.class);
            MainActivity.this.startActivity(myIntent);

            }
        });
        restaurantButton = (Button) findViewById(R.id.restaurantBtn);
        restaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, RestaurantActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });
        campusButton = (Button) findViewById(R.id.campusBtn);
        campusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, CampusActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });
        eventsButton = (Button) findViewById(R.id.eventsBtn);
        eventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, EventsActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });
        busButton = (Button) findViewById(R.id.busBtn);
        busButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, BusActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });
        staffSearchButton = (Button) findViewById(R.id.staffBtn);
        staffSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SearchStaffActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });
    }
}