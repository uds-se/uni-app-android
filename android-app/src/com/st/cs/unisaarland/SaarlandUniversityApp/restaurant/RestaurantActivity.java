package com.st.cs.unisaarland.SaarlandUniversityApp.restaurant;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.INetworkLoaderDelegate;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.NetworkHandler;
import com.st.cs.unisaarland.SaarlandUniversityApp.restaurant.model.*;
import com.st.cs.unisaarland.SaarlandUniversityApp.restaurant.uihelper.CircleFlowIndicator;
import com.st.cs.unisaarland.SaarlandUniversityApp.restaurant.uihelper.ViewFlow;
import com.st.cs.unisaarland.SaarlandUniversityApp.restaurant.uihelper.ViewFlowAdapter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/6/13
 * Time: 11:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class RestaurantActivity extends Activity {
    private final String RESTAURANT_FILE_NAME = "restaurant.dat";
    private ProgressBar bar;
    private NetworkHandler mensaNetworkHandler = null;
    private String backText = null;

    private final String MENSA_URL = "http://studentenwerk.netzindianer.net/_menu/actual/speiseplan-saarbruecken.xml";
    private final String AUS_CAFE_URL = "http://www.uni-saarland.de/campus/service-und-kultur/gastronomieaufdemcampus/auslaender-cafe.html";

    private HashMap<String,ArrayList<MensaItem>> mensaItemsDictionary = null;
    private ArrayList<String> keysList = null;

    INetworkLoaderDelegate mensaDelegate = new INetworkLoaderDelegate() {
        @Override
        public void onFailure(String message) {
            if (restaurantFileExist()){
                loadMensaItemsFromSavedFile();
                bar.clearAnimation();
                bar.setVisibility(View.INVISIBLE);
                mensaNetworkHandler.invalidateRequest();
                setContentView(R.layout.restaurant_layout);
                populateMensaItems();
            } else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(RestaurantActivity.this);
                builder1.setMessage(message);
                builder1.setCancelable(true);
                builder1.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                bar.clearAnimation();
                                bar.setVisibility(View.INVISIBLE);
                                mensaNetworkHandler.invalidateRequest();
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
            MensaXMLParser mensaParser = new MensaXMLParser(mensaResultDelegate);
            try {
                mensaParser.parse(parser);
            } catch (XmlPullParserException e) {
                Log.e("MyTag,", e.getMessage());
            } catch (IOException e) {
                Log.e("MyTag,", e.getMessage());
            }
        }
    };

    private IMensaResultDelegate mensaResultDelegate = new IMensaResultDelegate() {
        @Override
        public void mensaItemsList(HashMap<String,ArrayList<MensaItem>> daysDictionary) {
            new AusLanderCafeParser(auslanderResultDelegate,AUS_CAFE_URL,daysDictionary).parse();
        }
    };

    private IMensaResultDelegate auslanderResultDelegate = new IMensaResultDelegate() {
        @Override
        public void mensaItemsList(HashMap<String, ArrayList<MensaItem>> daysDictionary) {

            mensaItemsDictionary = daysDictionary;
            Set set = mensaItemsDictionary.keySet();
            keysList = new ArrayList<String>(mensaItemsDictionary.size());
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()){
                keysList.add(iter.next());
            }

            Collections.sort(keysList);
            mensaNetworkHandler.invalidateRequest();
            removeLoadingView();
        }
    };

    private void removeLoadingView() {
        if(bar!=null){
            bar.clearAnimation();
            bar.setVisibility(View.INVISIBLE);
            setContentView(R.layout.restaurant_layout);
            boolean itemsSaved = savMensaItemsToFile();
            if (itemsSaved) {
                Log.i("MyTag", "News are saved");
            }
            populateMensaItems();
        }

    }

    private void populateMensaItems() {

        ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        viewFlow.setAdapter(new ViewFlowAdapter(this,mensaItemsDictionary,keysList), 0);
        CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
        viewFlow.setFlowIndicator(indic);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        if(savedInstanceState!=null){
            backText = savedInstanceState.getString("back");
        }
        setActionBar();
    }

    @Override
    protected void onStart() {
        addLoadingView();
        super.onStart();
    }

    private void addLoadingView() {
        setContentView(R.layout.loading_layout);
        bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.animate();
        mensaNetworkHandler = new NetworkHandler(mensaDelegate);
        mensaNetworkHandler.connect(MENSA_URL, this);
    }

    private boolean savMensaItemsToFile(){
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir().getAbsolutePath()+ RESTAURANT_FILE_NAME)));
            oos.writeObject(mensaItemsDictionary);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean restaurantFileExist() {
        File f = new File(getFilesDir().getAbsolutePath()+ RESTAURANT_FILE_NAME);
        if(f.exists()) {
            return true;
        }
        return false;
    }
    private void removeOldDataFromFile() {
            boolean changed = false;
            Set set = mensaItemsDictionary.keySet();
            ArrayList<String> temp = new ArrayList<String>(mensaItemsDictionary.size());
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()){
                String tempDate = iter.next();
                long date = Long.parseLong(tempDate)*1000;
                Date now = new Date();
                Date tagDate = new Date(date);
                now.setHours(0);
                now.setMinutes(0);
                now.setSeconds(0);
                if(now.before(tagDate) ||
                        (now.getDate() == tagDate.getDate() && now.getMonth() == tagDate.getMonth())) {

                }else{
                    temp.add(tempDate);
                    changed = true;
                }
            }

            if(changed){
                for (int i=0;i<temp.size();i++){
                    mensaItemsDictionary.remove(temp.get(i));
                }
                savMensaItemsToFile();
            }
    }

    private void loadMensaItemsFromSavedFile() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(getFilesDir().getAbsolutePath()+ RESTAURANT_FILE_NAME)));
            mensaItemsDictionary = (HashMap<String, ArrayList<MensaItem>>) ois.readObject();
            ois.close();
            removeOldDataFromFile();
            Set set = mensaItemsDictionary.keySet();
            keysList = new ArrayList<String>(mensaItemsDictionary.size());
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()){
                keysList.add(iter.next());
            }

            Collections.sort(keysList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.mensa_text);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        if(backText == null){
            backPageText.setText(R.string.homeText);
        }else{
            backPageText.setText(backText);
        }
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        TextView rightText = (TextView) actionBar.getCustomView().findViewById(R.id.page_right_heading);
        rightText.setText(R.string.opening_hours);
        rightText.setVisibility(View.VISIBLE);
        rightText.setClickable(true);
        rightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(RestaurantActivity.this, OpeningHoursActivity.class);
                RestaurantActivity.this.startActivity(myIntent);
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

    @Override
    public void onBackPressed() {
        backText = null;
        bar = null;
        super.onBackPressed();
    }
}