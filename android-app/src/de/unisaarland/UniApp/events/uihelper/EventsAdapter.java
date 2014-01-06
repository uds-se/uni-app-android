package de.unisaarland.UniApp.events.uihelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.events.EventDetailActivity;
import de.unisaarland.UniApp.events.model.EventsModel;
import de.unisaarland.UniApp.networkcommunicator.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/2/13
 * Time: 1:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventsAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<EventsModel> eventModelsArray;
    private final HashMap<View,Integer> eventItemsMap = new HashMap<View,Integer>();

    public EventsAdapter(Context context, ArrayList<EventsModel> eventModelsArray) {
        this.context = context;
        this.eventModelsArray = eventModelsArray;
    }

    /*
        * Will be called when user clicks on a event
        * Will try to load event detail from the internet if internet is connected otherwise error will be displayed
        * */
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Util.isConnectedToInternet(context)) {
                Intent myIntent = new Intent(context, EventDetailActivity.class);
                int index = eventItemsMap.get(v);
                EventsModel model = eventModelsArray.get(index);
                myIntent.putExtra("model", model);
                context.startActivity(myIntent);
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
        return eventModelsArray.size();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getItem(int position) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getItemId(int position) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // sets the view of event item row
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(context, R.layout.event_item, null);
        }
        EventsModel model = eventModelsArray.get(position);
        String date = model.getPublicationDate();

        TextView eventMonth = (TextView) convertView.findViewById(R.id.month_text);
        eventMonth.setText(date.substring(8,11));
        TextView eventDate = (TextView) convertView.findViewById(R.id.day_text);
        eventDate.setText(date.substring(5,7));

        TextView eventDescription = (TextView) convertView.findViewById(R.id.event_description);
        eventDescription.setText(model.getEventTitle());
        eventDescription.setVisibility(View.VISIBLE);
        convertView.setOnClickListener(clickListener);
        eventItemsMap.put(convertView, position);
        return convertView;
    }
}
