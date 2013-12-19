package de.unisaarland.UniApp.bus.uihelper;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.BusDetailActivity;
import de.unisaarland.UniApp.bus.model.SearchStationModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/3/13
 * Time: 12:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchStationAdapter extends BaseAdapter {

    private final ArrayList<SearchStationModel> searchStationsArray;
    private final Context context;
    private final HashMap<View,Integer> searchStationsMap = new HashMap<View,Integer>();

    public SearchStationAdapter(Context context, ArrayList<SearchStationModel> searchStationsArray) {
        this.context = context;
        this.searchStationsArray = searchStationsArray;
    }
    @Override
    public int getCount() {
        return searchStationsArray.size();
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
        if(convertView == null){
            convertView = View.inflate(context, R.layout.bus_station_item, null);
        }
        SearchStationModel model = searchStationsArray.get(position);

        TextView stationTitle = (TextView) convertView.findViewById(R.id.bus_station_title);
        stationTitle.setText(model.getName());
        stationTitle.setVisibility(View.VISIBLE);
        TextView stationDescription = (TextView) convertView.findViewById(R.id.bus_station_detail);
        stationDescription.setText(model.getURL());
        stationDescription.setVisibility(View.VISIBLE);
        convertView.setOnClickListener(clickListener);
        searchStationsMap.put(convertView, position);
        return convertView;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(searchStationsMap.containsKey(v)){
                int index = searchStationsMap.get(v);
                SearchStationModel model = searchStationsArray.get(index);
                String url = "http://mobile.bahn.de/bin/mobil/query.exe/dox?country=DEU&rt=1&use_realtime_filter=1&webview=&searchMode=ADVANCED";
                Intent myIntent = new Intent(context, BusDetailActivity.class);
                myIntent.putExtra("url", url);
                context.startActivity(myIntent);
            }
        }
    };
}
