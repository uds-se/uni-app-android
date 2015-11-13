package de.unisaarland.UniApp.restaurant.uihelper;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
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

import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaItem;


public class RestaurantAdapter extends BaseAdapter {
    private final Context context;
    private List<MensaItem> mensaItems;

    private final boolean showIngredients;

    public RestaurantAdapter(Context context, List<MensaItem> mensaItems) {
        if (context == null || mensaItems == null)
            throw new NullPointerException();
        this.context = context;
        this.mensaItems = mensaItems;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        this.showIngredients = settings.getBoolean(
                context.getString(R.string.pref_mensa_ingredients), true);
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

        ImageView info = (ImageView) convertView.findViewById(R.id.img_info);
        String[] labels = model.getLabels();
        if (labels != null && labels.length != 0 && showIngredients) {
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
     * if showIngredients is set: returns a SpannableString for the given text. All occurences
     * of (A,B,C) are put in superscript with the parentheses removed.
     * otherwise: returns just a string with all those occurences removed.
     */
    private CharSequence createMensaItemSpannable(String desc) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (int pos = 0, oldPos = 0; pos < desc.length(); oldPos = pos) {
            int openParen = desc.indexOf('(', oldPos);
            int closeParen = desc.indexOf(')', openParen+1);
            pos = closeParen+1;
            if (openParen == -1 || closeParen == -1) {
                sb.append(desc, oldPos, desc.length());
                break;
            }
            if (!isValidIngredients(desc.substring(openParen+1, closeParen))) {
                sb.append(desc, oldPos, pos);
                continue;
            }
            // remove the potential space before the ingredients
            int firstStrEnd = openParen;
            while (firstStrEnd > oldPos && desc.charAt(firstStrEnd - 1) == ' ')
                --firstStrEnd;
            sb.append(desc, oldPos, firstStrEnd);
            if (showIngredients) {
                int oldLen = sb.length();
                sb.append(desc, openParen + 1, closeParen);
                int newLen = sb.length();
                sb.setSpan(new SuperscriptSpan(), oldLen, newLen,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.8f), oldLen, newLen,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        if (!showIngredients)
            return sb.toString(); // no spannable string, just the normal string

        return sb;
    }

    private boolean isValidIngredients(String substring) {
        String[] parts = substring.split(",");
        for (String part : parts)
            if (part.trim().length() > 2)
                return false;
        return true;
    }

    public boolean update(List<MensaItem> items) {
        if (items == null)
            throw new NullPointerException();
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