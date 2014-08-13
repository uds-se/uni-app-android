package de.unisaarland.UniApp.staff;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.staff.uihelper.StaffRadioGroupChooser;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/3/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchStaffActivity extends Activity implements OnCheckedChangeListener {
    private StaffRadioGroupChooser radioGroupChooser;
    private TextView lastName;
    private TextView firstName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        setContentView(R.layout.search_staff_layout);
        radioGroupChooser = (StaffRadioGroupChooser) findViewById(R.id.segment_text);
        radioGroupChooser.setOnCheckedChangeListener(this);

        lastName = (TextView) findViewById(R.id.last_name);
        firstName = (TextView) findViewById(R.id.first_name);
        final Button searchButton = (Button) findViewById(R.id.searchBtn);
        firstName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchButton.performClick();
                return true;
            }
        });

        lastName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchButton.performClick();
                return true;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lstNam = lastName.getText().toString();
                lstNam = lstNam.trim();
                String fstNam = firstName.getText().toString();
                fstNam = fstNam.trim();
                if(fstNam.length() ==0 && lstNam.length() == 0){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SearchStaffActivity.this);
                    builder1.setTitle(getString(R.string.empty_search_field));
                    builder1.setMessage(getString(R.string.fill_at_least_one));
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else if(Util.isConnectedToInternet(SearchStaffActivity.this)){
                    String allQueryURL = String.format("https://www.lsf.uni-saarland.de/qisserver/rds?state=wsearchv&search=7&purge=y&moduleParameter=person/person&personal.vorname=%s&personal.nachname=%s&P_start=0&P_anzahl=40&_form=display",fstNam,lstNam);
                    String profQueryURL = String.format("https://www.lsf.uni-saarland.de/qisserver/rds?state=wsearchv&search=7&purge=y&moduleParameter=person/person&choice.r_funktion.pfid=y&r_funktion.pfid=171&personal.vorname=%s&personal.nachname=%s&P_start=0&P_anzahl=50&_form=display",fstNam,lstNam);
                    String queryURL = "";
                    if(radioGroupChooser.getCheckedRadioButtonId() == R.id.professors){
                        queryURL = profQueryURL;
                    }else{
                        queryURL = allQueryURL;
                    }
                    Intent myIntent = new Intent(SearchStaffActivity.this, SearchResultActivity.class);
                    myIntent.putExtra("url", queryURL);
                    SearchStaffActivity.this.startActivity(myIntent);
                } else{
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SearchStaffActivity.this);
                    builder1.setMessage(getString(R.string.check_internet_message));
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.search_for_staff_text);
        /*
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.search_for_staff_text);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.homeText);
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        ImageButton backButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.back_icon);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new BackButtonClickListener(this));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        */
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


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

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