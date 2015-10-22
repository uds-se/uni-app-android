package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.utils.Util;


public class SearchStaffActivity extends ActionBarActivity {
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

        ActionBar actionBar = getSupportActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.search_for_staff_text);

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int lastChecked = prefs.getInt(getString(R.string.pref_staff_search_prof_sel), R.id.rb_only_prof);
        String lastFirstName = prefs.getString(getString(R.string.pref_staff_search_fstnam), "");
        String lastLastName = prefs.getString(getString(R.string.pref_staff_search_lstnam), "");
        radioChooser.check(lastChecked);
        firstName.setText(lastFirstName);
        lastName.setText(lastLastName);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String lstNam = lastName.getText().toString();
                lstNam = lstNam.trim();
                String fstNam = firstName.getText().toString();
                fstNam = fstNam.trim();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SearchStaffActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(getString(R.string.pref_staff_search_prof_sel), radioChooser.getCheckedRadioButtonId());
                editor.putString(getString(R.string.pref_staff_search_fstnam), fstNam);
                editor.putString(getString(R.string.pref_staff_search_lstnam), lstNam);
                editor.commit();

                if (fstNam.length() ==0 && lstNam.length() == 0) {
                    new AlertDialog.Builder(SearchStaffActivity.this)
                            .setTitle(R.string.empty_search_field)
                            .setMessage(R.string.fill_at_least_one)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                            .create().show();
                } else if (Util.isConnectedToInternet(SearchStaffActivity.this)) {
                    String profPart = "";
                    if (radioChooser.getCheckedRadioButtonId() == R.id.rb_only_prof)
                        profPart = "choice.r_funktion.pfid=y&r_funktion.pfid=171&";
                    String searchURL  = "https://www.lsf.uni-saarland.de/qisserver/rds?"+
                            profPart+"state=wsearchv&search=7&purge=y&moduleParameter=person/person"+
                            "&personal.vorname="+Uri.encode(fstNam)+"&personal.nachname="+
                            Uri.encode(lstNam)+"&P_start=0&P_anzahl=50&_form=display";
                    Intent myIntent = new Intent(SearchStaffActivity.this, SearchResultActivity.class);
                    myIntent.putExtra("url", searchURL);
                    SearchStaffActivity.this.startActivity(myIntent);
                } else {
                    new AlertDialog.Builder(SearchStaffActivity.this)
                            .setMessage(R.string.check_internet_message)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                            .create().show();
                }
            }
        });

    }
}