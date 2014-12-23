package de.unisaarland.UniApp.bus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.Calendar;

import de.unisaarland.UniApp.R;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/13/13
 * Time: 9:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class BusDetailActivity extends ActionBarActivity {
    private String url;
    private ProgressBar pBar;
    private WebView webView;
    private String backText = null;
    private String title;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        if(savedInstanceState!=null){
            url = savedInstanceState.getString("url");
            title = savedInstanceState.getString("back");
        }
        // set the dae and time to get the latest results from the saarvv website.
        Calendar calendar = Calendar.getInstance();
        String date = calendar.get(Calendar.DAY_OF_MONTH)+"."+(calendar.get(Calendar.MONTH)+1)+"."+(calendar.get(Calendar.YEAR));
        String time = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
        url = url.replace("NEWDATE",date);
        url = url.replace("NEWTIME",time);
    }

    @Override
    public void onBackPressed() {
        url = null;
        pBar = null;
        if(webView!=null){
            webView.invalidate();
        }
        webView = null;
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActionBar();
        setContentView(R.layout.person_detail_layout);
        pBar = (ProgressBar) findViewById(R.id.web_view_progress_bar);
        webView = (WebView) findViewById(R.id.web_view);
        showDetail();
    }

    /*
    * Show the detail of bus stops
    * */
    private void showDetail() {
        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });
        ImageButton forwardButton = (ImageButton) findViewById(R.id.forward_button);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });
        webView.setBackgroundColor(Color.WHITE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.loadUrl(url);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(BusDetailActivity.this);
                builder1.setMessage(description);
                builder1.setCancelable(true);
                builder1.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(pBar!=null && webView!=null) {
                    pBar.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }
            }
        });

        webView.reload();
        webView.refreshDrawableState();
    }

    /*
    * sets the custom navigation bar according to each activity.
    * */
    private void setActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.busText);

    }

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