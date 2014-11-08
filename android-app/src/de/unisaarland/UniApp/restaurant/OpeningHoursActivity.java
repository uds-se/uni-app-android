package de.unisaarland.UniApp.restaurant;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.SettingsActivity;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.restaurant.uihelper.AuslanderCafeFragment;
import de.unisaarland.UniApp.restaurant.uihelper.HeroesCafeFragment;
import de.unisaarland.UniApp.restaurant.uihelper.JuristenCafeFragment;
import de.unisaarland.UniApp.restaurant.uihelper.MensaCafeFragment;
import de.unisaarland.UniApp.restaurant.uihelper.MensaFragment;
import de.unisaarland.UniApp.restaurant.uihelper.OpeningTabListener;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/7/13
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */

//TODO: Add swipe to change Tabs
public class OpeningHoursActivity extends Activity {

    PagerAdapter mCollectionPagerAdapter;
    ViewPager mViewPager;
    ActionBar actionBar;

    public void onCreate(Bundle savedInstanceState) {
        setActionBar();
        super.onCreate(savedInstanceState);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String campus = settings.getString(SettingsActivity.KEY_CAMPUS_CHOOSER, "saar");
        if (campus.equals("saar")) {
            setContentView(R.layout.opening_layout);
            setTabs();
        }
        else
            setContentView(R.layout.campus_hom_opening_hours);

    }

    private void setActionBar() {
        actionBar = getActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.opening_hours));

    }

    private  void setTabs(){
        actionBar.addTab(actionBar.newTab().setTabListener(new OpeningTabListener<MensaFragment>(
                this, "mensa", MensaFragment.class)).setText(R.string.mensa));
        actionBar.addTab(actionBar.newTab().setTabListener(new OpeningTabListener<MensaCafeFragment>(
                this, "mensacafe", MensaCafeFragment.class)).setText(R.string.mensa_cafe));
        actionBar.addTab(actionBar.newTab().setTabListener(new OpeningTabListener<AuslanderCafeFragment>(
                this, "auslaendercafe", AuslanderCafeFragment.class)).setText(R.string.auslander_cafe));
        actionBar.addTab(actionBar.newTab().setTabListener(new OpeningTabListener<JuristenCafeFragment>(
                this, "juristencafe", JuristenCafeFragment.class)).setText(R.string.juristen_cafe));
        actionBar.addTab(actionBar.newTab().setTabListener(new OpeningTabListener<HeroesCafeFragment>(
                this, "horoescafe", HeroesCafeFragment.class)).setText(R.string.heroes_cafe));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
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

    @Override
    public void onBackPressed() {
        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        boolean isCopied = settings.getBoolean(Util.MENSA_ITEMS_LOADED,false);
        if(!isCopied){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Util.MENSA_ITEMS_LOADED, true);
            editor.commit();
        }
        super.onBackPressed();
    }
}
