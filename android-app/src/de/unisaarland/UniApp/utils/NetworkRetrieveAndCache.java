package de.unisaarland.UniApp.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.Date;


/**
 * This class implements the common behaviour of loading content from a remote URL,
 * parsing it, and caching it's content to avoid frequent reloading.
 */
public class NetworkRetrieveAndCache<ResultType> {

    private static final String TAG = NetworkRetrieveAndCache.class.getSimpleName();

    // URL to retrieve fresh content
    private final String URL;

    // Tag used for cache file and for preference key of last fetch time
    private final String contentTag;

    private final ContentCache cache;

    // Reload document it cached content is older than this number of seconds
    private final int reloadIfOlderSeconds;

    private final ContentExtractor<ResultType> extractor;

    private volatile WebFetcher lastWebFetcher = null;

    private final Delegate<ResultType> delegate;

    private final Context context;

    public NetworkRetrieveAndCache(String URL, String contentTag, int reloadIfOlderSeconds,
                                   ContentCache cache,
                                   ContentExtractor<ResultType> extractor,
                                   Delegate<ResultType> delegate, Context context) {
        this.URL = URL;
        this.contentTag = contentTag;
        this.cache = cache;
        this.reloadIfOlderSeconds = reloadIfOlderSeconds;
        this.extractor = extractor;
        this.delegate = delegate;
        this.context = context;
    }

    public static abstract class Delegate<ResultType> {
        public abstract void onUpdate(ResultType result);
        public abstract void onStartLoading();
        public abstract void onFailure(String message);
    }

    /**
     * load old content from cache file and start fetching new content if it is too old.
     */
    public void loadAsynchronously() {
        // We first load the cached data (and show it), then update the
        // document asynchronously if last fetch is too old
        Pair<Date, ResultType> cached = loadFromCache();
        if (cached != null)
            delegate.onUpdate(cached.second);
        boolean reloadContentFromWeb = cached == null
                || Math.abs(cached.first.getTime() - System.currentTimeMillis()) > 1000*reloadIfOlderSeconds;
        if (reloadContentFromWeb) {
            delegate.onStartLoading();

            Log.i(TAG, "Start loading '"+contentTag+"' for cache '"+cache.getName()+"' from URL '"+URL+"'");
            WebFetcher fetcher = new WebFetcher(new NetworkDelegate());
            fetcher.startFetchingAsynchronously(URL, context);
            lastWebFetcher = fetcher;
        }
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
            InputStream data = params[0];
            try {
                ResultType result = extractor.extract(data);
                saveToCache(result);
                return result;
            } catch (ParseException e) {
                errorMessage = "Error parsing remote content: " + e.getLocalizedMessage();
                Log.w(TAG, "parse error from URL '"+URL+"'", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultType result) {
            if (errorMessage != null) {
                delegate.onFailure(errorMessage);
            } else {
                delegate.onUpdate(result);
            }
        }
    }

    public void cancel() {
        WebFetcher fetcher = lastWebFetcher;
        if (fetcher != null)
            fetcher.invalidateRequest();
    }

    private Pair<Date, ResultType> loadFromCache() {
        Pair<Date, byte[]> cached = cache.getContentWithAge(contentTag);
        if (cached == null)
            return null;

        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cached.second));
            ResultType data = (ResultType)ois.readObject();
            ois.close();
            Log.i(TAG, "Using cached content '"+contentTag+"' from cache '"+cache.getName()+"'");
            return new Pair<>(cached.first, data);
        } catch (IOException e) {
            Log.w(TAG, "Cannot load data '"+contentTag+"' from cache '"+cache.getName()+"'", e);
            return null;
        } catch (ClassNotFoundException | ClassCastException e) {
            Log.e(TAG, "Weird class error when loading data '"+contentTag+"' from cache '"+cache.getName()+"'", e);
            return null;
        }
    }

    private void saveToCache(ResultType data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(data);
            oos.close();
        } catch (IOException e) {
            Log.w(TAG, "Cannot serialize data '"+contentTag+"' for cache '"+cache.getName()+"'", e);
            return;
        }

        cache.storeContent(contentTag, bos.toByteArray());

        Log.i(TAG, "Saved '"+contentTag+"' to cache '"+cache.getName()+"'");
    }
}
