package de.unisaarland.UniApp.campus.uihelper;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private final Map<Marker, PointOfInterest> poisMap;

    public CustomInfoWindowAdapter(Activity activity, Map<Marker, PointOfInterest> poisMap) {
        mWindow = activity.getLayoutInflater().inflate(R.layout.custom_info_window, null);
        this.poisMap = poisMap;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(final Marker marker, View view) {
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        titleUi.setText(marker.getTitle());
        final PointOfInterest p = poisMap.get(marker);
        ImageButton linkButton = (ImageButton)view.findViewById(R.id.web_button);
        if (p.isCanShowRightCallOut() == 1 && p.getWebsite() != null && p.getWebsite().length()>0) {

        } else {
            linkButton.setVisibility(View.INVISIBLE);
        }

        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        snippetUi.setText(snippet);
    }
}
