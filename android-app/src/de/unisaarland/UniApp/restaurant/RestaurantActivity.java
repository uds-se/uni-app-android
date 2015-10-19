package de.unisaarland.UniApp.restaurant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaItem;
import de.unisaarland.UniApp.restaurant.model.MensaXMLParser;
import de.unisaarland.UniApp.restaurant.uihelper.CircleFlowIndicator;
import de.unisaarland.UniApp.restaurant.uihelper.ViewFlow;
import de.unisaarland.UniApp.restaurant.uihelper.ViewFlowAdapter;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;

public class RestaurantActivity extends ActionBarActivity {

    private final String TAG = RestaurantActivity.class.getSimpleName();

    private NetworkRetrieveAndCache<Map<Long, List<MensaItem>>> mensaFetcher = null;

    private final String MENSA_URL_SB = "http://studentenwerk-saarland.de/_menu/actual/speiseplan-saarbruecken.xml";
    private final String MENSA_URL_HOM = "http://studentenwerk-saarland.de/_menu/actual/speiseplan-homburg.xml";

    @Override
    protected void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();
        //Enable Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.mensa_text);

        setContentView(R.layout.restaurant_layout);
        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.GONE);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String campus = settings.getString(Util.KEY_CAMPUS_CHOOSER, "saar");
        String mensaUrl = campus.equals("saar") ? MENSA_URL_SB : MENSA_URL_HOM;

        if (mensaFetcher == null || !mensaUrl.equals(mensaFetcher.getUrl())) {
            ContentCache cache = Util.getContentCache(this);
            mensaFetcher = new NetworkRetrieveAndCache<>(mensaUrl, "mensa-"+campus, 15*60, cache,
                    new MensaXMLParser(), new NetworkDelegate(), this);
        }
        mensaFetcher.loadAsynchronously();
    }

    @Override
    protected void onStop() {
        if (mensaFetcher != null) {
            mensaFetcher.cancel();
            mensaFetcher = null;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.restaurant_activity_icons, menu);
        return super.onCreateOptionsMenu(menu);
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
            case R.id.action_opening_hours:
                //Open opening hours actions when button is pressed
                Intent myIntent = new Intent(RestaurantActivity.this, OpeningHoursActivity.class);
                RestaurantActivity.this.startActivity(myIntent);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<Map<Long, List<MensaItem>>> {
        private boolean hasItems = false;

        @Override
        public void onUpdate(Map<Long, List<MensaItem>> result) {
            if (result.isEmpty()) {
                onFailure(getString(R.string.emptyDocumentError));
                return;
            }
            hasItems = true;

            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.clearAnimation();
            bar.setVisibility(View.INVISIBLE);
            populateItems(result);
        }

        @Override
        public void onStartLoading() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.setVisibility(View.VISIBLE);
            bar.animate();
        }

        @Override
        public void onFailure(String message) {
            new AlertDialog.Builder(RestaurantActivity.this)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                    bar.clearAnimation();
                                    bar.setVisibility(View.INVISIBLE);
                                    dialog.cancel();
                                    if (!hasItems)
                                        onBackPressed();
                                }
                            })
                    .create().show();
        }
    }

    private void populateItems(Map<Long, List<MensaItem>> items) {
        ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        viewFlow.setAdapter(new ViewFlowAdapter(this, items), 0);
        CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
        viewFlow.setFlowIndicator(indic);
    }

}