package de.unisaarland.UniApp.restaurant.uihelper;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.RestaurantActivity;
import de.unisaarland.UniApp.restaurant.model.MensaDayMenu;
import de.unisaarland.UniApp.restaurant.model.MensaItem;
import de.unisaarland.UniApp.utils.ui.RemoteOrLocalViewAdapter;

/**
 * Remote view factory and list adapter for the views for all the days in the
 * current mensa menu.
 */
public class MensaDaysAdapter extends RemoteOrLocalViewAdapter {
    private MensaDayMenu[] mensaItems;
    private final boolean isWidget;

    public MensaDaysAdapter(MensaDayMenu[] mensaItems, boolean isWidget) {
        this.isWidget = isWidget;
        this.mensaItems = mensaItems;
    }

    @Override
    public int getCount() {
        return mensaItems.length;
    }

    @Override
    public void buildView(int position, RemoteOrLocalViewAdapter.RemoteOrLocalViewBuilder builder) {
        builder.setLayout(R.layout.restaurant_layout_list);

        long dateMillis = mensaItems[position].getDayStartMillis();

        Date date = new Date(dateMillis);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        // Set date in current locale
        String datestring = DateFormat.getDateInstance(DateFormat.LONG).format(date);
        builder.setTextViewText(R.id.date_label, datestring);
        // Set day in current locale
        String daystring = dateCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                Locale.getDefault());
        builder.setTextViewText(R.id.day_label, daystring);

        MensaItem[] items = mensaItems[position].getItems();
        MensaItemsAdapter adapter = new MensaItemsAdapter(items, isWidget);
        builder.setAdapterOrUpdate(R.id.mensaList, adapter);

        if (isWidget) {
            Intent intent = new Intent(builder.getContext(), RestaurantActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(builder.getContext(), 0, intent, 0);
            builder.setOnItemClickIntent(R.id.mensaList, pendingIntent);
        }
    }

    @Override
    protected void update(RemoteOrLocalViewAdapter newAdapter) {
        MensaDaysAdapter adap = (MensaDaysAdapter) newAdapter;
        if (Arrays.equals(adap.mensaItems, mensaItems))
            return;
        mensaItems = adap.mensaItems;
        notifyDataSetChanged();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(mensaItems, flags);
        dest.writeByte(isWidget ? (byte)1 : 0);
    }

    public static final Parcelable.Creator<MensaDaysAdapter> CREATOR
            = new Parcelable.Creator<MensaDaysAdapter>() {

        @Override
        public MensaDaysAdapter createFromParcel(Parcel source) {
            MensaDayMenu[] items = (MensaDayMenu[]) source.readParcelableArray(
                    MensaDayMenu.class.getClassLoader());
            boolean isWidget = source.readByte() != 0;
            return new MensaDaysAdapter(
                    items, isWidget);
        }

        @Override
        public MensaDaysAdapter[] newArray(int size) {
            return new MensaDaysAdapter[size];
        }
    };
}
