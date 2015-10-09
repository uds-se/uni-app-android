package de.unisaarland.UniApp.utils;


import java.io.InputStream;

public interface INetworkLoaderDelegate {

    void onFailure(String message);

    /**
     * Automatically called when connect succeeds.
     */
    void onSuccess(InputStream data);
}
