package de.unisaarland.UniApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.unisaarland.UniApp.about.AboutActicvity;
import de.unisaarland.UniApp.bus.BusActivity;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.restaurant.RestaurantActivity;
import de.unisaarland.UniApp.rssViews.RSSActivity;
import de.unisaarland.UniApp.staff.SearchStaffActivity;
import de.unisaarland.UniApp.utils.Util;

/**
 * Launcher Activity of the application this Activity will be displayed when application is launched from the launcher
 */
public class MainActivity extends ActionBarActivity {

    //Buttons to perform actions
    private Button newsButton = null;
    private Button restaurantButton = null;
    private Button campusButton = null;
    private Button eventsButton = null;
    private Button busButton = null;
    private Button staffSearchButton = null;
    private TextView campusText = null;

    /**
     * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
     * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
     */
    @Override
    protected void onResume() {
        // sets the custom navigation bar according to each activity.
        //setActionBar();
        setContentView(R.layout.main);
        // set Listeners for the main screen to launch specific activity
        setButtonListeners();
        // If this is the first start, show preferences...
        checkFirstStart();
        // Set Text on the Mainscreen
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String campus = settings.getString(Util.KEY_CAMPUS_CHOOSER, "saar");
        campusText = (TextView) findViewById(R.id.campusText);
        // unfortunately, campusText happens to be null sometimes.
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE
                && campusText != null) {
            int text = campus.equals("saar") ? R.string.c_saarbruecken : R.string.c_homburg;
            campusText.setText(text);
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
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


    private void checkFirstStart() {
        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);

        //If App is used for the first time...
        if (settings.getBoolean(Util.FIRST_TIME, true)) {
            settings.edit().putBoolean(Util.FIRST_TIME, false).commit();
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            showSettings();
        }
    }

    private void setButtonListeners() {

        newsButton = (Button) findViewById(R.id.newsBtn);
        newsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, RSSActivity.class);
                myIntent.putExtra("category", RSSActivity.Category.News);
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
                Intent myIntent = new Intent(MainActivity.this, RSSActivity.class);
                myIntent.putExtra("category", RSSActivity.Category.Events);
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