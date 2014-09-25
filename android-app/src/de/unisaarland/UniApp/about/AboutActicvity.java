package de.unisaarland.UniApp.about;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.unisaarland.UniApp.R;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/10/13
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class AboutActicvity extends Activity {
    // email id on which the email is to be sent
    private final String EMAIL_ID = "uniapp@uni-saarland.de";

    /*
    * Will be called when activity created first time e.g. from scratch
    * */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        setContentView(R.layout.about_layout);
        try {
            final PackageManager packageManager = getPackageManager();
            final String versionName;
            final int versionCode;
            if (packageManager != null) {
                /*
                * get version number and package no from the manifest file and display it on about screen as
                * well as in the subject line of email.
                * */
                PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
                versionName = packageInfo.versionName;
                versionCode = packageInfo.versionCode;
                TextView version = (TextView) findViewById(R.id.version_number);
                version.setText("Version " + versionName + " (" + versionCode + ")");
            }else{
                versionName = null;
                versionCode = 0;
            }
            Button contact = (Button) findViewById(R.id.contact_btn);
            contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject) + " Version " + versionName + " (" + versionCode + ")");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { EMAIL_ID });
                        String emailBody = "";
                        sendIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
                        sendIntent.setType("message/rfc822");
                        AboutActicvity.this.startActivity(Intent.createChooser(sendIntent, "Send Email"));
                    }catch (Exception e){
                        Log.e("MyTag",e.getMessage());
                    }
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MyTag",e.getMessage());
        }
    }
    /**
     * sets the custom navigation bar according to each activity.
     */
    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.aboutText);
        /*
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.aboutText);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);*/
    }

    // Handling the Action Bar Buttons
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
