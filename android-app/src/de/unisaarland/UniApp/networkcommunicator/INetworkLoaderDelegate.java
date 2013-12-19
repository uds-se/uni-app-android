package de.unisaarland.UniApp.networkcommunicator;


import org.xmlpull.v1.XmlPullParser;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 11/29/13
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface INetworkLoaderDelegate {

    public void onFailure(String message);

    /**
     * Automatically called when connect succeeds.
     *
     */
    public void onSuccess(XmlPullParser parser);
}
