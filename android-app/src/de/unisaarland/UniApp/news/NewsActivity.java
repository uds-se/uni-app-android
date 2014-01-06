package de.unisaarland.UniApp.news;

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
import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.networkcommunicator.INetworkLoaderDelegate;
import de.unisaarland.UniApp.networkcommunicator.NetworkHandler;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.news.model.INewsResultDelegate;
import de.unisaarland.UniApp.news.model.NewsModel;
import de.unisaarland.UniApp.news.model.NewsXMLParser;
import de.unisaarland.UniApp.news.uihelper.NewsAdapter;
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

    /*
    * Called when back button is pressed either from device or navigation bar.
    * */
    @Override
    public void onBackPressed() {
        // will invalidate the connection establishment request if it is not being completed yet and free the resources
        if(networkHandler!=null){
            networkHandler.invalidateRequest();
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        // release the resources.
        if(newsModelsArray != null){
            newsModelsArray.clear();
        }
        bar = null;
        networkHandler = null;
        newsModelsArray = null;
        super.onStop();
    }

    INetworkLoaderDelegate delegate = new INetworkLoaderDelegate() {

        /*
        * Will be called in case of failure e.g internet connection problem
        * Will try to load news from already stored model or in case if that model is not present will show the
        * error dialog
        * */
        @Override
        public void onFailure(String message) {
            if (newsFileExist()){
                loadNewsFromSavedFile();
                if(bar!=null){
                    bar.clearAnimation();
                    bar.setVisibility(View.INVISIBLE);
                }
                setContentView(R.layout.news_panel);
                populateNewsItems();
            } else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(NewsActivity.this);
                builder1.setMessage(message);
                builder1.setCancelable(true);
                builder1.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(bar!=null){
                                    bar.clearAnimation();
                                    bar.setVisibility(View.INVISIBLE);
                                }
                                dialog.cancel();
                                onBackPressed();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
        /*
        * Will be called in case of success if connection is successfully established and parser is ready
        * call the News parser to parse the resultant file and return the list of news models to specified call back method.
        * */
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

    /*
    * Load news from already saved model if internet is not available or coming back from news detail
    * */
    private void loadNewsFromSavedFile() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(getFilesDir().getAbsolutePath()+ Util.NEWS_FILE_NAME)));
            newsModelsArray = (ArrayList<NewsModel>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Check if news file already exist.
    * */
    private boolean newsFileExist() {
        File f = new File(getFilesDir().getAbsolutePath()+ Util.NEWS_FILE_NAME);
        if(f.exists()) {
            return true;
        }
        return false;
    }

    /*
    * Call back method of NewsResult will be called when all news are parsed and Model list is generated
    * */
    private INewsResultDelegate newsResultDelegate = new INewsResultDelegate() {
        @Override
        public void newsList(ArrayList<NewsModel> newsModels) {
            newsModelsArray = newsModels;
            removeLoadingView();
        }
    };

    /*
    * Will be called when activity created first time e.g. from scratch
    * */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
    * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
    * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
    * */
    @Override
    protected void onResume() {
        super.onResume();
        // sets the custom navigation bar according to each activity.
        setActionBar();
        /*
        * Checks if news are already loaded and models are built then no need to download the from internet again
        * e.g. if activity is just changed to see the details of any specific news and comes back to the news list
        * otherwise if it is being loaded from main activity page and internet is available it will be downloaded from internet
        * again.
        * */
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

    /*
    * Will remove the loading view as news are being downloaded and parsed also the models are built
    * Will save the current news model
    * and show the news list.
    * */
    private void removeLoadingView() {
        if(bar!=null){
            bar.clearAnimation();
            bar.setVisibility(View.INVISIBLE);
        }
        setContentView(R.layout.news_panel);
        boolean itemsSaved = saveCurrentNewItemsToFile();
        if(itemsSaved){
            Log.i("MyTag","News are saved");
        }
        populateNewsItems();
    }

    /*
    * Save current news model to file (temporary) so that these will be used later in case if user don't have internet connection
    * and also if user is coming back from seeing a detailed news.
    * */
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
        //displays the loading view and download and parse the news items from internet
        setContentView(R.layout.loading_layout);
        // safety check in case user press the back button then bar will be null
        if(bar!=null){
            bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.animate();
        }
        /**
         * Calls the custom class to connect and download the specific XML and pass the deligate method which will be called
         * in case of success and failure
         */

        networkHandler = new NetworkHandler(delegate);
        networkHandler.connect(URL, this);
    }

    /*
    * after downloading and parsing the news when models are built it will call the adapter and pass the
    * specified model to it so that it will display list of news items.
    * */
    private void populateNewsItems() {
        ListView newsList = (ListView) findViewById(R.id.newsItemListView);
        newsList.setAdapter(new NewsAdapter(this,newsModelsArray));
    }

    /**
     * sets the custom navigation bar according to each activity.
     */
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

        /**
         * add the face book page and on clicking of this button it will check if the facebook app is installed on the device then it will
         * open the specific page on that app otherwise it will open the page on browser.
         */

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
