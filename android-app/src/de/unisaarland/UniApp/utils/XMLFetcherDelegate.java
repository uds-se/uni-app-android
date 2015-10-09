package de.unisaarland.UniApp.utils;


/**
 * Receives results from the WebXMLFetcher.
 *
 * All methods are called in the UI thread.
 */
public interface XMLFetcherDelegate<ResultType> {

    void onFailure(String errorMessage);

    void onSuccess(ResultType result);

}
