package com.st.cs.unisaarland.SaarlandUniversityApp.staff;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.Util;
import com.st.cs.unisaarland.SaarlandUniversityApp.staff.uihelper.SearchResultAdapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/13/13
 * Time: 12:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultActivity extends Activity {
    private String url = null;
    private ArrayList<String> namesArray;
    private ArrayList<String> linksArray;
    private ListView body;
    private ProgressBar pBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        url = savedInstanceState.getString("url");
        namesArray = new ArrayList<String>();
        linksArray = new ArrayList<String>();
    }

    @Override
    public void onBackPressed() {
        namesArray = null;
        linksArray = null;
        body = null;
        pBar = null;
        deleteTempFile();
        super.onBackPressed();
    }

    private void deleteTempFile() {
        if(tempSearchFileExist()){
            File f = new File(getFilesDir().getAbsolutePath()+ Util.TEMP_STAFF_SEARCH_FILE);
            boolean delete = f.delete();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActionBar();
        setContentView(R.layout.search_result_layout);
        body = (ListView) findViewById(R.id.body);
        pBar = (ProgressBar) findViewById(R.id.web_view_progress_bar);
        pBar.setVisibility(View.VISIBLE);
        body.setVisibility(View.GONE);
        if(tempSearchFileExist()){
            loadSearchResultFromFile();
            showSearchResults();
        }else{
            getTask(url).execute();
        }
    }

    private void loadSearchResultFromFile() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(getFilesDir().getAbsolutePath()+ Util.TEMP_STAFF_SEARCH_FILE)));
            namesArray = (ArrayList<String>) ois.readObject();
            linksArray = (ArrayList<String>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean tempSearchFileExist() {
        File f = new File(getFilesDir().getAbsolutePath()+ Util.TEMP_STAFF_SEARCH_FILE);
        if(f.exists()) {
            return true;
        }
        return false;
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
                Elements divElements = doc.getElementsByTag("div");
                for(Element divElement: divElements){
                    if(divElement.className().equals("erg_list_entry")){
                        Elements ergListLabelElements = divElement.getElementsByAttributeValueContaining("class", "erg_list_label");
                        if(ergListLabelElements.size()>0){
                            Element timeElement = ergListLabelElements.get(0);
                            if(timeElement.ownText().equals("Name:")){
                                Elements aElements = divElement.getElementsByTag("a");
                                Element nameElement = aElements.get(0);
                                String rawName = nameElement.text();
                                String[] nameArray = rawName.split(" ");
                                String firstName = nameArray[nameArray.length-2];
                                String name = nameArray[nameArray.length-1];
                                name = String.format("%s %s",firstName,name);
                                String url = nameElement.attr("href");
                                namesArray.add(name);
                                linksArray.add(url);
                            }
                        }

                    }
                }
                return 1;
            }

            @Override
            protected void onPostExecute(Integer i) {
                showSearchResults();
            }
        };
    }

    private void showSearchResults() {
        pBar.setVisibility(View.INVISIBLE);
        if(namesArray.size() == 0){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle(getString(R.string.no_staff_member_title));
            builder1.setMessage(getString(R.string.no_staff_member_found_description));
            builder1.setCancelable(true);
            builder1.setPositiveButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            onBackPressed();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        } else {
            body.setVisibility(View.VISIBLE);
            SearchResultAdapter adapter = new SearchResultAdapter(this,namesArray,linksArray);
            body.setAdapter(adapter);
        }
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.search_results);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.search_for_staff_text);
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