package de.unisaarland.UniApp.events;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.events.model.EventModel;
import de.unisaarland.UniApp.networkcommunicator.Util;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/12/13
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventDetailActivity extends ActionBarActivity {
    private final String TAG = EventDetailActivity.class.getSimpleName();

    private EventModel model;
    private WebView body = null;
    private ProgressBar pBar = null;

    /*
    * Will be called when activity created as this activity is being created from scratch every time when user
    * wants to view a new event details so all work is being done in onCreate no need to separate the work
    * in onResume.
    * It gets the event model object from intent which is saved with name model.
    * */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        model = (EventModel) savedInstanceState.getSerializable("model");
        // sets the custom navigation bar according to each activity.
        setActionBar();
        setContentView(R.layout.news_detail);
        showEvent();
    }

    /*
    * Called when back button is pressed either from device or navigation bar.
    */
    @Override
    public void onBackPressed() {
        model = null;
        pBar = null;
        body = null;
        super.onBackPressed();
    }

    /*
   * show the loading bar and call the async thread to load the detailed page and parse it
   * */
    private void showEvent() {
        body = (WebView) findViewById(R.id.body);
        pBar = (ProgressBar) findViewById(R.id.web_view_progress_bar);
        pBar.setVisibility(View.VISIBLE);
        body.setVisibility(View.GONE);

        getTask(model).execute();
    }

    private AsyncTask<Void, Void, Boolean> getTask(final EventModel model){
        return new AsyncTask<Void, Void, Boolean>() {
            String date;
            String heading;
            String body;

            // It will only show the event description all other attributes of html will be omitted.
            @Override
            protected Boolean doInBackground(Void... params) {
                Document doc = null;
                String link = model.getLink();
                try {
                    doc = Jsoup.connect(link).timeout(15 * 1000).get();
                } catch (IOException e) {
                    Log.w(TAG, "Error loading event details from '" + link + "'", e);
                    return Boolean.FALSE;
                }

                Elements elements = doc.getElementsByTag("div");
                for (Element ele : elements) {
                    if (ele.className().equals("news-single-item")) {
                        Elements e1 = ele.getElementsByAttributeValueContaining("class", "news-single-rightbox");
                        if (e1.size() > 0) {
                            Element timeElement = e1.get(0);
                            date = timeElement.ownText();
                        }
                        Elements titleElements = ele.getElementsByTag("h1");
                        if (titleElements.size() > 0) {
                            Element titleEle = titleElements.get(0);
                            heading = titleEle.ownText();
                        }
                        Elements subTitleElements = ele.getElementsByTag("h3");
                        if (subTitleElements.size() > 0) {
                            Element subTitleElement = subTitleElements.get(0);
                            String subTitle = subTitleElement.ownText();
                        }
                        Elements textElements = ele.getElementsByTag("p");
                        StringBuilder text = new StringBuilder();
                        if (textElements.size() > 0) {
                            for (Element textElement : textElements) {
                                text.append("");
                                textElement.tagName("div");
                                text.append(textElement.toString());
                            }
                        }

                        body = Util.cleanHtmlCodeInString(text.toString());
                    }
                }
                return Boolean.TRUE;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success.booleanValue()) {
                    loadmethod(date, heading, body);
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(EventDetailActivity.this);
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

    /**
     * load the downloaded description of a event and show it as html after setting the necessary html tags.
     **/
    private void loadmethod(String da, String head, String bod) {
        if (pBar != null && body != null) {
            pBar.setVisibility(View.GONE);
            String htmlStart = "<html><head></html><body><h5><center>"+da+"</center></h5><h3><center><font color=\"#034A78\">"+head+"</font></center></h3>";
            bod = "<body style=\"padding-left:10px; padding-right:10px\">" + bod + "</body>";
            body.loadDataWithBaseURL(null, htmlStart+bod, "text/html", "utf-8", null);
            body.getSettings().setJavaScriptEnabled(true);
            body.setVisibility(View.VISIBLE);
        }
    }

    /*
    * sets the custom navigation bar according to each activity.
    * */
    private void setActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.event_article);
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


    // custom class to show the back button action using navigation bar and will call the onBack method of activity
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