package de.unisaarland.UniApp.campus.uihelper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.database.DatabaseHandler;

import java.io.IOException;
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
    private final CampusActivity parent;
    private ArrayList<PointOfInterest> searchBase;
    private final Context context;
    private HashMap<View, Integer> itemsMap;

    public SearchViewAdapter(ArrayList<PointOfInterest> searchBase, Context context, int simple_dropdown_item_1line, CampusActivity parent) {
        super(context,simple_dropdown_item_1line,searchBase);
        this.context = context;
        this.searchBase = searchBase;
        itemsMap = new HashMap<View, Integer>();
        this.parent = parent;
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
            convertView = View.inflate(context, R.layout.campus_search_row_layout, null);
        }
        convertView.setClickable(true);
        TextView itemTitle = (TextView) convertView.findViewById(R.id.title);
        TextView itemDescription = (TextView) convertView.findViewById(R.id.description);
        PointOfInterest po = searchBase.get(position);
        if(po!=null){
            itemTitle.setText(po.getTitle());
            itemTitle.setVisibility(View.VISIBLE);
            itemDescription.setText(po.getSubtitle());
            itemDescription.setVisibility(View.VISIBLE);
            ImageView categoryIcon = (ImageView) convertView.findViewById(R.id.category_icon);
            try {
                Drawable d = Drawable.createFromStream(context.getAssets().open("cat" + po.getCategoryID() + ".png"), null);
                categoryIcon.setBackground(d);
            } catch (IOException e) {
                e.printStackTrace();
            }

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
                parent.searchItemSelected(model);
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
