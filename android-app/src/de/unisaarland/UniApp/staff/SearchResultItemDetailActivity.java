package de.unisaarland.UniApp.staff;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.campus.CampusActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/13/13
 * Time: 12:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultItemDetailActivity extends ActionBarActivity {
    private String url = null;
    private ProgressBar pBar;
    private String name;
    private String gender;
    private String academicDegree;
    private String building;
    private String room;
    private String phone;
    private String fax;
    private String email;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        url = savedInstanceState.getString("url");
    }

    @Override
    public void onBackPressed() {
        url = null;
        pBar = null;
        name = null;
        gender = null;
        academicDegree = null;
        building = null;
        room = null;
        phone = null;
        fax = null;
        email = null;
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setContentView(R.layout.search_result_detail_layout);
        pBar = (ProgressBar) findViewById(R.id.web_view_progress_bar);
        if(name == null){
            getTask(url).execute();
        }else{
            showResult(name, gender, academicDegree, building, room, phone, fax, email);
        }
        setActionBar();
    }

    private AsyncTask<Void,Void,Integer> getTask(final String url){
        return new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Document doc = null;
                try {
                    doc = Jsoup.connect(url).get();
                } catch (IOException e) {
                    Log.e("MyTag", e.getMessage());
                }
                Elements tableElements = doc.getElementsByTag("table");
                for(Element tableElement: tableElements){
                    if(tableElement.attr("summary").equals("Grunddaten zur Veranstaltung")){
                        Elements tdElements = tableElement.child(1).getElementsByTag("td");
                        String lastName = tdElements.get(0).text();
                        gender = tdElements.get(1).text();
                        gender = gender.substring(0, 1).toUpperCase() + gender.substring(1);
                        String firstName = tdElements.get(2).text();
                        name = String.format("%s %s", firstName, lastName);
                        academicDegree = tdElements.get(8).text();
                    }
                    if(tableElement.attr("summary").equals("Angaben zur Dienstadresse")){
                        Elements tdElements = tableElement.child(0).getElementsByTag("td");
                        phone = tdElements.get(1).text();
                        fax = tdElements.get(3).text();
                        email = tdElements.get(5).tagName("a").text();
                        room = tdElements.get(6).tagName("a").text();
                        String a = tdElements.get(7).text();
                        building = tdElements.get(8).tagName("a").text();
                        String[] tempArray = building.split(" ");
                        if(tempArray.length>1){
                            if(tempArray.length == 3){
                                building = tempArray[1]+tempArray[2];
                            }else{
                                building = tempArray[1];
                            }
                        }
                    }
                }
                return 1;
            }

            @Override
            protected void onPostExecute(Integer i) {
                showResult(name, gender, academicDegree, building, room, phone, fax, email);
            }
        };
    }

    private void showResult(String name, String gender, String academicDegree, String building, String room, String phone, String fax, String email) {
        if(pBar!=null){
            pBar.setVisibility(View.INVISIBLE);
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            TextView genderText = (TextView) findViewById(R.id.gendertext);
            genderText.setVisibility(View.VISIBLE);
            Button genderButton = (Button) findViewById(R.id.gender);
            genderButton.setText(gender);
            genderButton.setVisibility(View.VISIBLE);
            TextView academicDegreeText = (TextView) findViewById(R.id.academicText);
            academicDegreeText.setVisibility(View.VISIBLE);
            Button academicDegreeButton = (Button) findViewById(R.id.academic_degree);
            academicDegreeButton.setText(academicDegree);
            academicDegreeButton.setVisibility(View.VISIBLE);
            TextView buildingText = (TextView) findViewById(R.id.buildingText);
            buildingText.setVisibility(View.VISIBLE);
            final Button buildingButton = (Button) findViewById(R.id.building);
            ImageButton buildingButtonForwardIcon = (ImageButton) findViewById(R.id.building_forward_icon);
            if(building!=null && !building.equals("")){
                buildingButtonForwardIcon.setVisibility(View.VISIBLE);
                buildingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(SearchResultItemDetailActivity.this, CampusActivity.class);
                        myIntent.putExtra("building", buildingButton.getText());
                        SearchResultItemDetailActivity.this.startActivity(myIntent);

                    }
                });
            }
            buildingButton.setText(building);
            buildingButton.setVisibility(View.VISIBLE);
            TextView roomText = (TextView) findViewById(R.id.roomText);
            roomText.setVisibility(View.VISIBLE);
            Button roomButton = (Button) findViewById(R.id.room);
            roomButton.setText(room);
            roomButton.setVisibility(View.VISIBLE);
            TextView phoneText = (TextView) findViewById(R.id.phoneText);
            phoneText.setVisibility(View.VISIBLE);
            Button phoneButton = (Button) findViewById(R.id.phone);
            phoneButton.setText(phone);
            phoneButton.setVisibility(View.VISIBLE);
            TextView faxText = (TextView) findViewById(R.id.faxText);
            faxText.setVisibility(View.VISIBLE);
            Button faxButton = (Button) findViewById(R.id.fax);
            faxButton.setText(fax);
            faxButton.setVisibility(View.VISIBLE);
            TextView emailText = (TextView) findViewById(R.id.emailText);
            emailText.setVisibility(View.VISIBLE);
            Button emailButton = (Button) findViewById(R.id.email);
            emailButton.setText(email);
            emailButton.setVisibility(View.VISIBLE);
            Button moreButton = (Button) findViewById(R.id.more);
            ImageButton moreButtonForwardIcon = (ImageButton) findViewById(R.id.more_forward_icon);
            moreButtonForwardIcon.setVisibility(View.VISIBLE);
            moreButton.setVisibility(View.VISIBLE);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(SearchResultItemDetailActivity.this, PersonDetailWebActivity.class);
                    myIntent.putExtra("url", url);
                    SearchResultItemDetailActivity.this.startActivity(myIntent);

                }
            });
        }
    }

    private void setActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //Enabling UP-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.info);

    }

    // Handling the Action Bar Buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
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
}