package de.unisaarland.UniApp.campus.uihelper;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;

import java.util.HashMap;

/**
 * Created by Shahzad on 1/6/14.
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private final HashMap<String, PointOfInterest> poisMap;

    public CustomInfoWindowAdapter(Activity activity, HashMap<String, PointOfInterest> poisMap) {
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
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        titleUi.setText(title);
        final PointOfInterest p = poisMap.get(title);
        ImageButton linkButton = (ImageButton)view.findViewById(R.id.web_button);
        if(p.isCanShowRightCallOut() == 1 && p.getWebsite().length()>0){

        }else{
            linkButton.setVisibility(View.INVISIBLE);
        }

        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        snippetUi.setText(snippet);
    }
}
