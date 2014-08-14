package de.unisaarland.UniApp.events;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import de.unisaarland.UniApp.events.model.EventsModel;
import de.unisaarland.UniApp.networkcommunicator.Util;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/12/13
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventDetailActivity extends Activity {
    private EventsModel model;
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
        model = (EventsModel) savedInstanceState.getSerializable("model");
        // sets the custom navigation bar according to each activity.
        setActionBar();
        setContentView(R.layout.news_detail);
        showEvent();
    }

    /*
    * Called when back button is pressed either from device or navigation bar.
    * sets the preference variable that events are already loaded so in case of going back to
    * event activity events wouldn't be loaded back from internet again
    * */
    @Override
    public void onBackPressed() {
        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        boolean isCopied = settings.getBoolean(Util.EVENTS_LOADED,false);
        if(!isCopied){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Util.EVENTS_LOADED, true);
            editor.commit();
        }
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

    private AsyncTask<Void,Void,Integer> getTask(final EventsModel model){
        return new AsyncTask<Void, Void, Integer>() {
            String date;
            String heading;
            String body;

            // It will only show the event description all other attributes of html will be omitted.
            @Override
            protected Integer doInBackground(Void... params) {
                Document doc = null;
                try {
                    String str = model.getLink();
                    doc = Jsoup.connect(str).timeout(15*1000).get();

                Elements elements = doc.getElementsByTag("div");
                for(Element ele: elements){
                    if(ele.className().equals("news-single-item")){
                        Elements e1 = ele.getElementsByAttributeValueContaining("class", "news-single-rightbox");
                        if(e1.size()>0){
                            Element timeElement = e1.get(0);
                            date = timeElement.ownText();
                        }
                        Elements titleElements = ele.getElementsByTag("h1");
                        if(titleElements.size()>0){
                            Element titleEle = titleElements.get(0);
                            heading = titleEle.ownText();
                        }
                        Elements subTitleElements = ele.getElementsByTag("h3");
                        if(subTitleElements.size()>0){
                            Element subTitleElement = subTitleElements.get(0);
                            String subTitle = subTitleElement.ownText();
                        }
                        Elements textElements = ele.getElementsByTag("p");
                        StringBuilder text = new StringBuilder();
                        if(textElements.size()>0){
                            for(Element textElement: textElements){
                                text.append("");
                                textElement.tagName("div");
                                text.append(textElement.toString());
                            }
                        }

                        body = Util.cleanHtmlCodeInString(text.toString());
                    }
                }
                return 1;
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer i) {
                if (i == 1)
                loadmethod(date,heading,body);
                else{
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
        if(pBar != null && body != null){
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
        ActionBar actionBar = getActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.event_article);

        /*
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.event_article);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

       TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.eventsText);
        backPageText.setTextSize(12);
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        ImageButton bigButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.big);
        bigButton.setVisibility(View.VISIBLE);
        bigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int zoom = body.getSettings().getTextZoom();
                if(zoom < 130){
                    body.getSettings().setTextZoom(zoom+5);
                }
            }
        });
        ImageButton smallButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.small);
        smallButton.setVisibility(View.VISIBLE);
        smallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int zoom = body.getSettings().getTextZoom();
                if(zoom > 70){
                    body.getSettings().setTextZoom(zoom-5);
                }
            }
        });

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