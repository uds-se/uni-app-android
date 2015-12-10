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

    private final ContentExtractor<ResultType> extractor;

    private final Delegate<ResultType> delegate;

    private final Context context;

    private WebFetcher lastWebFetcher = null;
    private AsyncTask<InputStream, Void, ResultType> lastAsyncTask;

    /**
     * Initialize a new cache.
     * @param URL the URL to load
     * @param contentTag the tag to use for this data in the cache. can be null if cache is null.
     * @param cache the cache to use, can be null.
     * @param extractor extractor to parse ResultType from the loaded document
     * @param delegate object to receive parsed data
     * @param context current application context
     */
    public NetworkRetrieveAndCache(String URL, String contentTag,
                                   ContentCache cache,
                                   ContentExtractor<ResultType> extractor,
                                   Delegate<ResultType> delegate, Context context) {
        if (cache != null && contentTag == null)
            throw new NullPointerException("contentTag must not be null if cache is not null");
        this.url = URL;
        this.contentTag = contentTag;
        this.cache = cache;
        this.extractor = extractor;
        this.delegate = delegate;
        this.context = context;
    }

    public String getUrl() {
        return url;
    }

    public boolean loadedSince(long timeMillis) {
        if (cache == null)
            throw new IllegalArgumentException("cache is null");

        Date date = cache.getContentAge(contentTag);
        return date != null && date.getTime() >= timeMillis;
    }

    public interface Delegate<ResultType> {
        /**
         * Called in the UI thread whenever new data was retrieved (from the cached, or from
         * network).
         */
        void onUpdate(ResultType result);

        /**
         * Called in the UI thread when starting to load data from the network. Can be used to show
         * a spinner or something.
         */
        void onStartLoading();

        /**
         * Called in the UI thread when an error occured (network error, parser error, or custom
         * error from checkValidity().
         * @param message the error message
         */
        void onFailure(String message);

        /**
         * Called by any thread (!) to check whether the retrieved result from network is valid.
         * Return a custom error string to describe what is wrong this the data. This will be passed
         * on to the onFailure method.
         * @param result the parsed result
         * @return null if the data is valid, a custom error message otherwise
         */
        String checkValidity(ResultType result);
    }

    /**
     * Load old content from cache file and start fetching new content if it is too old.
     * @param reloadIfOlderSeconds document is reloaded if cached data is older than this number of
     *                             seconds. Pass 0 to always reload, and -1 to never reload.
     * @return true if the data was initially loaded from the cache, false otherwise. in any case,
     *         reloading might have been triggered.
     */
    public boolean loadAsynchronously(int reloadIfOlderSeconds) {
        // We first load the cached data (and show it), then update the
        // document asynchronously if last fetch is too old
        Pair<Date, ResultType> cached = loadFromCache();
        if (cached != null)
            delegate.onUpdate(cached.second);
        boolean reloadContentFromWeb = reloadIfOlderSeconds >= 0 && (cached == null
                || reloadIfOlderSeconds == 0
                || Math.abs(cached.first.getTime() - System.currentTimeMillis()) >
                   1000L*reloadIfOlderSeconds);
        if (reloadContentFromWeb) {
            delegate.onStartLoading();

            Log.i(TAG, "Start loading '"+contentTag+"' for cache '"+cache.getName()+"' from URL '"+url+"'");
            WebFetcher fetcher = new WebFetcher(new NetworkDelegate());
            fetcher.startFetchingAsynchronously(url, context);
            lastWebFetcher = fetcher;
        }
        return cached != null;
    }

    private class NetworkDelegate implements INetworkLoaderDelegate {
        @Override
        public void onFailure(String message) {
            delegate.onFailure(message);
        }

        @Override
        public void onSuccess(final InputStream data) {
            ParseInBackgroundTask task = new ParseInBackgroundTask();
            lastAsyncTask = task;
            task.execute(data);
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
                String customError = delegate.checkValidity(result);
                if (customError != null) {
                    errorMessage = customError;
                    return null;
                }
                saveToCache(result);
                return result;
            } catch (ParseException e) {
                errorMessage = "Error parsing remote content: " + e.getLocalizedMessage();
                Log.w(TAG, "parse error from URL '"+url+"'", e);
                return null;
            } catch (IOException e) {
                if (!isCancelled()) {
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
        if (lastWebFetcher != null)
            lastWebFetcher.invalidateRequest();
        if (lastAsyncTask != null)
            lastAsyncTask.cancel(true);
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
            //noinspection unchecked
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
