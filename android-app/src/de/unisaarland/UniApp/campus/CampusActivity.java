package de.unisaarland.UniApp.campus;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.BusDetailActivity;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.campus.model.CustomMapTileProvider;
import de.unisaarland.UniApp.campus.uihelper.CustomInfoWindowAdapter;
import de.unisaarland.UniApp.campus.uihelper.SearchAdapter;
import de.unisaarland.UniApp.database.DatabaseHandler;
import de.unisaarland.UniApp.restaurant.RestaurantActivity;
import de.unisaarland.UniApp.utils.UpNavigationActionBarActivity;

/*
* It implements Location listeners to show the distance of the bus stop from users current location.
* */
public class CampusActivity extends UpNavigationActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener, OnMarkerClickListener, OnInfoWindowClickListener {

    private static final String TAG = CampusActivity.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient locationClient;
    private static final int REQUEST_CODE = 5;
    private static final int REQUEST_CODE_LOCATION = 2;
    private final Map<Marker, PointOfInterest> poisMap = new HashMap<>();
    private Location currentLocation;
    private static final int TIME_INTERVAL = 3000; // 3 seconds

    //////////////location will be updated after every 3 seconds//////////////
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(TIME_INTERVAL)
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private Menu menu;
    private DatabaseHandler db = null;
    private boolean askedForLocationPermission = false;

