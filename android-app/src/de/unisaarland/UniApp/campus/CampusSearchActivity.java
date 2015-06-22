package de.unisaarland.UniApp.campus;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.campus.uihelper.CategoryIconCache;
import de.unisaarland.UniApp.campus.uihelper.DetailedPOIView;
import de.unisaarland.UniApp.database.DatabaseHandler;

public class CampusSearchActivity extends ActionBarActivity {

    private static final String TAG = CampusActivity.class.getSimpleName();

    private ArrayList<Integer> categoryIds;
    private ArrayList<String> categoryTitles;
    private ListView categoriesList;
    private CampusCategoriesAdapter campusCategoriesadapter;
    private Dialog optionMenuDialog;
    private ArrayList<Integer> result = new ArrayList<Integer>();

    private CategoryIconCache catIconCache;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    * Will be called when activity created first time e.g. from scratch and will get list of all
    * Point of Interests from database and will call populate to show the list in adapter
    * */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_search_layout);
        setActionBar();
        catIconCache = new CategoryIconCache(getAssets());
        DatabaseHandler db = new DatabaseHandler(this);
        categoryIds = db.getAllCategoryIDs();
        categoryTitles = db.getAllCategoryTitles();
        db.close();
        populateItems();
    }

    /*
    * populate the list of point of Interest categories
    * */
    private void populateItems() {
        categoriesList = (ListView) findViewById(R.id.categoriesList);
        campusCategoriesadapter = new CampusCategoriesAdapter(this, categoryTitles, categoryIds);
        categoriesList.setAdapter(campusCategoriesadapter);
    }
    // set custom navigation bar
    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();

        //Enabling Up-Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.categories);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // custom class to show the back button action using navigation bar and will call the onBack method of activity
    class BackButtonClickListener implements View.OnClickListener {
        final Activity activity;

        public BackButtonClickListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            activity.onBackPressed();
        }
    }

    class CampusCategoriesAdapter extends BaseAdapter {

        private final Context context;
        private final ArrayList<String> titles;
        private final ArrayList<Integer> categoryIds;
        private HashMap<View, Integer> categoriesMap;

        public CampusCategoriesAdapter(Context context, ArrayList<String> titles, ArrayList<Integer> categoryIds) {
            this.context = context;
            this.titles = titles;
            this.categoryIds = categoryIds;
            categoriesMap = new HashMap<View, Integer>(titles.size());
        }


        @Override
        public int getCount() {
            return titles.size();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getItem(int position) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getItemId(int position) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        // set categories view i.e buildings, restaurants etc
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(context, R.layout.campus_categories_row, null);

            TextView categoryTitle = (TextView) convertView.findViewById(R.id.category_title);
            categoryTitle.setText(titles.get(position));
            categoryTitle.setVisibility(View.VISIBLE);

            ImageView categoryIcon = (ImageView) convertView.findViewById(R.id.category_icon);
            int catId = categoryIds.get(position);
            categoryIcon.setBackgroundDrawable(catIconCache.getIconForCategory(catId));

            convertView.setOnClickListener(clickListener);
            categoriesMap.put(convertView, position);
            return convertView;
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (categoriesMap.containsKey(v)) {
                    int index = categoriesMap.get(v);
                    String title = titles.get(index);

                    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = layoutInflater.inflate(R.layout.campus_poi_layout,null);
                    DetailedPOIView poiView = new DetailedPOIView(view, CampusSearchActivity.this, result, title, categoryIds.get(index));

                    Button backIconButton = (Button) view.findViewById(R.id.bt_back);
                    backIconButton.setOnClickListener(new DialogBackButtonClickListener());

                    optionMenuDialog = new Dialog(context);
                    optionMenuDialog.setContentView(view);
                    optionMenuDialog.setTitle(title);
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
            }
        };
    }

    // set the result array in case if pinAll button or a single POI is selected.
    class DialogBackButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            optionMenuDialog.dismiss();
            if(result.size()>0){
                Intent data = new Intent();
                data.putExtra("idsArray", result);
                setResult(RESULT_OK, data);
                finish();
            }else{
//                setResult(RESULT_CANCELED);

            }
        }
    }

    private DialogInterface.OnKeyListener menuOnKeyListener = new DialogInterface.OnKeyListener() {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                dialog.dismiss();
            }
            return true;
        }

    };
}