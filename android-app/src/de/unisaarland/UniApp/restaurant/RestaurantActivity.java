package de.unisaarland.UniApp.restaurant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.CachedMensaPlan;
import de.unisaarland.UniApp.restaurant.model.MensaDayMenu;
import de.unisaarland.UniApp.restaurant.uihelper.CircleFlowIndicator;
import de.unisaarland.UniApp.restaurant.uihelper.MensaDaysAdapter;
import de.unisaarland.UniApp.restaurant.uihelper.ViewFlow;
import de.unisaarland.UniApp.settings.SettingsActivity;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;
import de.unisaarland.UniApp.utils.Util;
import de.unisaarland.UniApp.utils.ui.RemoteOrLocalViewAdapter;

public class RestaurantActivity extends UpNavigationActionBarActivity {

    private CachedMensaPlan mensaPlan = null;

    private long lastSelectedDate = 0;
    private int positionToSelect = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.restaurant_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // The widget passes the item to select, and it's always for the current date.
        positionToSelect = getIntent().getIntExtra("position", -1);
        if (positionToSelect != -1)
            lastSelectedDate = 0;

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

    private final class NetworkDelegate
            implements NetworkRetrieveAndCache.Delegate<MensaDayMenu[]> {
        private boolean hasItems = false;

        @Override
        public void onUpdate(MensaDayMenu[] result, boolean fromCache) {
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
        public String checkValidity(MensaDayMenu[] result) {
            // validity was already checked in CachedMensaPlan
            return null;
        }
    }

    private void populateItems(final MensaDayMenu[] items) {
        // compute which item to preselect (smallest item >= current day, or last selected item)
        long dateToSelect = lastSelectedDate != 0 ? lastSelectedDate
                : Util.getStartOfDay().getTimeInMillis();
        int itemToSelect = 0;
        for (MensaDayMenu day : items)
            if (day.getDayStartMillis() < dateToSelect)
                ++itemToSelect;

        ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        RemoteOrLocalViewAdapter.LocalAdapter adapter =
                (RemoteOrLocalViewAdapter.LocalAdapter) viewFlow.getAdapter();
        MensaDaysAdapter newAdapter = new MensaDaysAdapter(items, false);
        if (adapter == null) {
            viewFlow.setAdapter(newAdapter.asLocalAdapter(this),
                    itemToSelect);
        } else {
            adapter.update(newAdapter);
            if (viewFlow.getSelectedItemPosition() != itemToSelect)
                viewFlow.setSelection(itemToSelect);
        }
        viewFlow.setOnViewSwitchListener(new ViewFlow.ViewSwitchListener() {
            @Override
            public void onSwitched(View view, int position) {
                lastSelectedDate = items[position].getDayStartMillis();
            }
        });
        CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
        viewFlow.setFlowIndicator(indic);
        if (positionToSelect != -1) {
            ListView mensaList = (ListView) viewFlow.getSelectedView().findViewById(R.id.mensaList);
            int count = mensaList.getCount();
            if (positionToSelect < count)
                mensaList.setSelection(positionToSelect);
        }
    }
}