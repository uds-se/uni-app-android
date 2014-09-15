package de.unisaarland.UniApp;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * Created by Janek on 15.09.2014.
 */
public class SettingsActivity extends PreferenceActivity {

    public static final String KEY_CAMPUS_CHOOSER= "pref_campus_chooser";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getResources().getString(R.string.settings));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}