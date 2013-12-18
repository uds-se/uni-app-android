package com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 11/29/13
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class WebFetcher{
    private final INetworkLoaderDelegate delegate;
    private AsyncTask<Void, Void, Integer> newTask;
    private URL url;
    private HttpURLConnection con;
    private XmlPullParser parser = null;
    private final int CONNECTION_TIME = 10000;


    public WebFetcher(INetworkLoaderDelegate delegate) {
        this.delegate = delegate;
    }

    public void startFetchingAsynchronously(String urlStr,Context context) {
        try {
            urlStr = Util.urlEncode(urlStr);
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            url = null;
        }
        if (!Util.isConnectedToInternet(context)) {
            delegate.onFailure(context.getString(R.string.not_connected));
            return;
        }

        this.newTask = getTask();
        this.newTask.execute();
    }

    private AsyncTask<Void, Void, Integer> getTask() {
        return new AsyncTask<Void, Void, Integer>() {
            String errorMessage = "";
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    startFetching();
                }
                catch (Exception e){
                    errorMessage = e.getMessage();
                    return -1;
                }
                return 1;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                invalidateRequest();
            }

            @Override
            protected void onPostExecute(Integer i) {
                if(i == -1){
                    delegate.onFailure(errorMessage);
                }else{
                    delegate.onSuccess(parser);
                }
            }
        };
    }

    private void startFetching() throws Exception {
        try {
            openConnection();
            if (con != null) {
                InputStream is = con.getInputStream();
                parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                parser.nextTag();
            }
        }catch(Exception e){
            throw e;
        }
    }

    private void openConnection() throws Exception {
        if (url != null) {
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(CONNECTION_TIME /* milliseconds */);
            con.setConnectTimeout(CONNECTION_TIME /* milliseconds */);
            con.setRequestMethod("GET");
            con.setDoInput(true);
            // Starts the query
            con.connect();
        }
    }

    public void invalidateRequest() {
        try {
            disconnectConnection();
        } catch (Exception e) {
            Log.e("MyTag", e.getMessage());
        }
    }

    private void disconnectConnection() {
        if (con != null) {
            con.disconnect();
        }
    }
}
