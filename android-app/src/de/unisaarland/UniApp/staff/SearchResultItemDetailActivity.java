package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;


public class SearchResultItemDetailActivity extends ActionBarActivity {
    private String url = null;

    private NetworkRetrieveAndCache<StaffInfo> networkFetcher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        //Enabling UP-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.info);

        Bundle extras = getIntent().getExtras();
        url = extras.getString("url");

        setContentView(R.layout.search_result_detail_layout);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.setVisibility(View.GONE);
        ScrollView infoView = (ScrollView) findViewById(R.id.staff_info_scroll_view);
        infoView.setVisibility(View.INVISIBLE);

        if (networkFetcher == null) {
            String tag = "search-" + Integer.toHexString(url.hashCode());
            ContentCache cache = Util.getContentCache(this);
            networkFetcher = new NetworkRetrieveAndCache<>(url, tag, 15*60, cache,
                    new StaffInfoParser(url), new NetworkDelegate(), this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        networkFetcher.loadAsynchronously();
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
        public void onUpdate(StaffInfo result) {
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
    }

    private void showResult(StaffInfo info) {
        ScrollView infoView = (ScrollView) findViewById(R.id.staff_info_scroll_view);
        infoView.setVisibility(View.VISIBLE);
        Button nameButton = (Button) findViewById(R.id.name);
        nameButton.setText(info.getName());
        Button genderButton = (Button) findViewById(R.id.gender);
        genderButton.setText(info.getGender());
        Button academicDegreeButton = (Button) findViewById(R.id.academic_degree);
        academicDegreeButton.setText(info.getAcademicDegree());
        final Button buildingButton = (Button) findViewById(R.id.building);
        ImageButton buildingButtonForwardIcon = (ImageButton) findViewById(R.id.building_forward_icon);
        if(info.getBuilding() != null && !info.getBuilding().isEmpty()) {
            buildingButtonForwardIcon.setVisibility(View.VISIBLE);
            buildingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(SearchResultItemDetailActivity.this, CampusActivity.class);
                    myIntent.putExtra("building", buildingButton.getText());
                    SearchResultItemDetailActivity.this.startActivity(myIntent);

                }
            });
        }
        buildingButton.setText(info.getBuilding());
        Button roomButton = (Button) findViewById(R.id.room);
        roomButton.setText(info.getRoom());
        Button phoneButton = (Button) findViewById(R.id.phone);
        phoneButton.setText(info.getPhone());
        Button faxButton = (Button) findViewById(R.id.fax);
        faxButton.setText(info.getFax());
        Button emailButton = (Button) findViewById(R.id.email);
        emailButton.setText(info.getEmail());
        Button moreButton = (Button) findViewById(R.id.more);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SearchResultItemDetailActivity.this, PersonDetailWebActivity.class);
                myIntent.putExtra("url", url);
                SearchResultItemDetailActivity.this.startActivity(myIntent);

            }
        });
    }
}