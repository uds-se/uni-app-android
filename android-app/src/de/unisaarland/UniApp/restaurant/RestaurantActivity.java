package de.unisaarland.UniApp.restaurant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.CachedMensaPlan;
import de.unisaarland.UniApp.restaurant.model.MensaItem;
import de.unisaarland.UniApp.restaurant.uihelper.CircleFlowIndicator;
import de.unisaarland.UniApp.restaurant.uihelper.MensaItemsAdapter;
import de.unisaarland.UniApp.restaurant.uihelper.ViewFlow;
import de.unisaarland.UniApp.settings.SettingsActivity;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;
import de.unisaarland.UniApp.utils.Util;

public class RestaurantActivity extends UpNavigationActionBarActivity {

    private CachedMensaPlan mensaPlan = null;

    private long lastSelectedDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.restaurant_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.GONE);

        if (mensaPlan == null)
            mensaPlan = new CachedMensaPlan(new NetworkDelegate(), this);

        mensaPlan.load(15 * 60);
    }

    @Override
    protected void onStop() {
        if (mensaPlan != null) {
            mensaPlan.cancel();
            mensaPlan = null;
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
            case R.id.show_settings:
                startActivity(new Intent(RestaurantActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_opening_hours:
                //Open opening hours actions when button is pressed
                startActivity(new Intent(RestaurantActivity.this, OpeningHoursActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<Map<Long, List<MensaItem>>> {
        private boolean hasItems = false;

        @Override
        public void onUpdate(Map<Long, List<MensaItem>> result) {
            hasItems = true;

            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.setVisibility(View.GONE);
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
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.setVisibility(View.GONE);
            new AlertDialog.Builder(RestaurantActivity.this)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    if (!hasItems)
                                        onBackPressed();
                                }
                            })
                    .create().show();
        }

        @Override
        public String checkValidity(Map<Long, List<MensaItem>> result) {
            // validitiy was already checked in CachedMensaPlan
            return null;
        }
    }

    private void populateItems(Map<Long, List<MensaItem>> items) {
        // compute which item to preselect (smallest item >= current day, or last selected item)
        long dateToSelect = lastSelectedDate != 0 ? lastSelectedDate : Util.getStartOfDay().getTime();
        int itemToSelect = 0;
        for (Long l : items.keySet())
            if (l < dateToSelect)
                ++itemToSelect;

        ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        MensaItemsAdapter adapter = (MensaItemsAdapter) viewFlow.getAdapter();
        if (adapter == null) {
            viewFlow.setAdapter(new MensaItemsAdapter(this, items), itemToSelect);
            viewFlow.setOnViewSwitchListener(new ViewFlow.ViewSwitchListener() {
                @Override
                public void onSwitched(View view, int position) {
                    Long date = (Long) view.getTag(R.id.mensa_menu_date_tag);
                    lastSelectedDate = date.longValue();
                }
            });
        } else {
            adapter.update(items);
            viewFlow.setSelection(itemToSelect);
        }
        CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
        viewFlow.setFlowIndicator(indic);
    }
}