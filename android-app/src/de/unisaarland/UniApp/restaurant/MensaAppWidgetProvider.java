package de.unisaarland.UniApp.restaurant;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import de.unisaarland.UniApp.restaurant.model.CachedMensaPlan;
import de.unisaarland.UniApp.restaurant.model.MensaDayMenu;
import de.unisaarland.UniApp.restaurant.uihelper.MensaDaysAdapter;

public class MensaAppWidgetProvider extends AppWidgetProvider {

    private static final String TAG = MensaAppWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateAllWidgets(context);
    }

    public static void updateAllWidgets(Context context) {
        MensaDayMenu todayMenu = CachedMensaPlan.getTodaysMenuIfLoaded(context);
        if (todayMenu == null) {
            Log.w(TAG, "no cached mensa menu, skipping widget update and triggering loading");
            new CachedMensaPlan(null, context).load(30*60);
            return;
        }

        MensaDaysAdapter adap = new MensaDaysAdapter(new MensaDayMenu[] { todayMenu }, true);
        RemoteViews newView = adap.asRemoteViewsFactory(context, false).getViewAt(0);
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        man.updateAppWidget(new ComponentName(context, MensaAppWidgetProvider.class), newView);
    }
}