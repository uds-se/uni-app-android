package de.unisaarland.UniApp.staff;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.networkcommunicator.Util;


public class SearchStaffActivity extends ActionBarActivity implements OnCheckedChangeListener {
    private RadioGroup radioChooser;
    private TextView lastName;
    private TextView firstName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == 1){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        else{
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        //TODO: Hide Keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        setContentView(R.layout.search_staff_layout);
        setActionBar();
        radioChooser = (RadioGroup) findViewById(R.id.radioChooser);
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

        SharedPreferences prefs = getSharedPreferences(Util.PREFS_NAME, 0);
        int lastChecked = prefs.getInt(Util.STAFF_LAST_SELECTION, R.id.rb_only_prof);
        radioChooser.check(lastChecked);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences(Util.PREFS_NAME, 0).edit();
                editor.putInt(Util.STAFF_LAST_SELECTION, radioChooser.getCheckedRadioButtonId());
                editor.commit();

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
                    String allQueryURL  = "https://www.lsf.uni-saarland.de/qisserver/rds?state=wsearchv&search=7&purge=y&moduleParameter=person/person&personal.vorname=%s&personal.nachname=%s&P_start=0&P_anzahl=50&_form=display";
                    String profQueryURL = "https://www.lsf.uni-saarland.de/qisserver/rds?state=wsearchv&search=7&purge=y&moduleParameter=person/person&choice.r_funktion.pfid=y&r_funktion.pfid=171&personal.vorname=%s&personal.nachname=%s&P_start=0&P_anzahl=50&_form=display";
                    String queryURLRaw = radioChooser.getCheckedRadioButtonId() == R.id.rb_only_prof
                            ? profQueryURL : allQueryURL;
                    String queryURL = String.format(queryURLRaw, Uri.encode(fstNam), Uri.encode(lstNam));
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
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.search_for_staff_text);
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