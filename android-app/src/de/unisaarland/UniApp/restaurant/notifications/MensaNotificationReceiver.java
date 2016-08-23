package de.unisaarland.UniApp.restaurant.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.RestaurantActivity;
import de.unisaarland.UniApp.restaurant.model.CachedMensaPlan;
import de.unisaarland.UniApp.restaurant.model.MensaDayMenu;
import de.unisaarland.UniApp.restaurant.model.MensaItem;
import de.unisaarland.UniApp.restaurant.uihelper.MensaDaysAdapter;

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
        MensaDayMenu todayMenu = CachedMensaPlan.getTodaysMenuIfLoaded(context);
        if (todayMenu == null) {
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

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.uds_owl_silhouette)
                .setContentTitle(context.getString(R.string.mensa_notification_title))
                .setContentText(context.getString(R.string.mensa_notification_text_click))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notifBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notifBuilder.setContentText(context.getString(R.string.mensa_notification_text_expand));
            CharSequence text = getMensaMenuText(todayMenu);
            notifBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        }

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(R.id.notification_id_mensa_menu);
        mNotifyMgr.notify(R.id.notification_id_mensa_menu, notifBuilder.build());
    }

    private CharSequence getMensaMenuText(MensaDayMenu mensaMenu) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (MensaItem item : mensaMenu.getItems()) {
            if (sb.length() > 0)
                sb.append("\n");
            sb.append("• ").append(item.getTitleSpannable(false));
            /*
            int oldLen = sb.length();
            sb.append("• ").append(item.getCategory()).append(": ").append(item.getTitleSpannable(
                    false));
            int newLen = sb.length();
            sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), oldLen, newLen,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            CharSequence desc = item.getDescSpannable(false);
            if (desc.length() > 0)
                sb.append(" (").append(desc).append(")");
            */
        }
        return sb;
    }

    public RemoteViews getTodaysMensaMenuView(Context context, MensaDayMenu mensaMenu) {
        MensaDaysAdapter adap = new MensaDaysAdapter(new MensaDayMenu[] { mensaMenu }, true);
        return adap.asRemoteViewsFactory(context, true).getViewAt(0);
    }
}
