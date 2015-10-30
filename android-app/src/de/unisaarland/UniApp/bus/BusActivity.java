package de.unisaarland.UniApp.bus;

import android.location.Location;
import android.os.Bundle;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.bus.model.SearchStationModel;
import de.unisaarland.UniApp.bus.uihelper.BusStationsAdapter;
import de.unisaarland.UniApp.bus.uihelper.SearchStationAdapter;
import de.unisaarland.UniApp.database.DatabaseHandler;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/1/13
 * Time: 11:38 PM
 * To change this template use File | Settings | File Templates.
 */

/*
* It implements Location listeners to show the distance of the bus stop from users current location.
* */
public class BusActivity extends UpNavigationActionBarActivity implements ConnectionCallbacks,LocationListener,OnConnectionFailedListener {
    private static final int BUS_ID = 5;
    private ArrayList<PointOfInterest> busStationsArray = null;
    private Location currentLocation = null;
    private String provider = null;
    private ListView busStationsList = null;
    private BusStationsAdapter busStationAdapter = null;


    //////////////location will be updated after every 3 seconds//////////////
    private LocationClient locationClient;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(3000)         // 3 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    public void onPause() {
        super.onPause();
        if (locationClient != null) {
            locationClient.disconnect();
            locationClient = null;
        }
    }

    @Override
    protected void onStop() {
        if(busStationsArray != null){
            busStationsArray.clear();
        }
        busStationsArray = null;
        currentLocation = null;
        busStationsList = null;
        busStationAdapter = null;
        super.onStop();
    }

    /*
    * Will be called after onStart method and connect the location client to get the location updates
    * */
    @Override
    protected void onResume() {
        setUpLocationClientIfNeeded();
        locationClient.connect();
        super.onResume();
    }

    private void setUpLocationClientIfNeeded() {
        if (locationClient == null) {
            locationClient = new LocationClient(
                    this,
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }

    /*
    * Will be called when activity created first time e.g. from scratch
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_layout);
    }

    /*
    * Will be called after onCreate method
    * and here we will populate the list of bus stations which we are going to show.
    * */
    @Override
    protected void onStart() {
        busStationsArray = new ArrayList<PointOfInterest>();
        updateModel();
        super.onStart();
    }

    /*
    * retrieve list of bus stops from the database and sets them in busStationsArray.
    * */
    private void updateModel(){
        DatabaseHandler dbHandler = new DatabaseHandler(this);
        List<PointOfInterest> tempBusStations = dbHandler.getPOIsForCategoryWithID(BUS_ID);
        dbHandler.close();
        HashMap<String,String> tempHashMap = new HashMap<String, String>(5);
        for(PointOfInterest poi: tempBusStations){
            if(!tempHashMap.containsKey(poi.getTitle())){
                busStationsArray.add(poi);
                tempHashMap.put(poi.getTitle(),poi.getTitle());
            }
        }
        populateItems();
    }

    /*
    * call the adapter and sets the bus station names and the distance to each bus station from current location
    * in BusStationAdapter class.
    * */
    private void populateItems() {
        busStationsList = (ListView) findViewById(R.id.bus_stations_list_view);
        busStationAdapter = new BusStationsAdapter(this,busStationsArray,currentLocation,provider);
        busStationsList.setAdapter(busStationAdapter);

        /// for search stations
        ListView searchStationsList = (ListView) findViewById(R.id.search_list_view);
        ArrayList<SearchStationModel> searchStationArray = new ArrayList<SearchStationModel>();
        SearchStationModel searchStationModel = new SearchStationModel();
        searchStationModel.setName(getString(R.string.search_bus));
        searchStationModel.setURL("Bahn.de");
        searchStationArray.add(searchStationModel);
        searchStationsList.setAdapter(new SearchStationAdapter(this,searchStationArray));
    }

    ///////////////// call back methods of location client //////////////
    @Override
    public void onConnected(Bundle bundle) {
        locationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
    }

    @Override
    public void onDisconnected() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        // reset the adapter to show the updated distance list. It will be called after every 3 seconds.
        busStationAdapter.setCurrentLocation(currentLocation);
        busStationsList.invalidateViews();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}