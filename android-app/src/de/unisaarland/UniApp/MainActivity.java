package de.unisaarland.UniApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.unisaarland.UniApp.about.AboutActivity;
import de.unisaarland.UniApp.bus.BusActivity;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.restaurant.RestaurantActivity;
import de.unisaarland.UniApp.restaurant.notifications.MensaNotifications;
import de.unisaarland.UniApp.rssViews.RSSActivity;
import de.unisaarland.UniApp.settings.SettingsActivity;
import de.unisaarland.UniApp.staff.SearchStaffActivity;

/**
 * Launcher Activity of the application this Activity will be displayed when application is launched from the launcher
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // sets the custom navigation bar according to each activity.
        setContentView(R.layout.main);
        // set Listeners for the main screen to launch specific activity
        setButtonListeners();

        // And set the mensa preferences (is not strictly needed, since each alarm should trigger
        // the next one, but for the case that something goes wrong...)
        new MensaNotifications(this).setNext();
    }

    /**
     * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
     * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // If this is the first start, show preferences...
        if (!settings.contains(getString(R.string.pref_campus))) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        // take special care in preceeding code to use default values on the settings, as they
        // might not have been set yet...

        // Set Text on the Mainscreen
        String campus = settings.getString(getString(R.string.pref_campus),
                getString(R.string.pref_campus_saar));
        TextView campusText = (TextView) findViewById(R.id.campusText);
        // unfortunately, campusText happens to be null sometimes.
        if (campusText != null) {
            int text = campus.equals(getString(R.string.pref_campus_saar))
                    ? R.string.c_saarbruecken : R.string.c_homburg;
            campusText.setText(text);
        }

        showWhatsNew(settings);
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
            case R.id.show_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setButtonListeners() {

        Button newsButton = (Button) findViewById(R.id.newsBtn);
        newsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, RSSActivity.class);
                myIntent.putExtra("category", RSSActivity.Category.News);
                MainActivity.this.startActivity(myIntent);
            }
        });
        Button restaurantButton = (Button) findViewById(R.id.restaurantBtn);
        restaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, RestaurantActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
        Button campusButton = (Button) findViewById(R.id.campusBtn);
        campusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, CampusActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
        Button eventsButton = (Button) findViewById(R.id.eventsBtn);
        eventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, RSSActivity.class);
                myIntent.putExtra("category", RSSActivity.Category.Events);
                MainActivity.this.startActivity(myIntent);
            }
        });
        Button busButton = (Button) findViewById(R.id.busBtn);
        busButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, BusActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
        Button staffSearchButton = (Button) findViewById(R.id.staffBtn);
        staffSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SearchStaffActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
    }

    private void showWhatsNew(SharedPreferences settings) {
        // Check whether we need to show the "what's new" dialog...
        int currentVersionNumber = BuildConfig.VERSION_CODE;
        int savedVersionNumber = settings.getInt(getString(R.string.pref_last_whatsnew_version), 0);
        if (currentVersionNumber == savedVersionNumber)
            return;

        int currentHash = getString(R.string.whatsnew_text).hashCode();
        int savedHash = settings.getInt(getString(R.string.pref_last_whatsnew_hash), 0);
        if (currentHash == savedHash)
            return;

        settings.edit().
                putInt(getString(R.string.pref_last_whatsnew_version), currentVersionNumber).
                putInt(getString(R.string.pref_last_whatsnew_hash), currentHash).
                commit();

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.whatsnew_title)
                .setMessage(R.string.whatsnew_text)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).show();
    }
}