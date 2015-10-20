package de.unisaarland.UniApp.staff.uihelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.staff.SearchResult;
import de.unisaarland.UniApp.staff.SearchResultItemDetailActivity;
import de.unisaarland.UniApp.utils.Util;

public class SearchResultAdapter extends BaseAdapter {

    private final Context context;
    private List<SearchResult> result;

    public SearchResultAdapter(Context context, List<SearchResult> result) {
        this.context = context;
        this.result = result;
    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        return result.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.search_result_row, null);
        }

        TextView nameView = (TextView) convertView.findViewById(R.id.search_result_name);
        SearchResult searchResult = result.get(position);
        nameView.setText(searchResult.getName());
        convertView.setOnClickListener(clickListener);
        convertView.setTag(R.id.staff_search_person_tag, searchResult);
        return convertView;
    }

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!Util.isConnectedToInternet(context)) {
                new AlertDialog.Builder(context)
                        .setMessage(context.getString(R.string.not_connected))
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .create().show();
                return;
            }
            Intent myIntent = new Intent(context, SearchResultItemDetailActivity.class);
            SearchResult res = (SearchResult) v.getTag(R.id.staff_search_person_tag);
            myIntent.putExtra("url", res.getUrl());
            context.startActivity(myIntent);
        }
    };

    public void update(List<SearchResult> result) {
        this.result = result;
    }
}
