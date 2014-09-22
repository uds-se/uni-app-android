package de.unisaarland.UniApp.staff;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.staff.uihelper.SearchResultAdapter;

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
                    doc = Jsoup.connect(url).timeout(15*1000).get();
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
                                // filter out all leading "Prof.", "Dr.", "rer." ...
                                StringBuilder name = new StringBuilder();
                                boolean titlePart = true;
                                for (String namePart : nameArray) {
                                    if (!namePart.endsWith(".") &&
                                            !namePart.endsWith(".-"))
                                        titlePart = false;
                                    if (!titlePart)
                                        name.append(" ").append(namePart);
                                }
                                String url = nameElement.attr("href");
                                //safety check in case user press the back button of device
                                if (namesArray != null && linksArray != null) {
                                    namesArray.add(name.substring(1));
                                    linksArray.add(url);
                                }
                            }
                        }

                    }
                }
                return 1;
                } catch (IOException e) {
                   return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer i) {
                //if data fetching was successfull
                if (i == 1)
                showSearchResults();
                //else show error message and dismiss view
                else{
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SearchResultActivity.this);
                    builder1.setMessage(getString(R.string.not_connected));
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        };
    }

    private void showSearchResults() {
        if(pBar != null) {
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
                if(body != null){
                    body.setVisibility(View.VISIBLE);
                    SearchResultAdapter adapter = new SearchResultAdapter(this,namesArray,linksArray);
                    body.setAdapter(adapter);
                }
            }
        }
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.search_results);
        // add the custom view to the action bar
        /*
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.search_results);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.back);
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

}