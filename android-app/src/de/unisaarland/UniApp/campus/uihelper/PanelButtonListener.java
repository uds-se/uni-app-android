package de.unisaarland.UniApp.campus.uihelper;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.ArrayList;
import java.util.HashMap;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.campus.model.CustomMapTileProvider;
import de.unisaarland.UniApp.campus.model.CustomMapTileSupportProvider;

/**
 * Created by Shahzad on 1/6/14.
 */
public class PanelButtonListener implements View.OnClickListener{
    private final GoogleMap map;
    private final CampusActivity campusActivity;
    private final HashMap<String, PointOfInterest> poisMap;
    private final ArrayList<Marker> markers;
    // set the panel listener for changing map type and remove all pins
    public PanelButtonListener(CampusActivity campusActivity, GoogleMap map, HashMap<String, PointOfInterest> poisMap, ArrayList<Marker> markers){
        this.campusActivity = campusActivity;
        this.map = map;
        this.poisMap = poisMap;
        this.markers = markers;
    }
    @Override
    public void onClick(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) campusActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.map_options_layout, null);
        final Dialog optionMenuDialog = new Dialog(campusActivity);
        optionMenuDialog.setTitle(R.string.settings);
        Context context = (Context) campusActivity;
        final float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (200 * scale + 0.5f);
        int height = (int) (220 * scale + 0.5f);
        optionMenuDialog.getWindow().setLayout(width, height);
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
                int type = map.getMapType();
                if(markers != null){
                    markers.clear();
                }
                map.clear();
                map.setMyLocationEnabled(true);
                map.setMapType(type);
                map.setOnMyLocationButtonClickListener(campusActivity);
                map.setOnMarkerClickListener(campusActivity);
                map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        float maxZoom = 18.0f;
                        if (cameraPosition.zoom > maxZoom)
                            map.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
                    }
                });
                map.setBuildingsEnabled(false);
                map.getUiSettings().setZoomControlsEnabled(false);
                map.setInfoWindowAdapter(new CustomInfoWindowAdapter(campusActivity, poisMap));
                map.setOnInfoWindowClickListener(campusActivity);
                map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(campusActivity.getResources().getAssets())));
                map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileSupportProvider(campusActivity.getResources().getAssets())));
                CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(new LatLng(49.25419, 7.041324), 15);
                map.moveCamera(upd);
                optionMenuDialog.dismiss();
            }
        });
        optionMenuDialog.show();
    }
}
