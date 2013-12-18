package com.st.cs.unisaarland.SaarlandUniversityApp.restaurant;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;

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

        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.opening_hours);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.restaurantText);
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        ImageButton backButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.back_icon);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new BackButtonClickListener(this));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
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
}