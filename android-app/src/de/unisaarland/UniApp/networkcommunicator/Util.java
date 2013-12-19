package de.unisaarland.UniApp.networkcommunicator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 11/28/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {

    public static String PREFS_NAME = "myPreferences";
    public static final String NEWS_LOADED = "newsloaded";
    public static final String EVENTS_LOADED = "eventsloaded";
    public static final String NEWS_FILE_NAME = "news.dat";
    public static final String TEMP_STAFF_SEARCH_FILE = "staff.dat";

    public static String urlEncode(String url) {
        String[] parts = url.split(" ");
        StringBuffer strBuf = new StringBuffer(parts.length + 1);
        for (int i = 0; i < parts.length; i++) {
            strBuf.append(parts[i]);
            if ((i + 1) != (parts.length)) {
                strBuf.append("%20");
            }
        }
        String result = strBuf.toString();
        return result;
    }

    public static String cleanHtmlCodeInString(String str){
        String result = str;
        result = result.replace("<b>","");
        result = result.replace("</b>","");
        result = result.replace("(at)","@");
//        result = result.replace("</p>","\n");
//        //result = result.replaceAll("<[^>]+>","") ;
//        String[] temp = result.split("<[^>]+>");
//        String tempR = "";
//        for (int i =0;i<temp.length;i++){
//            tempR = tempR+temp[i];//.replaceFirst("<[^>]+>","");
//        }
////        result=result.replace("\r","").replace("\n","");
////        result = result.trim();
//        result = tempR;
//        result = result.replace("\n\n\n","\n\n");
        return result ;
    }


    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
