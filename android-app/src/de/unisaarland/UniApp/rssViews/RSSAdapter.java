package de.unisaarland.UniApp.rssViews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.rssViews.model.RSSItem;
import de.unisaarland.UniApp.utils.Util;


public class RSSAdapter extends BaseAdapter {

    private final Context context;
    private List<RSSItem> itemsArray;
    private final RSSActivity.Category cat;

    public RSSAdapter(Context context, List<RSSItem> itemsArray,
                      RSSActivity.Category cat) {
        this.context = context;
        this.itemsArray = itemsArray;
        this.cat = cat;
    }

    /**
     * Will be called when user clicks on a event
     * Will try to load event detail from the internet if internet is connected otherwise error will be displayed
     */
    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!Util.isConnectedToInternet(context)) {
                new AlertDialog.Builder(context)
                        .setMessage(R.string.not_connected)
                        .setCancelable(true)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .create().show();
                return;
            }
            Intent myIntent = new Intent(context, RSSDetailActivity.class);
            RSSItem item = (RSSItem) v.getTag(R.id.rss_view_model_tag);
            myIntent.putExtra("url", item.getLink());
            myIntent.putExtra("titleId", R.string.event_article);
            context.startActivity(myIntent);
        }
    };

    @Override
    public int getCount() {
        return itemsArray.size();
    }

    @Override
    public Object getItem(int position) {
        return itemsArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RSSItem item = itemsArray.get(position);
        View view = cat.getView(item, convertView, context);
        view.setOnClickListener(clickListener);
        view.setTag(R.id.rss_view_model_tag, item);
        return view;
    }

    public void update(List<RSSItem> items) {
        this.itemsArray = items;
    }
}
