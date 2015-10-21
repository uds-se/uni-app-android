package de.unisaarland.UniApp.restaurant.uihelper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaItem;


public class RestaurantAdapter extends BaseAdapter {
    private final Context context;
    private List<MensaItem> mensaItems;

    public RestaurantAdapter(Context context, List<MensaItem> mensaItems) {
        this.context = context;
        this.mensaItems = mensaItems;
    }

    @Override
    public int getCount() {
        return mensaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mensaItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.mensa_item, null);
        }
        MensaItem model = mensaItems.get(position);

        TextView mealCategory = (TextView) convertView.findViewById(R.id.mensa_menu_category);
        mealCategory.setText(model.getCategory());

        TextView mealTitle = (TextView) convertView.findViewById(R.id.mensa_menu_title);
        mealTitle.setText(createMensaItemSpannable(model.getTitle()));

        TextView mealDescription = (TextView) convertView.findViewById(R.id.mensa_menu_description);
        mealDescription.setText(createMensaItemSpannable(model.getDesc()));

        ImageView info = (ImageView) convertView.findViewById(R.id.info);
        String[] labels = model.getLabels();
        if (labels != null && labels.length != 0) {
            info.setOnClickListener(new LabelsClickListener(labels, context));
            info.setVisibility(View.VISIBLE);
        } else {
            info.setVisibility(View.GONE);
        }

        RelativeLayout descriptionBackgroundColor = (RelativeLayout) convertView.findViewById(R.id.contentBackground);
        descriptionBackgroundColor.setBackgroundColor(model.getColor());

        TextView mealPrice = (TextView) convertView.findViewById(R.id.mensa_menu_price);
        if (model.getPreis1() != 0) {
            String text = context.getString(R.string.mensaPriceFormat,
                    .01 * model.getPreis1(), .01 * model.getPreis2(), .01 * model.getPreis3());
            mealPrice.setText(text);
            mealPrice.setVisibility(View.VISIBLE);
        } else {
            mealPrice.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    /**
     * Returns a SpannableString for the given text. All occurences of (A,B,C) are put in
     * superscript with the parentheses removed.
     */
    private SpannableString createMensaItemSpannable(String desc) {
        // even entries are normal text, odd entries are superscript
        List<String> substrs = new ArrayList<>();
        int pos = 0;
        int added = 0;
        while (pos < desc.length()) {
            int openParen = desc.indexOf('(', pos);
            int closeParen = desc.indexOf(')', openParen+1);
            if (openParen == -1 || closeParen == -1)
                break;
            boolean valid = true;
            String[] parts = desc.substring(openParen+1, closeParen).split(",");
            for (String part : parts)
                if (part.trim().length() > 2)
                    valid = false;
            if (!valid) {
                pos = closeParen + 1;
                continue;
            }
            int firstStrEnd = openParen;
            if (firstStrEnd > added && desc.charAt(firstStrEnd-1) == ' ')
                --firstStrEnd;
            substrs.add(desc.substring(added, firstStrEnd));
            substrs.add(desc.substring(openParen+1, closeParen));
            added = pos = closeParen+1;
        }
        if (added < desc.length())
            substrs.add(desc.substring(added));

        StringBuilder sb = new StringBuilder();
        for (String s : substrs)
            sb.append(s);
        SpannableString str = new SpannableString(sb.toString());
        pos = 0;
        for (int i = 1; i < substrs.size(); i += 2) {
            pos += substrs.get(i-1).length();
            str.setSpan(new SuperscriptSpan(), pos, pos+substrs.get(i).length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            str.setSpan(new RelativeSizeSpan(0.8f), pos, pos+substrs.get(i).length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            pos += substrs.get(i).length();
        }
        return str;
    }

    public boolean update(List<MensaItem> items) {
        if (this.mensaItems.equals(items))
            return false;
        this.mensaItems = items;
        this.notifyDataSetChanged();
        return true;
    }


    private static class LabelsClickListener implements View.OnClickListener {

        private final String[] labels;
        private final Context context;

        public LabelsClickListener(String[] labels, Context context) {
            this.labels = labels;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.label_list, null);
            ListView list = (ListView) view.findViewById(R.id.label_list);
            list.setAdapter(new LabelAdapter(context, labels));
            final Dialog dialog = new Dialog(context, R.style.Transparent);
            dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR);
            dialog.setContentView(view);
            dialog.setTitle(view.getResources().getString(R.string.labels));
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.dimAmount = 0.7f;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
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
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.label_item, null);
            }
            String label = labels[position];
            TextView labelView = (TextView) convertView.findViewById(R.id.mensa_label_id);
            labelView.setText(label);
            TextView descView = (TextView) convertView.findViewById(R.id.mensa_label_desc);
            int descId = context.getResources().getIdentifier("label_" + label, "string", context.getPackageName());
            if (descId == 0) {
                descView.setText(R.string.no_description);
                descView.setTypeface(descView.getTypeface(), Typeface.ITALIC);
            } else {
                descView.setText(descId);
                descView.setTypeface(descView.getTypeface(), Typeface.NORMAL);
            }
            return convertView;
        }
    }
}