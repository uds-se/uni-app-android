package de.unisaarland.UniApp.bus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

/*
* It implements Location listeners to show the distance of the bus stop from users current location.
* */
public class BusActivity extends UpNavigationActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks, LocationListener {
    private static final int BUS_ID = 5;
    private static final int REQUEST_CODE_LOCATION = 2;
    private List<PointOfInterest> busStationsArray = null;
    private Location currentLocation = null;
    private String provider = null;
    private ListView busStationsList = null;
    private BusStationsAdapter busStationAdapter = null;
    private boolean permDenied = false;


    //////////////location will be updated after every 3 seconds//////////////
    private GoogleApiClient locationClient;
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
            locationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
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
        busStationsArray = new ArrayList<>();
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
        HashMap<String,String> tempHashMap = new HashMap<>(5);
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
        ArrayList<SearchStationModel> searchStationArray = new ArrayList<>();
        SearchStationModel searchStationModel = new SearchStationModel();
        searchStationModel.setName(getString(R.string.search_bus));
        searchStationModel.setURL("Bahn.de");
        searchStationArray.add(searchStationModel);
        searchStationsList.setAdapter(new SearchStationAdapter(this, searchStationArray));
    }

    ///////////////// call back methods of location client //////////////


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void onConnected(Bundle bundle) {

        // added check for dynamic permissions in API v23
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request missing location permission.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //call the request permission here
                if (permDenied && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    return;
                }
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            // Location permission has been granted, continue as usual.
            if (locationClient != null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        locationClient,
                        REQUEST,
                        this);  // LocationListener
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success!
                if(locationClient != null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            locationClient,
                            REQUEST,
                            this);  // LocationListener
                }
            } else {
                // Permission was denied or request was cancelled
                permDenied = true;
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        /* nop */
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        // reset the adapter to show the updated distance list. It will be called after every 3 seconds.
        busStationAdapter.setCurrentLocation(currentLocation);
        busStationsList.invalidateViews();
    }
}