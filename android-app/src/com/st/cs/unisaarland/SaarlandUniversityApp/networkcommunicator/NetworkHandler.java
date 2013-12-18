package com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator;

import android.content.Context;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 11/28/13
 * Time: 11:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetworkHandler {
        private INetworkLoaderDelegate delegate;
        private WebFetcher fetcher = null;

        public NetworkHandler(INetworkLoaderDelegate delegate) {
            this.delegate = delegate;
        }

    public void connect(String urlStr,Context context) {
            fetcher = new WebFetcher(delegate);
            fetcher.startFetchingAsynchronously(urlStr,context);
    }

    public void invalidateRequest(){
        fetcher.invalidateRequest();
    }
}
