package de.unisaarland.UniApp.events.uihelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.events.EventDetailActivity;
import de.unisaarland.UniApp.events.model.EventModel;
import de.unisaarland.UniApp.utils.Util;


public class EventsAdapter extends BaseAdapter {
    private final Context context;
    private final List<EventModel> eventModelsArray;
    private final HashMap<View,Integer> eventItemsMap = new HashMap<View,Integer>();

    public EventsAdapter(Context context, List<EventModel> eventModelsArray) {
        this.context = context;
        this.eventModelsArray = eventModelsArray;
    }

    /*
     * Will be called when user clicks on a event
     * Will try to load event detail from the internet if internet is connected otherwise error will be displayed
     */
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Util.isConnectedToInternet(context)) {
                Intent myIntent = new Intent(context, EventDetailActivity.class);
                int index = eventItemsMap.get(v);
                EventModel model = eventModelsArray.get(index);
                myIntent.putExtra("model", model);
                context.startActivity(myIntent);
                Activity activity = (Activity) context;


            }else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage(context.getString(R.string.not_connected));
                builder1.setCancelable(true);
                builder1.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
    };

    @Override
    public int getCount() {
        return eventModelsArray.size();
    }

    @Override
    public Object getItem(int position) {
        return eventModelsArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // sets the view of event item row
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.event_item, null);
        }
        EventModel model = eventModelsArray.get(position);
        Date date = model.getPublicationDate();
        //Set month in locale language
        SimpleDateFormat SDF = new SimpleDateFormat("MMM");
        TextView eventMonth = (TextView) convertView.findViewById(R.id.month_text);
        eventMonth.setText(SDF.format(date));
        //Set day in local language
        SDF = new SimpleDateFormat("d");
        TextView eventDate = (TextView) convertView.findViewById(R.id.day_text);
        eventDate.setText(SDF.format(date));

        TextView eventDescription = (TextView) convertView.findViewById(R.id.event_description);
        eventDescription.setText(model.getEventTitle());
        eventDescription.setVisibility(View.VISIBLE);
        convertView.setOnClickListener(clickListener);
        eventItemsMap.put(convertView, position);
        return convertView;
    }
}
