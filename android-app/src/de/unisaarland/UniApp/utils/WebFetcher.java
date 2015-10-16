package de.unisaarland.UniApp.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import de.unisaarland.UniApp.R;


public class WebFetcher {
    private static final String TAG = WebFetcher.class.getSimpleName();

    private final INetworkLoaderDelegate delegate;
    private AsyncTask<Void, Void, InputStream> lastBackgroundTask = null;
    private HttpURLConnection lastConnection = null;
    private static final int CONNECTION_TIME = 30000;

    private volatile boolean canceled = false;


    public WebFetcher(INetworkLoaderDelegate delegate) {
        assert (delegate != null);
        this.delegate = delegate;
    }

    public void startFetchingAsynchronously(String urlStr, final Context context) {
        final URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            // URLs are always provided by the app itself, they should never be malformed...
            AssertionError ae = new AssertionError(e.toString());
            ae.setStackTrace(e.getStackTrace());
            throw ae;
        }
        if (!Util.isConnectedToInternet(context)) {
            delegate.onFailure(context.getString(R.string.not_connected));
            return;
        }

        canceled = false;
        AsyncTask<Void, Void, InputStream> t = new AsyncTask<Void, Void, InputStream>() {
            private String errorMessage = "";
            @Override
            protected InputStream doInBackground(Void... params) {
                HttpURLConnection connection = null;
                try {
                    connection = startFetching(url);
                    return connection.getInputStream();
                } catch (IOException e) {
                    if (!canceled) {
                        Log.w(TAG, "error loading document: " + url, e);
                        errorMessage = context.getString(R.string.networkError);
                    }
                    return null;
                }
            }

            @Override
            protected void onPostExecute(InputStream data) {
                if (data != null) {
                    delegate.onSuccess(data);
                } else {
                    delegate.onFailure(errorMessage);
                }
            }
        };
        lastBackgroundTask = t;
        t.execute();
    }

    private HttpURLConnection startFetching(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setReadTimeout(CONNECTION_TIME /* milliseconds */);
        con.setConnectTimeout(CONNECTION_TIME /* milliseconds */);
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            // should never happen on an HttpURLConnection
            throw new AssertionError(e);
        }
        con.setDoInput(true);
        lastConnection = con;
        // Starts the query
        con.connect();
        return con;
    }

    public void invalidateRequest() {
        canceled = true;
        if (lastBackgroundTask != null)
            lastBackgroundTask.cancel(true);
        if (lastConnection != null) {
            try {
                lastConnection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
