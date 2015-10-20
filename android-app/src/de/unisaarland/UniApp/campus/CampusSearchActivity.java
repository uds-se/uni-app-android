package de.unisaarland.UniApp.campus;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.campus.uihelper.CategoryIconCache;
import de.unisaarland.UniApp.database.DatabaseHandler;

public class CampusSearchActivity extends ActionBarActivity {

    private static final String TAG = CampusActivity.class.getSimpleName();

    /**
     * Will be called when activity created first time e.g. from scratch and will get list of all
     * Point of Interests from database and will call populate to show the list in adapter
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_search_layout);

        ActionBar actionBar = getSupportActionBar();
        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.categories);

        DatabaseHandler db = new DatabaseHandler(this);
        List<Pair<String, Integer>> categories = db.getAllCategories();
        db.close();
        ListView categoriesList = (ListView) findViewById(R.id.categoriesList);
        categoriesList.setAdapter(new CampusCategoriesAdapter(this, categories));
    }

    private class CampusCategoriesAdapter extends BaseAdapter {

        private final Context context;
        private final List<Pair<String, Integer>> categories;
        private CategoryIconCache catIconCache;

        public CampusCategoriesAdapter(Context context, List<Pair<String, Integer>> categories) {
            this.context = context;
            this.categories = categories;
            this.catIconCache = new CategoryIconCache(getAssets());
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Object getItem(int position) {
            return categories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        // set categories view i.e buildings, restaurants etc
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(context, R.layout.campus_categories_row, null);

            TextView categoryTitle = (TextView) convertView.findViewById(R.id.category_title);
            categoryTitle.setText(categories.get(position).first);
            categoryTitle.setVisibility(View.VISIBLE);

            ImageView categoryIcon = (ImageView) convertView.findViewById(R.id.category_icon);
            int catId = categories.get(position).second;
            categoryIcon.setBackground(catIconCache.getIconForCategory(catId));

            convertView.setOnClickListener(categoryClickListener);
            convertView.setTag(R.id.campus_search_categorie_tag, categories.get(position));
            return convertView;
        }

        private final View.OnClickListener categoryClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<String, Integer> cat = (Pair<String, Integer>) v.getTag(R.id.campus_search_categorie_tag);

                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.campus_poi_layout,null);

                DatabaseHandler db = new DatabaseHandler(context);
                final List<PointOfInterest> pois = db.getPOIsForCategoryWithID(cat.second);
                Collections.sort(pois);

                TextView pinAll = (TextView) view.findViewById(R.id.bt_pin_all);
                if (cat.second == 1) {
                    pinAll.setVisibility(View.INVISIBLE);
                } else {
                    pinAll.setVisibility(View.VISIBLE);
                    pinAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            returnPOIs(pois);
                        }
                    });
                }

                ListView poisList = (ListView) view.findViewById(R.id.poisList);
                poisList.setAdapter(new CategoriePOIsAdapter(context, pois));

                final Dialog optionMenuDialog = new Dialog(context);
                optionMenuDialog.setContentView(view);
                optionMenuDialog.setTitle(cat.first);
                optionMenuDialog.setOnKeyListener(menuOnKeyListener);
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = (int) Math.round(size.x*0.9);
                int height = (int) Math.round(size.y*0.9);
                optionMenuDialog.getWindow().setLayout(width, height);
                WindowManager.LayoutParams lp = optionMenuDialog.getWindow().getAttributes();
                lp.dimAmount = 0.7f;
                optionMenuDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                optionMenuDialog.show();
            }
        };

        public void update(List<String> categoryTitles, List<Integer> categoryIds) {
        }
    }

    void returnPOIs(List<PointOfInterest> pois) {
        Intent data = new Intent();
        data.putExtra("pois", (Serializable) pois);
        setResult(RESULT_OK, data);
        finish();
    }

    private final DialogInterface.OnKeyListener menuOnKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                dialog.dismiss();
            }
            return true;
        }
    };

    private class CategoriePOIsAdapter extends BaseAdapter{

        private final Context context;
        private final List<PointOfInterest> pois;

        public CategoriePOIsAdapter(Context context, List<PointOfInterest> pois) {
            this.context = context;
            this.pois = pois;
        }

        @Override
        public int getCount() {
            return pois.size();
        }

        @Override
        public Object getItem(int position) {
            return pois.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(context, R.layout.poi_detailed_row, null);

            PointOfInterest poi = pois.get(position);
            TextView categoryTitle = (TextView) convertView.findViewById(R.id.poi_title);
            categoryTitle.setText(poi.getTitle());
            categoryTitle.setVisibility(View.VISIBLE);

            convertView.setOnClickListener(poiClickListener);
            convertView.setTag(R.id.campus_search_poi_tag, poi);
            return convertView;
        }
    }

    private View.OnClickListener poiClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PointOfInterest poi = (PointOfInterest) v.getTag(R.id.campus_search_poi_tag);
            returnPOIs(Collections.singletonList(poi));
        }
    };
}
