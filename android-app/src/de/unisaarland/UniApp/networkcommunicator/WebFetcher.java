package de.unisaarland.UniApp.networkcommunicator;

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
    private volatile HttpURLConnection lastConnection;
    private final int CONNECTION_TIME = 20000;


    public WebFetcher(INetworkLoaderDelegate delegate) {
        assert (delegate != null);
        this.delegate = delegate;
    }

    public void startFetchingAsynchronously(String urlStr, Context context) {
        final URL url;
        try {
            url = new URL(Util.urlEncode(urlStr));
        } catch (MalformedURLException e) {
            // URLs are always provided by the app itself, they should never be malformed...
            throw new AssertionError(e);
        }
        if (!Util.isConnectedToInternet(context)) {
            delegate.onFailure(context.getString(R.string.not_connected));
            return;
        }

        new AsyncTask<Void, Void, InputStream>() {
            private String errorMessage = "";
            @Override
            protected InputStream doInBackground(Void... params) {
                HttpURLConnection connection = null;
                try {
                    connection = startFetching(url);
                } catch (IOException e) {
                    Log.w(TAG, "error connecting to '"+url+"'", e);
                    errorMessage = "Error loading document: " + e.getLocalizedMessage();
                    return null;
                }

                try {
                    return connection.getInputStream();
                } catch (IOException e) {
                    errorMessage = "Error getting input stream from connection";
                    Log.w(TAG, errorMessage, e);
                    return null;
                }
            }

            @Override
            protected void onCancelled() {
                invalidateRequest();
                super.onCancelled();
            }

            @Override
            protected void onPostExecute(InputStream data) {
                if (data != null) {
                    delegate.onSuccess(data);
                } else {
                    if (errorMessage == null)
                        errorMessage = "Unknown error";
                    delegate.onFailure(errorMessage);
                }
            }
        }.execute();
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
        HttpURLConnection con = lastConnection;
        try {
            if (con != null)
                con.disconnect();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
