package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import de.unisaarland.UniApp.R;


public class PersonDetailWebActivity extends ActionBarActivity {
    private String url;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        url = extras.getString("url");
    }

    @Override
    public void onBackPressed() {
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.stopLoading();
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        ActionBar actionBar = getSupportActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.more_info);

        setContentView(R.layout.person_detail_layout);

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
                new AlertDialog.Builder(PersonDetailWebActivity.this)
                        .setMessage(description)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                })
                        .create().show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                ProgressBar pBar = (ProgressBar) findViewById(R.id.progress_bar);
                WebView webView = (WebView) findViewById(R.id.web_view);
                if (pBar != null && webView != null) {
                    pBar.setVisibility(View.GONE);
                }
            }
        });

        webView.loadUrl(url);
    }
}