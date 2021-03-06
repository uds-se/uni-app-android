package de.unisaarland.UniApp.staff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;
import de.unisaarland.UniApp.utils.Util;


public class PersonDetailWebActivity extends UpNavigationActionBarActivity {
    private String url;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        url = intent.getStringExtra("url");
        setup();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = (String) Util.getExtra("url", savedInstanceState, getIntent().getExtras(), url);

        setup();
    }

    private void setup() {
        if (url == null)
            throw new AssertionError("url should be passed via intent or saved state");

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
                pBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl(url);
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
}