    /**
     * Will be called when activity created first time e.g. from scratch and check if intent has any extra information
     * extra information will be set if activity is called from search staff page.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_layout);
        db = new DatabaseHandler(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
     * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpLocationClient();
    }

    @Override
    protected void onPause() {
        if (locationClient != null) {
            locationClient.disconnect();
            locationClient = null;
        }
        super.onPause();
    }


    public void searchItemSelected(PointOfInterest model) {
        for (Marker m : poisMap.keySet())
            m.remove();
        poisMap.clear();
        pinPOIsInArray(Collections.singletonList(model));
        final SearchView search = (SearchView) menu.findItem(R.id.activity_search).getActionView();
        search.setQuery("", false);
        search.setIconified(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
    }

    // return false as i want to open the marker from default implementation i haven't done any specific
    @Override
    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
        return false;
    }

    /*
    * on clicking the info button it will check if more then 1 action available then it will open a dialog
    * otherwise it will just open the chooser activity for the route
    * and it also has extra check in case of mensa it will open the mensa page.
    * */
    @Override
    public void onInfoWindowClick(final Marker marker) {
        final PointOfInterest p = poisMap.get(marker);
        if (p.isCanShowRightCallOut() == 1 && p.getWebsite() != null && p.getWebsite().length() > 0) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(CampusActivity.this);
            builder1.setTitle(getString(R.string.action));
            builder1.setMessage(getString(R.string.web_or_route));
            builder1.setCancelable(true);

            builder1.setPositiveButton(getString(R.string.website),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (p.isCanShowRightCallOut() == 1 && p.getWebsite().length() > 0) {
                                if ((marker.getTitle().equals("Mensa") && marker.getSnippet().equals("Restaurant")) ||
                                        marker.getTitle().equals("Mensacafé")) {
                                    Intent myIntent = new Intent(CampusActivity.this, RestaurantActivity.class);
                                    CampusActivity.this.startActivity(myIntent);

                                } else {
                                    Intent myIntent = new Intent(CampusActivity.this, BusDetailActivity.class);
                                    myIntent.putExtra("url", p.getWebsite());
                                    myIntent.putExtra("back", getString(R.string.campusText));
                                    CampusActivity.this.startActivity(myIntent);

                                }
                            }
                            dialog.cancel();
                        }
                    });
            builder1.setNegativeButton(getString(R.string.route),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            showRouteIfAvailable(p);
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        } else {
            showRouteIfAvailable(p);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // get map object and set default parameters e.g mapType and camera position
    // also set the panel listener for changing map type and remove all pins
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map != null)
            return;

        if (!isNetworkAvailable()) {
            new AlertDialog.Builder(CampusActivity.this)
                    .setTitle("Info")
                    .setMessage("Map could not be displayed. Please check your network connection and try again.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        // Try to obtain the map from the SupportMapFragment.
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mapInitialized(googleMap);
                    }
                });

    }

    private void mapInitialized(GoogleMap map) {
        this.map = map;

        poisMap.clear();
        // set default options of a map which are loaded with the activity
        // like default zoom level and campera position
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setBuildingsEnabled(false);
        map.setMapType(1);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter(this, poisMap));
        map.setOnInfoWindowClickListener(this);
        for (CustomMapTileProvider prov : CustomMapTileProvider.allTileProviders(getResources().getAssets()))
            map.addTileOverlay(new TileOverlayOptions().tileProvider(prov));

        resetCamera();

        // if info building != null means activity is called from search result details page
        // so it will get the building position from the database and will set the marker there.

        final String infoBuilding = getIntent().getStringExtra("building");
        if (infoBuilding != null) {
            List<PointOfInterest> pois = db.getPointsOfInterestForTitle(infoBuilding);
            if (!pois.isEmpty())
                pinPOIsInArray(pois);
        }
    }

    /**
     * It will show the route in external application from current position to destination
     * if current location is set e.g. using wifi, mobile network or gps otherwise it will display
     * message that please enable at least one location service and will open the settings page on Clicking
     */
    private void showRouteIfAvailable(PointOfInterest p) {
        if (currentLocation != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=" + currentLocation.getLatitude() +
                            "," + currentLocation.getLongitude() + "&daddr=" +
                            p.getLatitude() + "," + p.getLongitude()));
            startActivity(intent);
        } else {
            new AlertDialog.Builder(CampusActivity.this)
                    .setTitle(R.string.current_location_not_set)
                    .setMessage(R.string.enable_location_option)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(viewIntent);
                        }
                    })
                    .create().show();
        }
    }

    // setup location listener
    private GoogleApiClient setUpLocationClient() {
        if (locationClient == null) {
            locationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
            locationClient.connect();
        }
        return locationClient;
    }

    //Creation Custom Actionbar
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.campus_search_activity, menu);
        //Setting up the search widget
        this.menu = menu;

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.activity_search));
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setSuggestionsAdapter(new SearchAdapter(this, db.getAllData(), this));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                loadData(query);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                loadData(query);
                return true;
            }
        });
        return true;
    }


    private void loadData(String query) {
        //When the user input changes, the search results have to be adjusted
        Cursor cursor = db.getCursorPointsOfInterestPartialMatchedForSearchKey(query);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView search = (SearchView) menu.findItem(R.id.activity_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setSuggestionsAdapter(new SearchAdapter(this, cursor, this));
    }

    // Handling the Action Bar Buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_categories:
                Intent myIntent = new Intent(CampusActivity.this, CampusSearchActivity.class);
                CampusActivity.this.startActivityForResult(myIntent, REQUEST_CODE);

                return true;
            case R.id.action_settings:
                showOptions();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && resultCode == RESULT_OK && data.getExtras() != null) {
            @SuppressWarnings("unchecked")
            List<PointOfInterest> pois = (List<PointOfInterest>) data.getExtras().get("pois");
            pinPOIsInArray(pois);
        }
    }

    /**
     * add pins to all pointOfInterests in the list and will compute the visible rectangle so that
     * all point of interests are displayed on the screen.
     */
    private boolean pinPOIsInArray(List<PointOfInterest> POIs) {
        if (POIs.isEmpty()) {
            // use exception to get stack trace
            Log.w(TAG, new IllegalStateException("empty POI list"));
            return false;
        }
        // map is null if Google Play services are not installed on the device
        if (map == null) {
            Log.w(TAG, new IllegalStateException("map not initiated"));
            return false;
        }

        for (PointOfInterest poi : POIs) {
            int tempColor = poi.getColor();
            float color = tempColor == 1 ? BitmapDescriptorFactory.HUE_CYAN
                    : tempColor == 2 ? BitmapDescriptorFactory.HUE_GREEN
                    : BitmapDescriptorFactory.HUE_RED;

            Marker m = map.addMarker(new MarkerOptions()
                    .position(new LatLng(poi.getLatitude(), poi.getLongitude()))
                    .title(poi.getTitle())
                    .snippet(poi.getSubtitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
            poisMap.put(m, poi);
        }

        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (Marker m : poisMap.keySet())
            builder.include(m.getPosition());

        LatLngBounds bounds = builder.build();
        bounds = increaseBoundsByFactor(bounds, .2);
        bounds = adjustBoundsForMaxZoomLevel(bounds);

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));

        return true;
    }

    private LatLngBounds increaseBoundsByFactor(LatLngBounds bounds, double factor) {
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        LatLng newSw = new LatLng(sw.latitude + .5 * factor * (sw.latitude - ne.latitude),
                sw.longitude + .5 * factor * (sw.longitude - ne.longitude));
        LatLng newNe = new LatLng(ne.latitude + .5 * factor * (ne.latitude - sw.latitude),
                ne.longitude + .5 * factor * (ne.longitude - sw.longitude));
        return new LatLngBounds(newSw, newNe);
    }

    // will adjust the bounds to include markers
    private LatLngBounds adjustBoundsForMaxZoomLevel(LatLngBounds bounds) {
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double deltaLat = Math.abs(sw.latitude - ne.latitude);
        double deltaLon = Math.abs(sw.longitude - ne.longitude);

        final double zoomN = 0.002; // minimum zoom coefficient
        if (deltaLat < zoomN) {
            sw = new LatLng(sw.latitude - (zoomN - deltaLat / 2), sw.longitude);
            ne = new LatLng(ne.latitude + (zoomN - deltaLat / 2), ne.longitude);
            bounds = new LatLngBounds(sw, ne);
        } else if (deltaLon < zoomN) {
            sw = new LatLng(sw.latitude, sw.longitude - (zoomN - deltaLon / 2));
            ne = new LatLng(ne.latitude, ne.longitude + (zoomN - deltaLon / 2));
            bounds = new LatLngBounds(sw, ne);
        }

        return bounds;
    }

    @Override
    public void onConnected(Bundle bundle) {
        initLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        initLocationUpdates();
    }

    private boolean checkLocationUpdatesGranted() {
        boolean granted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;


        if (!granted && !askedForLocationPermission) {
            // Request missing location permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
            this.askedForLocationPermission = true;
        }

        return granted;
    }

    private void initLocationUpdates() {
        if (!checkLocationUpdatesGranted())
            return;

        LocationServices.FusedLocationApi.requestLocationUpdates(
                setUpLocationClient(),
                REQUEST,
                this);  // LocationListener
        if (map != null)
            map.setMyLocationEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        /* nop */
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private void showOptions() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.map_options_layout, null);
        final Dialog optionMenuDialog = new Dialog(this);
        optionMenuDialog.setTitle(R.string.settings);
        WindowManager.LayoutParams lp = optionMenuDialog.getWindow().getAttributes();
        lp.dimAmount = 0.7f;
        optionMenuDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        optionMenuDialog.setContentView(view);

        TextView satellite = (TextView) view.findViewById(R.id.satellite);
        satellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                optionMenuDialog.dismiss();
            }
        });
        TextView standard = (TextView) view.findViewById(R.id.normal);
        standard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                optionMenuDialog.dismiss();
            }
        });

        TextView hybrid = (TextView) view.findViewById(R.id.hybrid);
        hybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                optionMenuDialog.dismiss();
            }
        });

        TextView pins = (TextView) view.findViewById(R.id.remove);
        pins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllMarkers();
                resetCamera();
                optionMenuDialog.dismiss();
            }
        });
        optionMenuDialog.show();
    }

    private void removeAllMarkers() {
        for (Marker m : poisMap.keySet())
            m.remove();
        poisMap.clear();
    }

    private void resetCamera() {
        if (map == null)
            return;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String campus = settings.getString(getString(R.string.pref_campus), null);
        assert campus != null;
        LatLng latlng = campus.equals(getString(R.string.pref_campus_saar))
                ? new LatLng(49.25419, 7.041324)
                : new LatLng(49.305582, 7.344296);
        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(latlng, 15);
        map.moveCamera(upd);
    }

}
