package de.unisaarland.UniApp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;


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

    private final ContentCache cache;

    // Reload document it cached content is older than this number of seconds
    private final int reloadIfOlderSeconds;

    private final XMLExtractor<ResultType> xmlExtractor;

    private volatile WebXMLFetcher lastWebFetcher = null;

    private final Delegate<ResultType> delegate;

    private final Context context;

    public NetworkXMLRetrieveAndCache(String URL, String contentTag, int reloadIfOlderSeconds,
                                      ContentCache cache,
                                      XMLExtractor<ResultType> xmlExtractor,
                                      Delegate<ResultType> delegate, Context context) {
        this.URL = URL;
        this.contentTag = contentTag;
        this.cache = cache;
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

    private Pair<Date, ResultType> loadFromCache() {
        Pair<Date, byte[]> cached = cache.getContentWithAge(contentTag);
        if (cached == null)
            return null;

        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cached.second));
            ResultType data = (ResultType)ois.readObject();
            ois.close();
            return new Pair<>(cached.first, data);
        } catch (IOException e) {
            Log.w(TAG, "Cannot load data '"+contentTag+"' from cache", e);
            return null;
        } catch (ClassNotFoundException | ClassCastException e) {
            Log.e(TAG, "Weird class error when loading data '"+contentTag+"' from cache", e);
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
            Log.w(TAG, "Cannot serialize data '"+contentTag+"'", e);
            return;
        }

        cache.storeContent(contentTag, bos.toByteArray());

        Log.i(TAG, "Saved '"+contentTag+"' to cache");
    }
}
