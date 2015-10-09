package de.unisaarland.UniApp.utils;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.transform.Result;

import de.unisaarland.UniApp.R;

public class WebXMLFetcher<ResultType> {

    private static final String TAG = WebXMLFetcher.class.getSimpleName();

    private volatile WebFetcher lastWebFetcher = null;
    private final XMLExtractor<ResultType> extractor;
    private final XMLFetcherDelegate<ResultType> delegate;

    public WebXMLFetcher(XMLExtractor<ResultType> extractor,
                         XMLFetcherDelegate<ResultType> delegate) {
        this.extractor = extractor;
        this.delegate = delegate;
    }

    public void startFetchingAsynchronously(String url, Context context) {
        WebFetcher fetcher = new WebFetcher(new NetworkDelegate());
        fetcher.startFetchingAsynchronously(url, context);
    }

    public void cancel() {
        WebFetcher fetcher = lastWebFetcher;
        if (fetcher != null)
            fetcher.invalidateRequest();
    }

    private class NetworkDelegate implements INetworkLoaderDelegate {
        @Override
        public void onFailure(String message) {
            delegate.onFailure(message);
        }

        @Override
        public void onSuccess(final InputStream data) {
            new ParseInBackgroundTask().execute(data);
        }
    }

    private class ParseInBackgroundTask
            extends AsyncTask<InputStream, Void, ResultType> {
        private String errorMessage = null;

        @Override
        protected ResultType doInBackground(InputStream... params) {
            XmlPullParser parser = Xml.newPullParser();
            InputStream data = params[0];
            try {
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(data, null);
            } catch (XmlPullParserException e) {
                throw new AssertionError(e);
            }

            try {
                return extractor.extractFromXML(parser);
            } catch (XmlPullParserException | ParseException e) {
                errorMessage = "Error parsing remote XML: " + e.getLocalizedMessage();
                Log.w(TAG, "parse error", e);
                return null;
            } catch (IOException e) {
                errorMessage = "Error retrieving remote XML: " + e.getLocalizedMessage();
                Log.w(TAG, "retrieve error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultType result) {
            if (errorMessage != null) {
                delegate.onFailure(errorMessage);
            } else {
                delegate.onSuccess(result);
            }
        }
    }

}
