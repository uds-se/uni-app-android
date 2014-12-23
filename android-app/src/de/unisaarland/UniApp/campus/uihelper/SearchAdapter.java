package de.unisaarland.UniApp.campus.uihelper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.database.DatabaseHandler;

/**
 * Created by Janek on 13.08.2014.
 */
public class SearchAdapter extends android.support.v4.widget.CursorAdapter {
    private TextView itemTitle;
    private TextView itemDescription;
    ImageView categoryIcon;
    private HashMap<View, Integer> itemsMap;
    CampusActivity parent;


    public SearchAdapter(Context context, Cursor cursor,CampusActivity parent) {
        super(context, cursor, false);
        this.parent = parent;
        itemsMap = new HashMap<View,Integer>();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        itemTitle = (TextView) view.findViewById(R.id.title);
            itemDescription = (TextView) view.findViewById(R.id.description);
            categoryIcon = (ImageView) (ImageView) view.findViewById(R.id.category_icon);
            String title = cursor.getString(1);
            String subtitle = cursor.getString(2);
            String categoryid = cursor.getString(cursor.getColumnIndex("categorieID"));
            Integer id = cursor.getInt(cursor.getColumnIndex("ID"));
            itemTitle.setText(title);
            itemDescription.setText(subtitle);
            try {
                Drawable d = Drawable.createFromStream(context.getAssets().open("cat" + categoryid + ".png"), null);
                categoryIcon.setBackgroundDrawable(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
            view.setOnClickListener(clickListener);
            itemsMap.put(view, id);

    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(itemsMap.containsKey(v)){
                Integer id = itemsMap.get(v);
                ArrayList<Integer> point = new ArrayList<Integer>();
                point.add(id);
                DatabaseHandler db = new DatabaseHandler(parent);
                PointOfInterest model = db.getPointsOfInterestForIDs(point).get(0);
                db.close();
                if (model != null)
                     parent.searchItemSelected(model);
            }
        }
    };

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.campus_search_row_layout, parent, false);
        return view;
    }
}
