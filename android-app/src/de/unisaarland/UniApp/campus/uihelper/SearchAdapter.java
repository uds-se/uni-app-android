package de.unisaarland.UniApp.campus.uihelper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.campus.CampusActivity;
import de.unisaarland.UniApp.database.DatabaseHandler;


public class SearchAdapter extends android.support.v4.widget.CursorAdapter {
    private TextView itemTitle;
    private TextView itemDescription;
    private ImageView categoryIcon;
    private Map<View, Integer> itemsMap = new HashMap<View,Integer>();
    private final CampusActivity parent;

    private final CategoryIconCache catIconCache;


    public SearchAdapter(Context context, Cursor cursor, CampusActivity parent) {
        super(context, cursor, false);
        this.parent = parent;
        this.catIconCache = new CategoryIconCache(context.getAssets());
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        itemTitle = (TextView) view.findViewById(R.id.title);
        itemDescription = (TextView) view.findViewById(R.id.description);
        categoryIcon = (ImageView) view.findViewById(R.id.category_icon);
        String title = cursor.getString(1);
        String subtitle = cursor.getString(2);
        int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categorieID"));
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
        itemTitle.setText(title);
        itemDescription.setText(subtitle);
        categoryIcon.setBackgroundDrawable(catIconCache.getIconForCategory(categoryId));
        view.setOnClickListener(clickListener);
        itemsMap.put(view, id);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer id = itemsMap.get(v);
            if (id == null)
                return;
            DatabaseHandler db = new DatabaseHandler(parent);
            PointOfInterest model = db.getPointsOfInterestForIDs(Arrays.asList(id)).get(0);
            db.close();
            if (model != null)
                 parent.searchItemSelected(model);
        }
    };

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.campus_search_row_layout, parent, false);
        return view;
    }
}
