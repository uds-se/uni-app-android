package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;
import de.unisaarland.UniApp.utils.Util;


public class SearchResultItemDetailActivity extends UpNavigationActionBarActivity {
    private String url = null;

    private NetworkRetrieveAndCache<StaffInfo> networkFetcher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        url = extras.getString("url");

        setContentView(R.layout.search_result_detail_layout);
    }

    // onResume gets called after onCreate or onNewIntent
    @Override
    protected void onResume() {
        super.onResume();
        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.GONE);
        ScrollView infoView = (ScrollView) findViewById(R.id.staff_info_scroll_view);
        infoView.setVisibility(View.INVISIBLE);

        if (networkFetcher == null || networkFetcher.getUrl() != url) {
            String tag = "search-" + Integer.toHexString(url.hashCode());
            ContentCache cache = Util.getContentCache(this);
            networkFetcher = new NetworkRetrieveAndCache<>(url, tag, cache,
                    new StaffInfoParser(url), new NetworkDelegate(), this);
        }
        networkFetcher.loadAsynchronously(15 * 60);
    }

    @Override
    protected void onStop() {
        if (networkFetcher != null) {
            networkFetcher.cancel();
            networkFetcher = null;
        }
        super.onStop();
    }

    private class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<StaffInfo> {
        private boolean hasResult = false;

        @Override
        public void onUpdate(StaffInfo result, boolean fromCache) {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.setVisibility(View.GONE);
            hasResult = true;
            showResult(result);
        }

        @Override
        public void onStartLoading() {
            ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
            bar.animate();
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onFailure(String message) {
            new AlertDialog.Builder(SearchResultItemDetailActivity.this)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                    bar.setVisibility(View.GONE);
                                    dialog.dismiss();
                                    if (!hasResult)
                                        onBackPressed();
                                }
                            })
                    .create().show();
        }

        @Override
        public String checkValidity(StaffInfo result) {
            return null;
        }
    }

    private void showResult(final StaffInfo info) {
        ScrollView infoView = (ScrollView) findViewById(R.id.staff_info_scroll_view);
        infoView.setVisibility(View.VISIBLE);
        TextView nameButton = (TextView) findViewById(R.id.name);
        nameButton.setText(info.getName());
        TextView genderButton = (TextView) findViewById(R.id.gender);
        genderButton.setText(info.getGender());
        TextView academicDegreeButton = (TextView) findViewById(R.id.academic_degree);
        academicDegreeButton.setText(info.getAcademicDegree());
        final TextView buildingButton = (TextView) findViewById(R.id.building);
        ImageButton buildingButtonForwardIcon = (ImageButton) findViewById(R.id.building_forward_icon);
        if (info.getBuilding() != null && !info.getBuilding().isEmpty()) {
            buildingButtonForwardIcon.setVisibility(View.VISIBLE);
            View.OnClickListener showBuildingClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(SearchResultItemDetailActivity.this, CampusActivity.class);
                    myIntent.putExtra("building", info.getBuilding());
                    SearchResultItemDetailActivity.this.startActivity(myIntent);
                }
            };
            buildingButton.setOnClickListener(showBuildingClick);
            buildingButtonForwardIcon.setOnClickListener(showBuildingClick);
        } else {
            buildingButtonForwardIcon.setVisibility(View.GONE);
        }
        buildingButton.setText(info.getBuilding());
        TextView roomButton = (TextView) findViewById(R.id.room);
        roomButton.setText(info.getRoom());
        TextView phoneButton = (TextView) findViewById(R.id.phone);
        phoneButton.setText(info.getPhone());
        TextView faxButton = (TextView) findViewById(R.id.fax);
        faxButton.setText(info.getFax());
        TextView emailButton = (TextView) findViewById(R.id.email);
        emailButton.setText(info.getEmail());
        View.OnClickListener showMoreClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SearchResultItemDetailActivity.this, PersonDetailWebActivity.class);
                myIntent.putExtra("url", url);
                SearchResultItemDetailActivity.this.startActivity(myIntent);
            }
        };
        findViewById(R.id.more).setOnClickListener(showMoreClick);
        findViewById(R.id.more_forward_icon).setOnClickListener(showMoreClick);
    }
}