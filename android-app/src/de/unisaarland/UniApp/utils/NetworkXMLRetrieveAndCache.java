package de.unisaarland.UniApp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * This class implements the common behaviour of loading content from a remote XML,
 * parsing it, and caching it's content to avoid frequent reloading.
 */
public class NetworkXMLRetrieveAndCache<ResultType> {

    private static final String TAG = NetworkXMLRetrieveAndCache.class.getSimpleName();

    // URL to retrieve fresh content
    private final String URL;

    // Tag used for cache file and for preference key of last fetch time
    private final String contentTag;

    // Reload document it cached content is older than this number of seconds
    private final long reloadIfOlderSeconds;

    private final XMLExtractor<ResultType> xmlExtractor;

    private volatile WebXMLFetcher lastWebFetcher = null;

    private final Delegate<ResultType> delegate;

    private final Context context;

    public NetworkXMLRetrieveAndCache(String URL, String contentTag, long reloadIfOlderSeconds,
                                      XMLExtractor<ResultType> xmlExtractor,
                                      Delegate<ResultType> delegate, Context context) {
        this.URL = URL;
        this.contentTag = contentTag;
        this.reloadIfOlderSeconds = reloadIfOlderSeconds;
        this.xmlExtractor = xmlExtractor;
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
    public void load() {
        SharedPreferences settings = context.getSharedPreferences(Util.PREFS_NAME, 0);
        long lastLoadMillis = settings.getLong("last-xml-fetch-"+contentTag, 0);
        // We first load the cached data (and show it), then update the
        // document asynchronously if last fetch is too old
        ResultType cachedStuff = loadFromCache();
        if (cachedStuff != null)
            delegate.onUpdate(cachedStuff);
        long cachedNewsAgeMillis = Math.abs(lastLoadMillis - System.currentTimeMillis());
        // Reload after `reloadIfOlderSeconds` minutes
        if (cachedStuff == null || cachedNewsAgeMillis >= 1000*reloadIfOlderSeconds) {
            delegate.onStartLoading();

            WebXMLFetcher fetcher = new WebXMLFetcher(xmlExtractor, new XMLListener());
            fetcher.startFetchingAsynchronously(URL, context);
            lastWebFetcher = fetcher;
        }
    }

    public void cancel() {
        WebXMLFetcher fetcher = lastWebFetcher;
        if (fetcher != null)
            fetcher.cancel();
    }

    private final class XMLListener implements XMLFetcherDelegate<ResultType> {
        @Override
        public void onFailure(String message) {
            delegate.onFailure(message);
        }

        @Override
        public void onSuccess(ResultType data) {
            saveToCache(data);
            SharedPreferences.Editor editor = context.getSharedPreferences(Util.PREFS_NAME, 0).edit();
            editor.putLong("last-xml-fetch-"+contentTag, System.currentTimeMillis());
            editor.commit();
            delegate.onUpdate(data);
        }
    }

    private ResultType loadFromCache() {
        File file = new File(context.getCacheDir(), "cached-xml-"+contentTag);
        if (!file.exists()) {
            Log.w(TAG, "Cache file does not exist: "+file);
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            ResultType cached = (ResultType)ois.readObject();
            ois.close();
            return cached;
        } catch (IOException e) {
            Log.w(TAG, "Cannot load data from cache file '"+file+"'", e);
            return null;
        } catch (ClassNotFoundException | ClassCastException e) {
            Log.e(TAG, "Weird class error when loading data from cache file '"+file+"'", e);
            return null;
        }
    }

    private void saveToCache(ResultType data) {
        File file = new File(context.getCacheDir(), "cached-xml-"+contentTag);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(data);
            oos.close();
        } catch (IOException e) {
            Log.w(TAG, "Cannot save data to cache file '"+file+"'", e);
            return;
        }

        Log.i(TAG, "Saved events to cache");
    }
}
