package com.st.cs.unisaarland.SaarlandUniversityApp.campus.uihelper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.bus.model.PointOfInterest;
import com.st.cs.unisaarland.SaarlandUniversityApp.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/16/13
 * Time: 1:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchViewAdapter extends ArrayAdapter<PointOfInterest>{
    private final AutoCompleteTextView search;
    private ArrayList<PointOfInterest> searchBase;
    private final Context context;
    private HashMap<View, Integer> itemsMap;

    public SearchViewAdapter(ArrayList<PointOfInterest> searchBase, Context context, int simple_dropdown_item_1line, AutoCompleteTextView search) {
        super(context,simple_dropdown_item_1line,searchBase);
        this.context = context;
        this.searchBase = searchBase;
        itemsMap = new HashMap<View, Integer>();
        this.search =search;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return searchBase.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.campus_categories_row, null);
        }
        convertView.setClickable(true);
        TextView categoryTitle = (TextView) convertView.findViewById(R.id.category_title);
        PointOfInterest po = searchBase.get(position);
        if(po!=null){
            categoryTitle.setText(po.getTitle());
            categoryTitle.setVisibility(View.VISIBLE);
            ImageView categoryIcon = (ImageView) convertView.findViewById(R.id.category_icon);
            categoryIcon.setBackgroundResource(R.drawable.app_icon);
            convertView.setOnClickListener(clickListener);
            itemsMap.put(convertView, position);
        }
        return convertView;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(itemsMap.containsKey(v)){
                int index = itemsMap.get(v);
                PointOfInterest model = searchBase.get(index);
                String url = "http://mobile.bahn.de/bin/mobil/query.exe/dox?country=DEU&rt=1&use_realtime_filter=1&webview=&searchMode=ADVANCED";
            }
        }
    };


    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = resultValue.toString();
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                DatabaseHandler db = new DatabaseHandler(context);
                ArrayList<PointOfInterest> suggestions = db.getPointsOfInterestPartialMatchedForSearchKey(constraint.toString());
                db.close();
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(results != null && results.count > 0) {
                itemsMap.clear();
                itemsMap = null;
                itemsMap = new HashMap<View, Integer>();
                searchBase = (ArrayList<PointOfInterest>)results.values;
                notifyDataSetChanged();
            }
        }
    };
}
