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
    private final String url;

    // Tag used for cache file and for preference key of last fetch time
    private final String contentTag;

    private final ContentCache cache;

    // Reload document it cached content is older than this number of seconds
    private final int reloadIfOlderSeconds;

    private final ContentExtractor<ResultType> extractor;

    private final Delegate<ResultType> delegate;

    private final Context context;

    private volatile WebFetcher lastWebFetcher = null;

    private volatile boolean canceled = false;


    /**
     * Initialize a new cache.
     * @param URL the URL to load
     * @param contentTag the tag to use for this data in the cache. can be null if cache is null.
     * @param reloadIfOlderSeconds document is reloaded if cached data is older than this number of seconds.
     * @param cache the cache to use, can be null.
     * @param extractor extractor to parse ResultType from the loaded document
     * @param delegate object to receive parsed data
     * @param context current application context
     */
    public NetworkRetrieveAndCache(String URL, String contentTag, int reloadIfOlderSeconds,
                                   ContentCache cache,
                                   ContentExtractor<ResultType> extractor,
                                   Delegate<ResultType> delegate, Context context) {
        if (cache != null && contentTag == null)
            throw new NullPointerException("contentTag must not be null if cache is not null");
        this.url = URL;
        this.contentTag = contentTag;
        this.cache = cache;
        this.reloadIfOlderSeconds = reloadIfOlderSeconds;
        this.extractor = extractor;
        this.delegate = delegate;
        this.context = context;
    }

    public String getUrl() {
        return url;
    }

    public interface Delegate<ResultType> {
        void onUpdate(ResultType result);
        void onStartLoading();
        void onFailure(String message);
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

            Log.i(TAG, "Start loading '"+contentTag+"' for cache '"+cache.getName()+"' from URL '"+url+"'");
            WebFetcher fetcher = new WebFetcher(new NetworkDelegate());
            canceled = false;
            fetcher.startFetchingAsynchronously(url, context);
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
                Log.w(TAG, "parse error from URL '"+url+"'", e);
                return null;
            } catch (IOException e) {
                if (!canceled) {
                    errorMessage = "Error receiving remote content: " + e.getLocalizedMessage();
                    Log.w(TAG, "I/O error from URL '" + url + "'", e);
                }
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
        if (fetcher != null) {
            canceled = true;
            fetcher.invalidateRequest();
        }
    }

    private Pair<Date, ResultType> loadFromCache() {
        if (cache == null)
            return null;

        Pair<Date, byte[]> cached = cache.getContentWithAge(contentTag);
        if (cached == null)
            return null;

        ResultType data = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(cached.second));
            data = (ResultType)ois.readObject();
            ois.close();
        } catch (IOException e) {
            throw Util.makeAssertionError(e);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw Util.makeRuntimeException("Weird class error when deserializing", e);
        } finally {
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
                // ignore
            }
        }

        long ageSeconds = (System.currentTimeMillis() - cached.first.getTime())/1000;
        Log.i(TAG, "Using cached content '"+contentTag+"' (age: "+ageSeconds+" seconds) from cache '"+cache.getName()+"'");
        return new Pair<>(cached.first, data);
    }

    private void saveToCache(ResultType data) {
        if (cache == null)
            return;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(data);
            oos.close();
        } catch (IOException e) {
            throw Util.makeAssertionError(e);
        }

        cache.storeContent(contentTag, bos.toByteArray());

        Log.i(TAG, "Saved '"+contentTag+"' to cache '"+cache.getName()+"'");
    }
}
