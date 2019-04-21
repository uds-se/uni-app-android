package de.unisaarland.UniApp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Util {

    public static final String EVENTS_URL =
            "https://www.uni-saarland.de/universitaet/aktuell/veranstaltungen.html?type=100&tx_ttnews[cat]=30";
    public static final String NEWS_URL =
            "https://www.uni-saarland.de/universitaet/aktuell/pm.html?type=100&tx_ttnews[cat]=26";

    public static final String SUPPORT_MAIL = "uniapp@uni-saarland.de";


    // check if internet is connected
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static Calendar getStartOfDay(long time) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(time);
        return new GregorianCalendar(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    public static Calendar getStartOfDay() {
        GregorianCalendar now = new GregorianCalendar();
        return new GregorianCalendar(now.get(Calendar.YEAR),
                now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
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

    /**
     * Get a value from either the extras passed in the intent, or from saved instance state.
     * The extras have precedence if both exist. If none exist, the specified default value is
     * returned.
     */
    public static Object getExtra(String key, Bundle savedInstanceState, Bundle extras,
                                  Object defaultValue) {
        Object value;
        if (extras != null && (value = extras.get(key)) != null)
            return value;
        if (savedInstanceState != null && (value = savedInstanceState.get(key)) != null)
            return value;
        return defaultValue;
    }

    public static View findParentWithId(View v, int id) {
        while (v.getId() != id) {
            ViewParent parent = v.getParent();
            if (!(parent instanceof View))
                return null;
            v = (View) parent;
        }
        return v; // null or parent with given id
    }

}
