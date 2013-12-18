package com.st.cs.unisaarland.SaarlandUniversityApp.restaurant.model;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/13/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class AusLanderCafeParser {
    private final HashMap<String, ArrayList<MensaItem>> daysDictionary;
    private final IMensaResultDelegate auslanderResultDelegate;
    String url;
    public AusLanderCafeParser(IMensaResultDelegate auslanderResultDelegate, String url, HashMap<String, ArrayList<MensaItem>> daysDictionary){
        this.url = url;
        this.daysDictionary = daysDictionary;
        this.auslanderResultDelegate = auslanderResultDelegate;
    }

    public void parse(){
        getTask(url).execute();
    }

    private AsyncTask<Void,Void,Integer> getTask(final String url){
        return new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Document doc = null;
                try {
                    doc = Jsoup.connect(url).get();
                } catch (IOException e) {
                    Log.e("MyTag", e.getMessage());
                }
                Elements divElements = doc.getElementsByTag("table");
                Element divElement = null;
                for(int i=0 ; i < divElements.size() ; i++){
                    divElement = divElements.get(i);
                    if(divElement.className().equals("contenttable contenttable-0")){
                        break;
                    }
                }

                Elements dayElements = divElement.getElementsByTag("td");
                Calendar c = Calendar.getInstance();
                int currentDay = c.get(Calendar.DAY_OF_WEEK);
                if(currentDay != 7 && currentDay != 1){
                    currentDay = 2;
                    c.set(Calendar.DAY_OF_WEEK,currentDay);
                    c.set(Calendar.HOUR_OF_DAY,0);
                    c.set(Calendar.MINUTE,0);
                    c.set(Calendar.SECOND,0);

                    //// monday
                    ArrayList<MensaItem> items = daysDictionary.get((Long.toString(c.getTimeInMillis()/1000)));
                    String dayName = dayElements.get(0).text();
                    String item1 = dayElements.get(2).text();
                    String item1Price = dayElements.get(3).text();
                    String item2 = dayElements.get(4).text();
                    String item2Price = dayElements.get(5).text();
                    if(items!=null){
                        MensaItem mItem1 = new MensaItem();
                        mItem1.setCategory("Ausländer-Café");
                        mItem1.setTitle(item1);
                        mItem1.setDesc("");
                        mItem1.setPreis1(item1Price);
                        mItem1.setPreis2(item1Price);
                        mItem1.setPreis3(item1Price);
                        mItem1.setTag(c.getTime());
                        mItem1.setColor("217,38,26");
                        items.add(mItem1);
                        MensaItem mItem2 = new MensaItem();
                        mItem2.setCategory("Ausländer-Café");
                        mItem2.setTitle(item2);
                        mItem2.setDesc("");
                        mItem2.setPreis1(item2Price);
                        mItem2.setPreis2(item2Price);
                        mItem2.setPreis3(item2Price);
                        mItem2.setTag(c.getTime());
                        mItem2.setColor("245,204,43");
                        items.add(mItem2);
                    }
                    currentDay++;
                    c.set(Calendar.DAY_OF_WEEK,currentDay);
                    //// Tuesday
                    items = daysDictionary.get((Long.toString(c.getTimeInMillis()/1000)));
                    dayName = dayElements.get(6).text();
                    item1 = dayElements.get(8).text();
                    item1Price = dayElements.get(9).text();
                    item2 = dayElements.get(10).text();
                    item2Price = dayElements.get(11).text();
                    if(items!=null){
                        MensaItem mItem1 = new MensaItem();
                        mItem1.setCategory("Ausländer-Café");
                        mItem1.setTitle(item1);
                        mItem1.setDesc("");
                        mItem1.setPreis1(item1Price);
                        mItem1.setPreis2(item1Price);
                        mItem1.setPreis3(item1Price);
                        mItem1.setTag(c.getTime());
                        mItem1.setColor("217,38,26");
                        items.add(mItem1);
                        MensaItem mItem2 = new MensaItem();
                        mItem2.setCategory("Ausländer-Café");
                        mItem2.setTitle(item2);
                        mItem2.setDesc("");
                        mItem2.setPreis1(item2Price);
                        mItem2.setPreis2(item2Price);
                        mItem2.setPreis3(item2Price);
                        mItem2.setTag(c.getTime());
                        mItem2.setColor("245,204,43");
                        items.add(mItem2);
                    }
                    currentDay++;
                    c.set(Calendar.DAY_OF_WEEK,currentDay);
                    //// Wednesday
                    items = daysDictionary.get((Long.toString(c.getTimeInMillis()/1000)));
                    dayName = dayElements.get(12).text();
                    item1 = dayElements.get(14).text();
                    item1Price = dayElements.get(15).text();
                    item2 = dayElements.get(16).text();
                    item2Price = dayElements.get(17).text();
                    if(items!=null){
                        MensaItem mItem1 = new MensaItem();
                        mItem1.setCategory("Ausländer-Café");
                        mItem1.setTitle(item1);
                        mItem1.setDesc("");
                        mItem1.setPreis1(item1Price);
                        mItem1.setPreis2(item1Price);
                        mItem1.setPreis3(item1Price);
                        mItem1.setTag(c.getTime());
                        mItem1.setColor("217,38,26");
                        items.add(mItem1);
                        MensaItem mItem2 = new MensaItem();
                        mItem2.setCategory("Ausländer-Café");
                        mItem2.setTitle(item2);
                        mItem2.setDesc("");
                        mItem2.setPreis1(item2Price);
                        mItem2.setPreis2(item2Price);
                        mItem2.setPreis3(item2Price);
                        mItem2.setTag(c.getTime());
                        mItem2.setColor("245,204,43");
                        items.add(mItem2);
                    }
                    currentDay++;
                    c.set(Calendar.DAY_OF_WEEK,currentDay);
                    //// Thursday
                    items = daysDictionary.get((Long.toString(c.getTimeInMillis()/1000)));
                    dayName = dayElements.get(18).text();
                    item1 = dayElements.get(20).text();
                    item1Price = dayElements.get(21).text();
                    item2 = dayElements.get(22).text();
                    item2Price = dayElements.get(23).text();
                    if(items!=null){
                        MensaItem mItem1 = new MensaItem();
                        mItem1.setCategory("Ausländer-Café");
                        mItem1.setTitle(item1);
                        mItem1.setDesc("");
                        mItem1.setPreis1(item1Price);
                        mItem1.setPreis2(item1Price);
                        mItem1.setPreis3(item1Price);
                        mItem1.setTag(c.getTime());
                        mItem1.setColor("217,38,26");
                        items.add(mItem1);
                        MensaItem mItem2 = new MensaItem();
                        mItem2.setCategory("Ausländer-Café");
                        mItem2.setTitle(item2);
                        mItem2.setDesc("");
                        mItem2.setPreis1(item2Price);
                        mItem2.setPreis2(item2Price);
                        mItem2.setPreis3(item2Price);
                        mItem2.setTag(c.getTime());
                        mItem2.setColor("245,204,43");
                        items.add(mItem2);
                    }
                    currentDay++;
                    c.set(Calendar.DAY_OF_WEEK,currentDay);
                    //// Friday
                    items = daysDictionary.get((Long.toString(c.getTimeInMillis()/1000)));
                    dayName = dayElements.get(24).text();
                    item1 = dayElements.get(26).text();
                    item1Price = dayElements.get(27).text();
                    item2 = dayElements.get(28).text();
                    item2Price = dayElements.get(29).text();
                    if(items!=null){
                        MensaItem mItem1 = new MensaItem();
                        mItem1.setCategory("Ausländer-Café");
                        mItem1.setTitle(item1);
                        mItem1.setDesc("");
                        mItem1.setPreis1(item1Price);
                        mItem1.setPreis2(item1Price);
                        mItem1.setPreis3(item1Price);
                        mItem1.setTag(c.getTime());
                        mItem1.setColor("217,38,26");
                        items.add(mItem1);
                        MensaItem mItem2 = new MensaItem();
                        mItem2.setCategory("Ausländer-Café");
                        mItem2.setTitle(item2);
                        mItem2.setDesc("");
                        mItem2.setPreis1(item2Price);
                        mItem2.setPreis2(item2Price);
                        mItem2.setPreis3(item2Price);
                        mItem2.setTag(c.getTime());
                        mItem2.setColor("245,204,43");
                        items.add(mItem2);
                    }
                }
                return 1;
            }

            @Override
            protected void onPostExecute(Integer i) {
                auslanderResultDelegate.mensaItemsList(daysDictionary);
            }
        };
    }
}