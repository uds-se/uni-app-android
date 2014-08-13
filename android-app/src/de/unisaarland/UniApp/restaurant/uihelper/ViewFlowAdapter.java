package de.unisaarland.UniApp.restaurant.uihelper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaItem;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/7/13
 * Time: 3:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class ViewFlowAdapter extends BaseAdapter {
    private final HashMap<String, ArrayList<MensaItem>> mensaItemsDictionary;
    private final Context context;
    private final ArrayList<String> keysList;

    public ViewFlowAdapter(Context context, HashMap<String, ArrayList<MensaItem>> mensaItemsDictionary, ArrayList<String> keysList) {
        this.context = context;
        this.mensaItemsDictionary = mensaItemsDictionary;
        this.keysList = keysList;
    }

    @Override
    public int getCount() {
        return mensaItemsDictionary.size();
    }

    @Override
    public Object getItem(int position) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getItemId(int position) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(context,R.layout.restaurant_layout_list, null);
        TextView topLabel = (TextView) convertView.findViewById(R.id.top_label);
        Date d = new Date(Long.parseLong(keysList.get(position))*1000);
        topLabel.setText(d.toString().substring(0,11));
        topLabel.setVisibility(View.VISIBLE);
        ListView mensaList = (ListView) convertView.findViewById(R.id.mensaList);
        mensaList.setAdapter(new RestaurantAdapter(context,mensaItemsDictionary.get(keysList.get(position))));
        return convertView;
    }
}
