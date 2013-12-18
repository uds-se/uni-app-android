package com.st.cs.unisaarland.SaarlandUniversityApp.news;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.INetworkLoaderDelegate;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.NetworkHandler;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.Util;
import com.st.cs.unisaarland.SaarlandUniversityApp.news.model.INewsResultDelegate;
import com.st.cs.unisaarland.SaarlandUniversityApp.news.model.NewsModel;
import com.st.cs.unisaarland.SaarlandUniversityApp.news.model.NewsXMLParser;
import com.st.cs.unisaarland.SaarlandUniversityApp.news.uihelper.NewsAdapter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 11/28/13
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewsActivity extends Activity {
    private ProgressBar bar;
    private ArrayList<NewsModel> newsModelsArray;
    private final String URL = "http://www.uni-saarland.de/aktuelles/presse/pms.html?type=100&tx_ttnews[cat]=26";
    private NetworkHandler networkHandler = null;

    @Override
    public void onBackPressed() {
        if(networkHandler!=null){
            networkHandler.invalidateRequest();
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        if(newsModelsArray != null){
            newsModelsArray.clear();
        }
        bar = null;
        networkHandler = null;
        newsModelsArray = null;
        super.onStop();
    }

    INetworkLoaderDelegate delegate = new INetworkLoaderDelegate() {
        @Override
        public void onFailure(String message) {
            if (newsFileExist()){
                loadNewsFromSavedFile();
                bar.clearAnimation();
                bar.setVisibility(View.INVISIBLE);
                setContentView(R.layout.news_panel);
                populateNewsItems();
            } else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(NewsActivity.this);
                builder1.setMessage(message);
                builder1.setCancelable(true);
                builder1.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                bar.clearAnimation();
                                bar.setVisibility(View.INVISIBLE);
                                dialog.cancel();
                                onBackPressed();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }

        @Override
        public void onSuccess(XmlPullParser parser) {
            NewsXMLParser newsParser = new NewsXMLParser(newsResultDelegate);
            try {
               newsParser.parse(parser);
            } catch (XmlPullParserException e) {
                Log.e("MyTag,", e.getMessage());
            } catch (IOException e) {
                Log.e("MyTag,", e.getMessage());
            }
        }
    };

    private void loadNewsFromSavedFile() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(getFilesDir().getAbsolutePath()+ Util.NEWS_FILE_NAME)));
            newsModelsArray = (ArrayList<NewsModel>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean newsFileExist() {
        File f = new File(getFilesDir().getAbsolutePath()+ Util.NEWS_FILE_NAME);
        if(f.exists()) {
            return true;
        }
        return false;
    }

    private INewsResultDelegate newsResultDelegate = new INewsResultDelegate() {
        @Override
        public void newsList(ArrayList<NewsModel> newsModels) {
            newsModelsArray = newsModels;
            removeLoadingView();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActionBar();

        SharedPreferences settings = getSharedPreferences(Util.PREFS_NAME, 0);
        boolean isCopied = settings.getBoolean(Util.NEWS_LOADED,false);
        if(!isCopied){
            addLoadingView();
        }else{
            loadNewsFromSavedFile();
            setContentView(R.layout.news_panel);
            populateNewsItems();
        }
    }

    private void removeLoadingView() {
        bar.clearAnimation();
        bar.setVisibility(View.INVISIBLE);
        setContentView(R.layout.news_panel);
        boolean itemsSaved = saveCurrentNewItemsToFile();
        if(itemsSaved){
            Log.i("MyTag","News are saved");
        }
        populateNewsItems();
    }

    private boolean saveCurrentNewItemsToFile() {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir().getAbsolutePath()+ Util.NEWS_FILE_NAME)));
            oos.writeObject(newsModelsArray);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void addLoadingView() {
        setContentView(R.layout.loading_layout);
        bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.animate();
        networkHandler = new NetworkHandler(delegate);
        networkHandler.connect(URL, this);
    }

    private void populateNewsItems() {
        ListView newsList = (ListView) findViewById(R.id.newsItemListView);
        newsList.setAdapter(new NewsAdapter(this,newsModelsArray));
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.newsText);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.homeText);
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        ImageButton backButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.back_icon);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new BackButtonClickListener(this));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        ImageButton facebookButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.page_right_icon);
        facebookButton.setVisibility(View.VISIBLE);
        facebookButton.setBackgroundResource(R.drawable.facebook_icon);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(networkHandler!=null){
                    networkHandler.invalidateRequest();
                }
                Uri dataUri = Uri.parse("fb://profile/120807804649363");
                Intent receiverIntent = new Intent(Intent.ACTION_VIEW, dataUri);

                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(receiverIntent, 0);

                if (activities.size() > 0) {
                    startActivity(receiverIntent);
                } else {
                    Uri webpage = Uri.parse("http://www.facebook.com/120807804649363");
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);

                    packageManager = getPackageManager();
                    activities = packageManager.queryIntentActivities(webIntent, 0);

                    if (activities.size() > 0) {
                        startActivity(webIntent);
                    }
                }
            }
        });
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
