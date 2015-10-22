package de.unisaarland.UniApp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Util {

    public static final String EVENTS_URL =
            "http://www.uni-saarland.de/aktuelles/veranstaltungen/alle-veranstaltungen/rss.xml";
    public static final String NEWS_URL =
            "http://www.uni-saarland.de/aktuelles/presse/pms.html?type=100&tx_ttnews[cat]=26";

    // check if internet is connected
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static Date getStartOfDay(long time) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(time);
        return new GregorianCalendar(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).getTime();
    }

    public static Date getStartOfDay() {
        GregorianCalendar now = new GregorianCalendar();
        return new GregorianCalendar(now.get(Calendar.YEAR),
                now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).getTime();
    }

    /**
     * Return the general remote content cache for stuff like news, events, mensa plan, ....
     * Old elements are discarded after 30 days.
     */
    private static ContentCache cache = null;
    public static synchronized ContentCache getContentCache(Context context) {
        if (cache == null)
            cache = new ContentCache(context, "content", 60*60*24*30);
        return cache;
    }

    public static AssertionError makeAssertionError(Exception e) {
        AssertionError ae = new AssertionError(e.toString());
        ae.setStackTrace(e.getStackTrace());
        return ae;
    }

    public static RuntimeException makeRuntimeException(String msg, Exception e) {
        String fullMsg = e.toString();
        if (msg != null && !msg.isEmpty())
            fullMsg = msg + ": " + fullMsg;
        RuntimeException re = new RuntimeException(fullMsg);
        re.setStackTrace(e.getStackTrace());
        return re;
    }

    public static RuntimeException makeRuntimeException(Exception e) {
        return makeRuntimeException(null, e);
    }
}
