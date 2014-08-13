package de.unisaarland.UniApp.restaurant;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.networkcommunicator.Util;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/7/13
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class OpeningHoursActivity extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        setActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opening_layout);
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.mensa_text);
        /*
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.opening_hours);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.mensa_text);
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        ImageButton backButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.back_icon);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new BackButtonClickListener(this));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);*/
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