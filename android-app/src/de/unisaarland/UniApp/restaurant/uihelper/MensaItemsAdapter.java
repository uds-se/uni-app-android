package de.unisaarland.UniApp.restaurant.uihelper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaItem;

public class MensaItemsAdapter extends BaseAdapter {
    private Map<Long, List<MensaItem>> mensaItems;
    private final Context context;
    private long[] dates;

    public MensaItemsAdapter(Context context, Map<Long, List<MensaItem>> mensaItems) {
        if (context == null || mensaItems == null)
            throw new NullPointerException();
        this.context = context;
        this.mensaItems = mensaItems;
        recomputeDates();
    }

    private void recomputeDates() {
        dates = new long[mensaItems.size()];

        int idx = 0;
        for (Long l : mensaItems.keySet())
            dates[idx++] = l;
        Arrays.sort(dates);
    }

    @Override
    public int getCount() {
        return mensaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mensaItems.get(dates[position]);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context,R.layout.restaurant_layout_list, null);
        TextView date_label = (TextView) convertView.findViewById(R.id.date_label);
        TextView day_label = (TextView) convertView.findViewById(R.id.day_label);

        Date date = new Date(dates[position]);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        // Set date in current locale
        String datestring = DateFormat.getDateInstance(DateFormat.LONG).format(date);
        date_label.setText(datestring);
        // Set day in current locale
        String daystring = dateCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                Locale.getDefault());
        day_label.setText(daystring);
        ListView mensaList = (ListView) convertView.findViewById(R.id.mensaList);
        List<MensaItem> items = mensaItems.get(dates[position]);
        RestaurantAdapter adapter = (RestaurantAdapter) mensaList.getAdapter();
        if (adapter == null) {
            mensaList.setAdapter(new RestaurantAdapter(context, items));
        } else {
            adapter.update(items);
        }
        convertView.setTag(R.id.mensa_menu_date_tag, Long.valueOf(dates[position]));
        return convertView;
    }

    public boolean update(Map<Long, List<MensaItem>> items) {
        if (items == null)
            throw new NullPointerException();
        if (mensaItems.equals(items))
            return false;
        mensaItems = items;
        recomputeDates();
        this.notifyDataSetChanged();
        return true;
    }
}
