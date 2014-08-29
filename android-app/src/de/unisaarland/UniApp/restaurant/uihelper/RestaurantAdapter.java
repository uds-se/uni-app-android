package de.unisaarland.UniApp.restaurant.uihelper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.mensa_item, null);
        }
        MensaItem model = mensaItems.get(position);

        TextView mealTitle = (TextView) convertView.findViewById(R.id.mensa_menu_category);
        mealTitle.setText(model.getCategory());
        mealTitle.setVisibility(View.VISIBLE);

        TextView mealDescription = (TextView) convertView.findViewById(R.id.mensa_menu_detail);
        mealDescription.setText(model.getTitle() + " " + model.getDesc());
        mealDescription.setVisibility(View.VISIBLE);


        ImageView info = (ImageView) convertView.findViewById(R.id.info);
        String kennzeichnungen = model.getKennzeichnungen();
        if (kennzeichnungen != null && !kennzeichnungen.equals(""))
        {
            info.setOnClickListener(new ClickListener(kennzeichnungen,context));
        }
        else {
            info.getLayoutParams().width = 0;
        }

        TextView descriptionBackgroundColor = (TextView) convertView.findViewById(R.id.mensa_menu_detail_background);
        descriptionBackgroundColor.setVisibility(View.VISIBLE);
        descriptionBackgroundColor.setText(model.getTitle() + " " + model.getDesc());
        String modelColor = model.getColor();
        String colors[] =modelColor.split(",");
        descriptionBackgroundColor.setBackgroundColor(Color.rgb(Integer.parseInt(colors[0]),Integer.parseInt(colors[1]),Integer.parseInt(colors[2])));
        descriptionBackgroundColor.setTextColor(Color.rgb(Integer.parseInt(colors[0]),Integer.parseInt(colors[1]),Integer.parseInt(colors[2])));

        TextView mealPrice = (TextView) convertView.findViewById(R.id.mensa_menu_price);
        if (!model.getPreis1().equals("")) {
            mealPrice.setText("Studenten: " + model.getPreis1() + "€ Mitarbeiter: " + model.getPreis2() + "€ Besucher: " + model.getPreis3() + "€");
            mealPrice.setVisibility(View.VISIBLE);
        }
        return convertView;
    }


}

 class ClickListener implements View.OnClickListener{

     private String ingredis;
     private Context context;

    public  ClickListener(String ingredis, Context context){
        this.ingredis = ingredis;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.label_list,null);
        ListView list = (ListView) view.findViewById(R.id.label_list);
        String[] labels = ingredis.split(",");
        list.setAdapter(new labelAdapter(context,labels));
        Dialog dialog = new Dialog(context, R.style.Transparent);
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        dialog.setContentView(view);
        dialog.setTitle(view.getResources().getString(R.string.labels));
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = (int) Math.round(size.x*0.9);
        int height = (int) Math.round(size.y*0.9);
        dialog.getWindow().setLayout(width, height);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.7f;
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();
    }
}

class labelAdapter extends BaseAdapter {

    private final Context context;
    private final String[] labels;



    public labelAdapter(Context context, String[] labels) {
        this.context = context;
        this.labels = labels;
    }

    @Override
    public int getCount() {
        return labels.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // sets the view of news item row
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(context,R.layout.label_item,null);
        }
        TextView tw_label = (TextView) convertView.findViewById(R.id.label);
        String stringname;
        try{
            stringname = "label_" + labels[position];
        }
        catch(Exception e){
            stringname = "Keine Angabe";
        }
        tw_label.setText(context.getResources().getIdentifier(stringname, "string", context.getPackageName()));
        return convertView;
    }
}
