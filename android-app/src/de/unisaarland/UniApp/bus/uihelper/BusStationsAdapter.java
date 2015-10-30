package de.unisaarland.UniApp.bus.uihelper;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.BusDetailActivity;
import de.unisaarland.UniApp.bus.model.PointOfInterest;


public class BusStationsAdapter extends BaseAdapter {
    private final Context context;
    private final List<PointOfInterest> pointOfInterestsArray;
    private final Map<View,Integer> pointOfInterestsMap = new HashMap<>();
    private Location currentLocation = null;
    private String provider;

    public BusStationsAdapter(Context context, List<PointOfInterest> pointOfInterestsArray, Location currentLocation, String provider) {
        this.context = context;
        this.pointOfInterestsArray = pointOfInterestsArray;
        this.currentLocation = currentLocation;
        this.provider = provider;
    }

    public void setCurrentLocation(Location loc){
        currentLocation = loc;
    }

    @Override
    public int getCount() {
        return pointOfInterestsArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getItemId(int position) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //generate the row items of a list and update the distance from current position to bus stop bu using the current location
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(context, R.layout.bus_station_item, null);
        }
        PointOfInterest model = pointOfInterestsArray.get(position);

        TextView stationTitle = (TextView) convertView.findViewById(R.id.bus_station_title);
        stationTitle.setText(model.getTitle());
        stationTitle.setVisibility(View.VISIBLE);
        TextView stationDescription = (TextView) convertView.findViewById(R.id.bus_station_detail);
        if(currentLocation != null){
            Location lc = new Location(provider);
            lc.setLatitude(model.getLatitude());
            lc.setLongitude(model.getLongitude());
            float distanceInM = currentLocation.distanceTo(lc);

            if (distanceInM > 1000) {
                stationDescription.setText(String.format("%.1f km",distanceInM/1000));
            } else {
                if (distanceInM > 25) {
                    stationDescription.setText(String.format("%.0f m",distanceInM));
                } else {
                    if (distanceInM > 0.001) {
                        stationDescription.setText(context.getString(R.string.at_station));
                    } else {
                        stationDescription.setText("");
                    }
                }
            }
            stationDescription.setVisibility(View.VISIBLE);
        }else{
            stationDescription.setVisibility(View.INVISIBLE);
        }

        convertView.setOnClickListener(clickListener);
        pointOfInterestsMap.put(convertView, position);
        return convertView;
    }

    // On clicking of any item will call the BudDetailActivity and will pass it the url to be opened.
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(pointOfInterestsMap.containsKey(v)){
                int index = pointOfInterestsMap.get(v);
                PointOfInterest model = pointOfInterestsArray.get(index);
                String url = model.getWebsite();
                Intent myIntent = new Intent(context, BusDetailActivity.class);
                myIntent.putExtra("url", url);
                myIntent.putExtra("back", model.getTitle());
                context.startActivity(myIntent);
            }
        }
    };
}
