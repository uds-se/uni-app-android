package de.unisaarland.UniApp.utils;


import java.io.InputStream;

/**
 * Receives results from the WebFetcher.
 *
 * All methods are called in the UI thread.
 */
public interface INetworkLoaderDelegate {

    void onFailure(String message);

    /**
     * Automatically called when connect succeeds.
     */
    void onSuccess(InputStream data);
}
