package de.unisaarland.UniApp.restaurant.model;

import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/9/13
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CafeXMLParser {

    private final IMensaResultDelegate mensaResultDelegate;
    private HashMap<String,ArrayList<MensaItem>> entries = null;
    private final String START_TAG = "html";
    private final String TAG = "strong";

    public CafeXMLParser(IMensaResultDelegate mensaResultDelegate) {
        this.mensaResultDelegate = mensaResultDelegate;
    }

    public List<MensaItem> parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        try {
            readFeed(parser);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        entries = new HashMap<String,ArrayList<MensaItem>>(5);
        getTask(parser).execute();
    }

    private AsyncTask<Void, Void, Integer> getTask(final XmlPullParser parser) {
        return new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    parser.require(XmlPullParser.START_TAG, null, START_TAG);
                    while (parser.next()!=XmlPullParser.END_DOCUMENT) {
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }


                        if(parser.getName().equals(TAG)){
                            String name = parser.getName();
                            System.out.print(name);
                        }
//                            String tempDate = parser.getAttributeValue(0);
//                            date = Long.parseLong(tempDate)*1000;
//                            Date now = new Date();
//                            Date tagDate = new Date(date);
//                            now.setHours(0);
//                            now.setMinutes(0);
//                            now.setSeconds(0);
//                            if(now.before(tagDate) ||
//                                    (now.getDate() == tagDate.getDate() && now.getMonth() == tagDate.getMonth())) {
//                                ArrayList<MensaItem> items = readEntry(parser);
//                                entries.put(tempDate, items);
//                            }
//                        }
                    }
                    return 1;
                }
                catch (Exception e){
                    Log.e("MyTag", e.getMessage());
                }
                return 1;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected void onPostExecute(Integer i) {
                mensaResultDelegate.mensaItemsList(entries);
            }
        };
    }

}
