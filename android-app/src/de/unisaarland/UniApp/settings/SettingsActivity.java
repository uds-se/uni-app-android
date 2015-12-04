package de.unisaarland.UniApp.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;

public class SettingsActivity extends UpNavigationActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    // Just setting the summary to "%s" does not work, as older android versions do not update
    // the label when the preference value changes (cmp.
    // http://stackoverflow.com/questions/7017082/change-the-summary-of-a-listpreference-with-the-new-value-android/15329652#15329652)
    // So we just add a new pref change listener and update the campus selection view ourselves.
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle paramBundle) {
            super.onCreate(paramBundle);
            addPreferencesFromResource(R.xml.preferences);
            getActivity().setTheme(R.style.AppTheme);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref0 = findPreference(key);
            if (!(pref0 instanceof ListPreference))
                return;
            ListPreference pref = (ListPreference) pref0;
            String value = pref.getValue();
            CharSequence[] entries = pref.getEntries();
            CharSequence[] entryValues = pref.getEntryValues();
            CharSequence summ = pref.getSummary();
            // check whether the current summary matches any entry. if so, update it with
            // the new entry.
            for (CharSequence cs : entries) {
                if (summ.toString().equals(cs.toString())) {
                    for (int i = 0; i < entryValues.length; ++i) {
                        if (entryValues[i].toString().equals(value)) {
                            pref.setSummary(entries[i]);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}