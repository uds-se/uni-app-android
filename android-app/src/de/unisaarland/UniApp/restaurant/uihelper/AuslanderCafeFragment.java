package de.unisaarland.UniApp.restaurant.uihelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.unisaarland.UniApp.R;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/8/13
 * Time: 2:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuslanderCafeFragment extends Fragment {

    public AuslanderCafeFragment() {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.auslander_cafe_view, null);
        return root;
    }
}
