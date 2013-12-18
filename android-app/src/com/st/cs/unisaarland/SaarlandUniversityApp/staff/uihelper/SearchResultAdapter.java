package com.st.cs.unisaarland.SaarlandUniversityApp.staff.uihelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.networkcommunicator.Util;
import com.st.cs.unisaarland.SaarlandUniversityApp.staff.SearchResultItemDetailActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/12/13
 * Time: 11:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<String> linksArray;
    private final ArrayList<String> namesArray;
    private final HashMap<View,Integer> resultItemsMap = new HashMap<View,Integer>();

    public SearchResultAdapter(Context context, ArrayList<String> namesArray, ArrayList<String> linksArray) {
        this.context = context;
        this.namesArray = namesArray;
        this.linksArray = linksArray;
    }

    @Override
    public int getCount() {
        return namesArray.size();  //To change body of implemented methods use File | Settings | File Templates.
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
            convertView = View.inflate(context, R.layout.search_result_row, null);
        }

        TextView newsTitle = (TextView) convertView.findViewById(R.id.name);
        newsTitle.setText(namesArray.get(position));
        convertView.setOnClickListener(clickListener);
        resultItemsMap.put(convertView,position);
        return convertView;
    }

    private boolean saveSearchResultToFile() {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir().getAbsolutePath()+ Util.TEMP_STAFF_SEARCH_FILE)));
            oos.writeObject(namesArray);
            oos.writeObject(linksArray);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Util.isConnectedToInternet(context)) {
                saveSearchResultToFile();
                Intent myIntent = new Intent(context, SearchResultItemDetailActivity.class);
                int index = resultItemsMap.get(v);
                myIntent.putExtra("url", linksArray.get(index));
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
}
