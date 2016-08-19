package de.unisaarland.UniApp.restaurant.uihelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;

import java.util.Arrays;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaItem;
import de.unisaarland.UniApp.utils.ui.RemoteOrLocalViewAdapter;

/**
 * Remote view factory and list adapter for the mensa items of one specific day.
 */
public class MensaItemsAdapter extends RemoteOrLocalViewAdapter {
    boolean isWidget;
    private MensaItem[] items;

    public MensaItemsAdapter(MensaItem[] items, boolean isWidget) {
        this.isWidget = isWidget;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public void buildView(int position, RemoteOrLocalViewBuilder builder) {
        builder.setLayout(R.layout.mensa_item);

        MensaItem model = items[position];

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                builder.getContext());
        boolean showIngredients = settings.getBoolean(
                builder.getContext().getString(R.string.pref_mensa_ingredients), true);

        builder.setTextViewText(R.id.mensa_menu_category, model.getCategory());
        builder.setTextViewText(R.id.mensa_menu_title,
                model.getTitleSpannable(showIngredients));
        builder.setTextViewText(R.id.mensa_menu_description,
                model.getDescSpannable(showIngredients));

        String[] labels = model.getLabels();
        if (!isWidget && labels != null && labels.length != 0 && showIngredients) {
            Intent intent = new Intent(builder.getContext(),
                    MensaShowIngredientsActivity.class);
            intent.setAction(MensaShowIngredientsActivity.ACTION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(MensaShowIngredientsActivity.encodeLabels(labels));
            builder.setOnClickIntent(R.id.img_info, intent);
            builder.setViewVisibility(R.id.img_info, View.VISIBLE);
        } else {
            builder.setViewVisibility(R.id.img_info, View.GONE);
        }

        builder.setBackgroundColor(R.id.contentBackground, model.getColor());

        if (model.getPreis1() != 0) {
            String text = builder.getContext().getString(R.string.mensaPriceFormat,
                    .01 * model.getPreis1(), .01 * model.getPreis2(), .01 * model.getPreis3());
            builder.setTextViewText(R.id.mensa_menu_price, text);
            builder.setViewVisibility(R.id.mensa_menu_price, View.VISIBLE);
        } else {
            builder.setViewVisibility(R.id.mensa_menu_price, View.INVISIBLE);
        }

        if (isWidget) {
            Intent intent = new Intent();
            intent.putExtra("position", position);
            builder.setItemFillInIntent(R.id.mensa_item, intent);
        }
    }

    /**
     * Update the views on this adapter. This only works if this class is used as list adapter.
     * Remote views have to be updated manually.
     */
    @Override
    protected void update(RemoteOrLocalViewAdapter newAdapter) {
        MensaItemsAdapter adap = (MensaItemsAdapter) newAdapter;
        if (Arrays.equals(items, adap.items))
            return;
        items = adap.items;
        notifyDataSetChanged();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(items.length);
        dest.writeByte(isWidget ? (byte)1 : 0);
        for (MensaItem item : items)
            dest.writeParcelable(item, flags);
    }

    public static final Parcelable.Creator<MensaItemsAdapter> CREATOR
            = new Parcelable.Creator<MensaItemsAdapter>() {

        @Override
        public MensaItemsAdapter createFromParcel(Parcel source) {
            int len = source.readInt();
            boolean isWidget = source.readByte() != 0;
            MensaItem[] items = new MensaItem[len];
            for (int i = 0; i < len; ++i)
                items[i] = source.readParcelable(MensaItem.class.getClassLoader());
            return new MensaItemsAdapter(items, isWidget);
        }

        @Override
        public MensaItemsAdapter[] newArray(int size) {
            return new MensaItemsAdapter[size];
        }
    };
}
