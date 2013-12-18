package com.st.cs.unisaarland.SaarlandUniversityApp.campus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.campus.uihelper.DetailedPOIView;
import com.st.cs.unisaarland.SaarlandUniversityApp.database.DatabaseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/9/13
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class CampusSearchActivity extends Activity {
    ArrayList<Integer> categoryIds;
    ArrayList<String> categoryTitles;
    private ListView categoriesList;
    private CampusCategoriesAdapter campusCategoriesadapter;
    private Dialog optionMenuDialog;
    private ArrayList<Integer> result = new ArrayList<Integer>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_search_layout);
        setActionBar();
        DatabaseHandler db = new DatabaseHandler(this);
        categoryIds = db.getAllCategoryIDs();
        categoryTitles = db.getAllCategoryTitles();
        db.close();
        populateItems();
    }

    private void populateItems() {
        categoriesList = (ListView) findViewById(R.id.categoriesList);
        campusCategoriesadapter = new CampusCategoriesAdapter(this, categoryTitles, categoryIds);
        categoriesList.setAdapter(campusCategoriesadapter);
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.campusText);
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        ImageButton backButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.back_icon);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new BackButtonClickListener(this));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText("Categories");
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    }

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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.campus_categories_row, null);
            }

            TextView categoryTitle = (TextView) convertView.findViewById(R.id.category_title);
            categoryTitle.setText(titles.get(position));
            categoryTitle.setVisibility(View.VISIBLE);

            try {
                Drawable d = Drawable.createFromStream(context.getAssets().open("cat" + categoryIds.get(position) + ".png"), null);
                ImageView categoryIcon = (ImageView) convertView.findViewById(R.id.category_icon);
                categoryIcon.setBackground(d);

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
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
                    View view = layoutInflater.inflate(R.layout.campus_poi_layout, null);
                    DetailedPOIView poiView = new DetailedPOIView(view, CampusSearchActivity.this, result, title, categoryIds.get(index));

                    ImageButton backIconButton = (ImageButton) view.findViewById(R.id.back_icon);
                    TextView backText = (TextView) view.findViewById(R.id.backText);
                    backText.setOnClickListener(new DialogBackButtonClickListener());
                    backIconButton.setVisibility(View.VISIBLE);
                    backIconButton.setOnClickListener(new DialogBackButtonClickListener());

                    optionMenuDialog = new Dialog(context, R.style.Transparent);
                    optionMenuDialog.setContentView(view);
                    optionMenuDialog.setOnKeyListener(menuOnKeyListener);
                    optionMenuDialog.show();
                }
            }
        };
    }


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

        ;
    };
}