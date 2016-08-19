package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;


public class SearchStaffActivity extends UpNavigationActionBarActivity {

    private static String getSearchUrl(String firstName, String lastName, int function) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.lsf.uni-saarland.de/qisserver/rds?");
        if (function != 0) {
            sb.append("choice.r_funktion.pfid=y&r_funktion.pfid=").append(function).append("&");
        }
        sb.append("state=wsearchv&search=7&purge=y&moduleParameter=person/person&personal.vorname=");
        sb.append(Uri.encode(firstName)).append("&personal.nachname=").append(Uri.encode(lastName));
        sb.append("&P_start=0&P_anzahl=50&_form=display");
        return sb.toString();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
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
        */

        setContentView(R.layout.search_staff_layout);

        final RadioGroup radioChooser = (RadioGroup) findViewById(R.id.radioChooser);
        final TextView lastName = (TextView) findViewById(R.id.last_name);
        final TextView firstName = (TextView) findViewById(R.id.first_name);
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
        if (lastChecked == R.id.rb_only_prof || lastChecked == R.id.rb_all)
            radioChooser.check(lastChecked);
        firstName.setText(lastFirstName);
        lastName.setText(lastLastName);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String lstNam = lastName.getText().toString().trim();
                String fstNam = firstName.getText().toString().trim();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SearchStaffActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(getString(R.string.pref_staff_search_prof_sel), radioChooser.getCheckedRadioButtonId());
                editor.putString(getString(R.string.pref_staff_search_fstnam), fstNam);
                editor.putString(getString(R.string.pref_staff_search_lstnam), lstNam);
                editor.apply();

                if (fstNam.length() ==0 && lstNam.length() == 0) {
                    new AlertDialog.Builder(SearchStaffActivity.this)
                            .setTitle(R.string.empty_search_field)
                            .setMessage(R.string.fill_at_least_one)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                })
                            .create().show();
                } else {
                    String[] urls;
                    if (radioChooser.getCheckedRadioButtonId() == R.id.rb_only_prof) {
                        urls = new String[] {
                                getSearchUrl(fstNam, lstNam, 166),
                                getSearchUrl(fstNam, lstNam, 171)
                        };
                    } else {
                        urls = new String[]{
                                getSearchUrl(fstNam, lstNam, 0)
                        };
                    }
                    Intent myIntent = new Intent(SearchStaffActivity.this, SearchResultActivity.class);
                    myIntent.putExtra("urls", urls);
                    SearchStaffActivity.this.startActivity(myIntent);
                }
            }
        });
    }
}