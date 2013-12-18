package com.st.cs.unisaarland.SaarlandUniversityApp.campus.uihelper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.bus.model.PointOfInterest;
import com.st.cs.unisaarland.SaarlandUniversityApp.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/9/13
 * Time: 4:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class DetailedPOIView {
    private final ArrayList<Integer> result;
    private final int categoryId;
    private final Context context;
    private final ListView detailedList;
    private String title = null;
    private ArrayList<PointOfInterest> pois ;
    private HashMap<View,PointOfInterest> poisMap = new HashMap<View, PointOfInterest>();
    private ImageButton backIconButton;

    public DetailedPOIView(View view,Context context, ArrayList<Integer> result,String title,int categoryId) {
        this.context = context;
        this.result = result;
        this.title = title;
        this.categoryId = categoryId;
        DatabaseHandler db = new DatabaseHandler(context);
        pois = db.getPointsOfInterestForCategoryWithID(this.categoryId);
        Collections.sort(pois);

        TextView heading = (TextView) view.findViewById(R.id.page_heading);
        heading.setText(title);

        backIconButton = (ImageButton) view.findViewById(R.id.back_icon);
        TextView pinAll = (TextView) view.findViewById(R.id.pin_all);
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