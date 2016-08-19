package de.unisaarland.UniApp.restaurant.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.CachedMensaPlan;
import de.unisaarland.UniApp.restaurant.model.MensaNotificationTimes;
import de.unisaarland.UniApp.utils.Util;

public class MensaNotifications {

    private static final String TAG = MensaNotifications.class.getSimpleName();

    private final Context context;

    private static final int weekDays[] = { Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
        Calendar.THURSDAY, Calendar.FRIDAY };

    public MensaNotifications(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setNext() {
        // load the current notification times
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        long rawTimes = settings.getLong(context.getString(R.string.pref_mensa_notification_times),
                MensaNotificationTimes.DEFAULT_TIMES);
        MensaNotificationTimes times = new MensaNotificationTimes(rawTimes);
        long currentMillis = System.currentTimeMillis();
        long nextMillis = getNextNotificationMillis(currentMillis, times);
        setBootReceiverEnabled(nextMillis != 0);
        if (nextMillis == 0) {
            Log.i(TAG, "no notifications enabled");
            return;
        }

        // strategy: at 30 minutes before the notification, start trying to load the mensa menu.
        // load again 120-40 seconds before notification.
        // add jitter / randomness to all dates, to not overload the server.

        long millisForAlarm;
        String action;
        Random rand = new Random();

        // if more than 30 minutes before notification: schedule within 30 and 28 minutes before,
        // but only preload if not loaded within 20 minutes
        if (currentMillis < nextMillis - 30 * 60 * 1000) {
            millisForAlarm = nextMillis - 30 * 60 * 1000 + rand.nextInt(120 * 1000);
            action = "preload-20";
        }
        // if more than 3 minutes before the notification, and not loaded within
        // the last 60 minutes, schedule within 30-90 seconds
        else if (currentMillis < nextMillis - 2 * 60 * 1000 &&
                !CachedMensaPlan.loadedSince(currentMillis - 30 * 60 * 1000, context)) {
            long min = currentMillis + 30 * 1000;
            long max = Math.min(min + 60 * 1000, nextMillis - 45 * 1000);
            millisForAlarm = min + rand.nextInt((int) (max - min));
            action = "preload-60";
        }
        // if more than 45 seconds before the notification, and not loaded within
        // the last 5 minutes, reload (for freshest data ;) )
        else if (currentMillis < nextMillis - 30 * 1000 &&
                !CachedMensaPlan.loadedSince(currentMillis - 5 * 30 * 1000, context)) {
            long min = Math.max(currentMillis, nextMillis - 90 * 1000);
            long max = nextMillis - 30 * 1000;
            millisForAlarm = min + rand.nextInt((int) (max - min));
            action = "preload-5";
        }
        // otherwise schedule at the notification time for showing the notification
        else {
            millisForAlarm = nextMillis;
            action = "show";
        }

        Log.i(TAG, "scheduling mensa notifications alarm for " + action + " at " +
                new Date(millisForAlarm));

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MensaNotificationReceiver.class);
        intent.setAction(action);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.set(AlarmManager.RTC_WAKEUP, millisForAlarm, alarmIntent);
    }

    private long getNextNotificationMillis(long currentMillis, MensaNotificationTimes times) {
        long nextMillis = 0;
        for (int day = 0; day < 5; ++day) {
            if (!times.isEnabled(day))
                continue;
            // move calendar to the right weekday
            Calendar cal = Util.getStartOfDay(currentMillis);
            cal.set(Calendar.HOUR, times.getHour(day));
            cal.set(Calendar.MINUTE, times.getMinute(day));
            while (cal.get(Calendar.DAY_OF_WEEK) != weekDays[day]
                    || cal.getTimeInMillis() <= currentMillis)
                cal.add(Calendar.DATE, 1);
            if (nextMillis == 0 || cal.getTimeInMillis() < nextMillis)
                nextMillis = cal.getTimeInMillis();
        }
        return nextMillis;
    }

    private void setBootReceiverEnabled(boolean enable) {
        ComponentName receiver = new ComponentName(context, MensaNotificationReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                       : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
