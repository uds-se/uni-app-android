package de.unisaarland.UniApp.news;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.news.model.NewsModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/11/13
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewsArticleActivity extends Activity {
    private NewsModel model;
    private WebView body = null;
    private ProgressBar pBar = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        model = (NewsModel) savedInstanceState.getSerializable("model");
        setActionBar();
        setContentView(R.layout.news_detail);
        showNews();
    }

    @Override
    public void onBackPressed() {
        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        boolean isCopied = settings.getBoolean(Util.NEWS_LOADED,false);
        if(!isCopied){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Util.NEWS_LOADED, true);
            editor.commit();
        }
        model = null;
        pBar = null;
        body = null;
        super.onBackPressed();
    }

    private void showNews() {
        body = (WebView) findViewById(R.id.body);
        pBar = (ProgressBar) findViewById(R.id.web_view_progress_bar);
        pBar.setVisibility(View.VISIBLE);
        body.setVisibility(View.GONE);

        getTask(model).execute();
    }

    private AsyncTask<Void,Void,Integer> getTask(final NewsModel model){
        return new AsyncTask<Void, Void, Integer>() {
            String date;
            String heading;
            String body;
            @Override
            protected Integer doInBackground(Void... params) {
                Document doc = null;
                try {
                    String str = model.getLink();
                    doc = Jsoup.connect(str).get();
                } catch (IOException e) {
                    Log.e("MyTag", e.getMessage());
                }
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
                                text.append(textElement.toString());
                            }
                        }

                        body = Util.cleanHtmlCodeInString(text.toString());
                    }
                }
                return 1;
            }

            @Override
            protected void onPostExecute(Integer i) {
                loadmethod(date,heading,body);
            }
        };
    }

    private void loadmethod(String da, String head, String bod) {
        pBar.setVisibility(View.GONE);
        String htmlStart = "<html><head></html><body><h2><center>"+da+"</center></h2><h3><center><font color=\"#5578ff\">"+head+"</font></center></h3>";
        body.loadDataWithBaseURL(null, htmlStart+bod, "text/html", "utf-8", null);
        body.getSettings().setJavaScriptEnabled(true);
        body.setVisibility(View.VISIBLE);
    }


    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.article_text);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.newsText);
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