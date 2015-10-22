package de.unisaarland.UniApp.restaurant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.uihelper.AuslanderCafeFragment;
import de.unisaarland.UniApp.restaurant.uihelper.HeroesCafeFragment;
import de.unisaarland.UniApp.restaurant.uihelper.JuristenCafeFragment;
import de.unisaarland.UniApp.restaurant.uihelper.MensaCafeFragment;
import de.unisaarland.UniApp.restaurant.uihelper.MensaFragment;
import de.unisaarland.UniApp.restaurant.uihelper.SupportFragmentTabListener;


//TODO: Add swipe to change Tabs
public class OpeningHoursActivity extends ActionBarActivity {

    private PagerAdapter mCollectionPagerAdapter;
    private ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String campus = settings.getString(getString(R.string.pref_campus), null);
        setActionBar();
        if (campus.equals(getString(R.string.pref_campus_saar))) {
            //setContentView(R.layout.opening_layout);
            setTabs();
        } else
            setContentView(R.layout.campus_hom_opening_hours);
    }

    private void setActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //Enable Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.opening_hours));

    }

    private void setTabs() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.addTab(actionBar.newTab().setTabListener(new SupportFragmentTabListener<MensaFragment>(
                this, "mensa", MensaFragment.class)).setText(R.string.mensa));
        actionBar.addTab(actionBar.newTab().setTabListener(new SupportFragmentTabListener<MensaCafeFragment>(
                this, "mensacafe", MensaCafeFragment.class)).setText(R.string.mensa_cafe));
        actionBar.addTab(actionBar.newTab().setTabListener(new SupportFragmentTabListener<AuslanderCafeFragment>(
                this, "auslaendercafe", AuslanderCafeFragment.class)).setText(R.string.auslander_cafe));
        actionBar.addTab(actionBar.newTab().setTabListener(new SupportFragmentTabListener<JuristenCafeFragment>(
                this, "juristencafe", JuristenCafeFragment.class)).setText(R.string.juristen_cafe));
        actionBar.addTab(actionBar.newTab().setTabListener(new SupportFragmentTabListener<HeroesCafeFragment>(
                this, "horoescafe", HeroesCafeFragment.class)).setText(R.string.heroes_cafe));
        actionBar.setNavigationMode(android.support.v7.app.ActionBar.NAVIGATION_MODE_TABS);
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
}
