package de.unisaarland.UniApp.restaurant.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.RestaurantActivity;
import de.unisaarland.UniApp.restaurant.model.CachedMensaPlan;
import de.unisaarland.UniApp.restaurant.model.MensaItem;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;

public class MensaNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = MensaNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            new MensaNotifications(context).setNext();
        } else if (action.startsWith("preload")) {
            int ifOlderThanSeconds = 0;
            if (action.startsWith("preload-")) {
                try {
                    ifOlderThanSeconds = Integer.valueOf(action.substring(8)) * 60;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Illegal number for preload: " + action);
                }
            }
            Log.i(TAG, "preloading mensa plan if older than " + ifOlderThanSeconds + " seconds");
            CachedMensaPlan plan = new CachedMensaPlan(null, context);
            plan.load(ifOlderThanSeconds);
        } else if (action.equals("show")) {
            Log.i(TAG, "showing mensa menu notification");
            showNotification(context);
        } else
            Log.e(TAG, "Unknown action: " + action);

        // and trigger the next alarm
        new MensaNotifications(context).setNext();
    }

    private void showNotification(Context context) {
        final List<MensaItem>[] todayList = new List[1];
        final long today = Util.getStartOfDay().getTimeInMillis();

        NetworkRetrieveAndCache.Delegate<Map<Long, List<MensaItem>>> delegate =
                new NetworkRetrieveAndCache.Delegate<Map<Long, List<MensaItem>>>() {
            public boolean hasShown = false;

            @Override
            public void onUpdate(Map<Long, List<MensaItem>> result) {
                if (hasShown)
                    throw new AssertionError("we should only be updated once");
                hasShown = true;
                todayList[0] = result.get(today);
            }

            @Override
            public void onStartLoading() {
                throw new AssertionError("we should not load");
            }

            @Override
            public void onFailure(String message) {
                throw new AssertionError("without loading there should be no failure");
            }

            @Override
            public String checkValidity(Map<Long, List<MensaItem>> result) {
                return null;
            }
        };
        CachedMensaPlan plan = new CachedMensaPlan(delegate, context);
        plan.load(-1);
        if (todayList[0] == null) {
            Log.w(TAG, "no cached mensa menu, skipping notification");
            //showErrorNotification();
            return;
        }

        // create the intent for opening the full mensa menu
        Intent intent = new Intent(context, RestaurantActivity.class);

        // create an artificial stack which takes the user back through the parent activities
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack
        stackBuilder.addParentStack(RestaurantActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(intent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        CharSequence text = getMensaMenuText(todayList[0]);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.restaurant_icon)
                .setContentTitle(context.getString(R.string.mensa_notification_title))
                .setContentText(context.getString(R.string.mensa_nofitication_text_expand))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(R.id.notification_id_mensa_menu, notifBuilder.build());
    }

    private CharSequence getMensaMenuText(List<MensaItem> mensaItems) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (MensaItem item : mensaItems) {
            if (sb.length() > 0)
                sb.append("\n");
            int oldLen = sb.length();
            sb.append("• ").append(item.getCategory()).append(": ").append(item.getTitleSpannable(false));
            int newLen = sb.length();
            sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), oldLen, newLen,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            CharSequence desc = item.getDescSpannable(false);
            if (desc.length() > 0)
                sb.append(" (").append(desc).append(")");
        }
        return sb;
    }
}
