package de.unisaarland.UniApp.rssViews;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.rssViews.model.RSSItem;


public class RSSAdapter extends BaseAdapter {

    private final Context context;
    private List<RSSItem> items;
    private final RSSActivity.Category cat;

    public RSSAdapter(Context context, List<RSSItem> items, RSSActivity.Category cat) {
        this.context = context;
        this.items = items;
        this.cat = cat;
    }

    /**
     * Will be called when user clicks on a event
     * Will try to load event detail from the internet if internet is connected otherwise error will be displayed
     */
    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(context, RSSDetailActivity.class);
            RSSItem item = (RSSItem) v.getTag(R.id.rss_view_model_tag);
            myIntent.putExtra("url", item.getLink());
            myIntent.putExtra("titleId", cat.articleTitle);
            context.startActivity(myIntent);
        }
    };

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RSSItem item = items.get(position);
        View view = cat.getView(item, convertView, context);
        view.setOnClickListener(clickListener);
        view.setTag(R.id.rss_view_model_tag, item);
        return view;
    }

    public boolean update(List<RSSItem> items) {
        if (this.items.equals(items))
            return false;
        this.items = items;
        this.notifyDataSetChanged();
        return true;
    }
}
