package de.unisaarland.UniApp.restaurant.uihelper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaItem;

public class ViewFlowAdapter extends BaseAdapter {
    private final Map<Long, List<MensaItem>> mensaItems;
    private final Context context;
    private final long[] dates;

    public ViewFlowAdapter(Context context, Map<Long, List<MensaItem>> mensaItems) {
        this.context = context;
        this.mensaItems = mensaItems;
        this.dates = new long[mensaItems.size()];

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

        Date d = new Date(dates[position]);
        //Set date
        SimpleDateFormat parserSDF = new SimpleDateFormat("d. MMMM yyyy");
        String datestring = parserSDF.format(d);
        date_label.setText(datestring);
        date_label.setVisibility(View.VISIBLE);
        //Set day
        parserSDF = new SimpleDateFormat("EEEE");
        String daystring = parserSDF.format(d);
        day_label.setText(daystring);
        day_label.setVisibility(View.VISIBLE);
        ListView mensaList = (ListView) convertView.findViewById(R.id.mensaList);
        List<MensaItem> items = mensaItems.get(dates[position]);
        mensaList.setAdapter(new RestaurantAdapter(context, items));
        return convertView;
    }
}
