package de.unisaarland.UniApp.campus.uihelper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.database.DatabaseHandler;


public class DetailedPOIView {
    private final ArrayList<Integer> result;
    private final int categoryId;
    private final Context context;
    private final ListView detailedList;
    private String title = null;
    private ArrayList<PointOfInterest> pois ;
    private HashMap<View,PointOfInterest> poisMap = new HashMap<View, PointOfInterest>();
    private Button backIconButton;

    public DetailedPOIView(View view,Context context, ArrayList<Integer> result,String title,int categoryId) {
        this.context = context;
        this.result = result;
        this.title = title;
        this.categoryId = categoryId;
        DatabaseHandler db = new DatabaseHandler(context);
        pois = db.getPointsOfInterestForCategoryWithID(this.categoryId);
        Collections.sort(pois);

        backIconButton = (Button) view.findViewById(R.id.bt_back);
        TextView pinAll = (TextView) view.findViewById(R.id.bt_pin_all);
        if(categoryId == 1){
            pinAll.setVisibility(View.INVISIBLE);
        }else{
            pinAll.setVisibility(View.VISIBLE);
            pinAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(PointOfInterest p: pois){
                        DetailedPOIView.this.result.add(p.getID());
                    }
                    backIconButton.performClick();
                }
            });
        }

        MyAdapter adapter = new MyAdapter();
        detailedList = (ListView) view.findViewById(R.id.categoriesList);
        detailedList.setAdapter(adapter);

    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return pois.size();
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
                convertView = View.inflate(context, R.layout.poi_detailed_row, null);
            }
            PointOfInterest model = pois.get(position);
            TextView categoryTitle = (TextView) convertView.findViewById(R.id.poi_title);
            categoryTitle.setText(model.getTitle());
            categoryTitle.setVisibility(View.VISIBLE);


            convertView.setOnClickListener(clickListener);
            poisMap.put(convertView, model);
            return convertView;
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(poisMap.containsKey(v)){
                    PointOfInterest poi = poisMap.get(v);
                    result.add(poi.getID());
                    backIconButton.performClick();
                }
            }
        };
    }
}