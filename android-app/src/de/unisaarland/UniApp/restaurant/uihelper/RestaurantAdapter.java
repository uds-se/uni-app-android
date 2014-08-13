package de.unisaarland.UniApp.restaurant.uihelper;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.model.MensaItem;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/6/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class RestaurantAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<MensaItem> mensaItems;

    public RestaurantAdapter(Context context, ArrayList<MensaItem> mensaItems) {
        this.context = context;
        this.mensaItems = mensaItems;
    }

    @Override
    public int getCount() {
        return mensaItems.size();  //To change body of implemented methods use File | Settings | File Templates.
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
        if(convertView == null){
            convertView = View.inflate(context, R.layout.mensa_item, null);
        }
        MensaItem model = mensaItems.get(position);

        TextView mealTitle = (TextView) convertView.findViewById(R.id.mensa_menu_category);
        mealTitle.setText(model.getCategory());
        mealTitle.setVisibility(View.VISIBLE);

        TextView mealDescription = (TextView) convertView.findViewById(R.id.mensa_menu_detail);
        mealDescription.setText(model.getTitle() + " " + model.getDesc());
        mealDescription.setVisibility(View.VISIBLE);

        TextView descriptionBackgroundColor = (TextView) convertView.findViewById(R.id.mensa_menu_detail_background);
        descriptionBackgroundColor.setVisibility(View.VISIBLE);
        descriptionBackgroundColor.setText(model.getTitle() + " " + model.getDesc());
        String modelColor = model.getColor();
        String colors[] =modelColor.split(",");
        descriptionBackgroundColor.setBackgroundColor(Color.rgb(Integer.parseInt(colors[0]),Integer.parseInt(colors[1]),Integer.parseInt(colors[2])));
        descriptionBackgroundColor.setTextColor(Color.rgb(Integer.parseInt(colors[0]),Integer.parseInt(colors[1]),Integer.parseInt(colors[2])));


        TextView mealPrice = (TextView) convertView.findViewById(R.id.mensa_menu_price);
        mealPrice.setText("Studenten: " +model.getPreis1() +"€ Mitarbeiter: " + model.getPreis2()+"€ Besucher:" + model.getPreis3()+"€");
        mealPrice.setVisibility(View.VISIBLE);
        return convertView;
    }
}
