package de.unisaarland.UniApp.restaurant.uihelper;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import de.unisaarland.UniApp.R;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/8/13
 * Time: 2:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class TabsFragment extends Fragment implements TabHost.OnTabChangeListener{

    private static final String TAG = "FragmentTabs";
    public static final String TAB_MENSA = "Mensa";
    public static final String TAB_MENSA_CAFE= "Mensa cafe";
    public static final String TAB_AUSLANDER = "Auslandercafe";
    public static final String TAB_JURISTEN = "Juristencafe";
    public static final String TAB_HEROES = "Heroes Cafe";

    private View root;
    private TabHost tabHost;
    private int currentTab;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.tabs_fragment, null);
        tabHost = (TabHost) root.findViewById(android.R.id.tabhost);
        setupTabs();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        tabHost.setOnTabChangedListener(this);
        tabHost.setCurrentTab(currentTab);
        // manually start loading stuff in the first tab
        updateTab(TAB_MENSA, R.id.mensa);
    }

    private void setupTabs() {
        tabHost.setup(); // important!
        tabHost.addTab(newTab(TAB_MENSA, R.string.mensa, R.id.mensa));
        tabHost.addTab(newTab(TAB_MENSA_CAFE, R.string.mensa_cafe, R.id.mensa_cafe));
        tabHost.addTab(newTab(TAB_AUSLANDER, R.string.auslander_cafe, R.id.auslander));
        tabHost.addTab(newTab(TAB_JURISTEN, R.string.juristen_cafe, R.id.juristen));
        tabHost.addTab(newTab(TAB_HEROES, R.string.heroes_cafe, R.id.heroes));
    }

    private TabHost.TabSpec newTab(String tag, int labelId, int tabContentId) {
        Log.d(TAG, "buildTab(): tag=" + tag);

        View indicator = LayoutInflater.from(getActivity()).inflate(R.layout.tab,(ViewGroup) root.findViewById(android.R.id.tabs), false);
        ((TextView) indicator.findViewById(R.id.text)).setText(labelId);

        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabContentId);
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId) {
        if (TAB_MENSA.equals(tabId)) {
            updateTab(tabId, R.id.mensa);
            currentTab = 0;
        }else if (TAB_MENSA_CAFE.equals(tabId)) {
            updateTab(tabId, R.id.mensa_cafe);
            currentTab = 1;
        } else if(TAB_AUSLANDER.equals(tabId)){
            updateTab(tabId, R.id.auslander);
            currentTab = 2;
        } else if(TAB_JURISTEN.equals(tabId)){
            updateTab(tabId, R.id.juristen);
            currentTab = 3;
        }  else if (TAB_HEROES.equals(tabId)){
            updateTab(tabId,R.id.heroes);
            currentTab = 4;
        }
    }

    private void updateTab(String tabId, int placeholder) {
        FragmentManager fm = getFragmentManager();
        if(TAB_MENSA.equals(tabId)){
            if (fm.findFragmentByTag(tabId) == null) {
                fm.beginTransaction()
                        .replace(placeholder, new MensaFragment(), tabId)
                        .commit();
            }
        } else if(TAB_MENSA_CAFE.equals(tabId)){
            if (fm.findFragmentByTag(tabId) == null) {
                fm.beginTransaction()
                        .replace(placeholder, new MensaCafeFragment(), tabId)
                        .commit();
            }
        } else if (TAB_AUSLANDER.equals(tabId)){
            if (fm.findFragmentByTag(tabId) == null) {
                fm.beginTransaction()
                        .replace(placeholder, new AuslanderCafeFragment(), tabId)
                        .commit();
            }
        } else if (TAB_JURISTEN.equals(tabId)){
            if (fm.findFragmentByTag(tabId) == null) {
                fm.beginTransaction()
                        .replace(placeholder, new JuristenCafeFragment(), tabId)
                        .commit();
            }
        } else if (TAB_HEROES.equals(tabId)){
            if (fm.findFragmentByTag(tabId) == null) {
                fm.beginTransaction()
                        .replace(placeholder, new HeroesCafeFragment(), tabId)
                        .commit();
            }
        }
    }
}
