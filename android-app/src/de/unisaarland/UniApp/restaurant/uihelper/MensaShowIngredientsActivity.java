package de.unisaarland.UniApp.restaurant.uihelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.unisaarland.UniApp.R;

public class MensaShowIngredientsActivity extends Activity {

    public static final String ACTION = "de.unisaarland.UniApp.restaurant.showIngredients";

    /**
     * Encode an array of labels to be used as data for the intent.
     */
    public static Uri encodeLabels(String[] labels) {
        Uri.Builder ub = new Uri.Builder();
        for (String s : labels)
            ub.appendPath(s);
        return ub.build();
    }

    public static String[] decodeLabels(Uri uri) {
        List<String> labelsList = uri.getPathSegments();
        return labelsList.toArray(new String[labelsList.size()]);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.label_list);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] labels = decodeLabels(getIntent().getData());

        ListView list = (ListView) findViewById(R.id.label_list);
        list.setAdapter(new LabelAdapter(this, labels));
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.7f;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private static class LabelAdapter extends BaseAdapter {

        private final Context context;
        private final String[] labels;


        public LabelAdapter(Context context, String[] labels) {
            this.context = context;
            this.labels = labels;
        }

        @Override
        public int getCount() {
            return labels.length;
        }

        @Override
        public Object getItem(int position) {
            return labels[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        // sets the view of news item row
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(context, R.layout.label_item, null);
            String label = labels[position];
            TextView labelView = (TextView) convertView.findViewById(R.id.mensa_label_id);
            labelView.setText(capitalize(label));
            TextView descView = (TextView) convertView.findViewById(R.id.mensa_label_desc);
            int descId = context.getResources().getIdentifier("label_" + label, "string", context.getPackageName());
            if (descId == 0) {
                descView.setText(R.string.no_description);
                descView.setTypeface(descView.getTypeface(), Typeface.ITALIC);
            } else {
                descView.setText(descId);
                // getting rid of previously set typeface is not so easy:
                // http://stackoverflow.com/questions/6200533/set-textview-style-bold-or-italic
                descView.setTypeface(Typeface.create(descView.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
            }
            return convertView;
        }

        private static String capitalize(String label) {
            if (label.isEmpty())
                return label;
            char firstChar = label.charAt(0);
            String rest = label.substring(1);
            if (Character.toUpperCase(firstChar) == firstChar && rest.toLowerCase().equals(rest))
                return label;
            return label.substring(0, 1).toUpperCase() + label.substring(1);
        }
    }
}
