package de.unisaarland.UniApp.news.uihelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.networkcommunicator.Util;
import de.unisaarland.UniApp.news.NewsArticleActivity;
import de.unisaarland.UniApp.news.model.NewsModel;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 11/28/13
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewsAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<NewsModel> newsModelsArray;
    private final HashMap<View,Integer> newsItemsMap = new HashMap<View,Integer>();

    /*
        * Will be called when user clicks on a news
        * Will try to load news detail from the internet if internet is connected otherwise error will be displayed
        * */
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Util.isConnectedToInternet(context)) {
                Intent myIntent = new Intent(context, NewsArticleActivity.class);
                int index = newsItemsMap.get(v);
                NewsModel model = newsModelsArray.get(index);
                myIntent.putExtra("model", model);
                context.startActivity(myIntent);
            }else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage(context.getString(R.string.not_connected));
                builder1.setCancelable(true);
                builder1.setPositiveButton(context.getString(R.string.ok),
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

    public NewsAdapter(Context context, ArrayList<NewsModel> newsModelsArray) {
        this.context = context;
        this.newsModelsArray = newsModelsArray;
    }

    @Override
    public int getCount() {
        return newsModelsArray.size();
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
            convertView = View.inflate(context, R.layout.news_item, null);
        }
        NewsModel model = newsModelsArray.get(position);
        TextView newsTitle = (TextView) convertView.findViewById(R.id.news_title);
        newsTitle.setText(model.getPublicationDate());
        TextView newsDescription = (TextView) convertView.findViewById(R.id.news_description);
        newsDescription.setGravity(Gravity.CENTER_VERTICAL);
        newsDescription.setText(model.getNewsTitle());
        convertView.setOnClickListener(clickListener);
        newsItemsMap.put(convertView,position);
        return convertView;
    }
}