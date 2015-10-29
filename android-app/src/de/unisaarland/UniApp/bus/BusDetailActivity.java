package de.unisaarland.UniApp.bus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.Calendar;

import de.unisaarland.UniApp.R;


public class BusDetailActivity extends ActionBarActivity {
    private String url;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        url = savedInstanceState.getString("url", url);
        if (extras != null)
            url = extras.getString("url", url);

        if (url == null)
            throw new NullPointerException("url must be set from intent or saved state");

        setContentView(R.layout.person_detail_layout);

        ActionBar actionBar = getSupportActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.busText);

        // set the date and time to get the latest results from the saarvv website.
        Calendar calendar = Calendar.getInstance();
        String date = calendar.get(Calendar.DAY_OF_MONTH)+"."+(calendar.get(Calendar.MONTH)+1)+"."+(calendar.get(Calendar.YEAR));
        String time = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
        url = url.replace("NEWDATE", date).replace("NEWTIME", time);
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.stopLoading();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final ProgressBar pBar = (ProgressBar) findViewById(R.id.progress_bar);
        final WebView webView = (WebView) findViewById(R.id.web_view);

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
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                new AlertDialog.Builder(BusDetailActivity.this)
                        .setMessage(description)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .create().show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pBar.setVisibility(View.GONE);
            }
        });

        pBar.animate();
        pBar.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }
}