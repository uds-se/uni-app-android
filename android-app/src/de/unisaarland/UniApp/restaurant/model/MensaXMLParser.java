package de.unisaarland.UniApp.restaurant.model;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/6/13
 * Time: 12:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class MensaXMLParser {
    private final IMensaResultDelegate mensaResultDelegate;
    private HashMap<String,ArrayList<MensaItem>> entries = null;
    private final String START_TAG = "speiseplan";
    private final String TAG = "tag";
    private final String ITEM_TAG = "item";

    private final String TITLE = "title";
    private final String CATEGORY = "category";
    private final String DESCRIPTION = "description";
    private final String COMPONENTS = "components";
    private final String KENNZEICHNUNGEN = "kennzeichnungen";
    private final String BEILAGEN = "beilagen";
    private final String PREIS1 = "preis1";
    private final String PREIS2 = "preis2";
    private final String PREIS3 = "preis3";
    private final String COLOR = "color";

    private Long date;

    public MensaXMLParser(IMensaResultDelegate mensaResultDelegate) {
        this.mensaResultDelegate = mensaResultDelegate;
    }

    public List<MensaItem> parse(InputStream data) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(data, null);
        } catch (XmlPullParserException e) {
            throw new AssertionError(e);
        }

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
                            String tempDate = parser.getAttributeValue(0);
                            date = Long.parseLong(tempDate)*1000;
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY,0);
                            cal.set(Calendar.MINUTE,0);
                            cal.set(Calendar.SECOND,0);

                            Date now = cal.getTime();
                            Date tagDate = new Date(date);

                            if(now.before(tagDate) ||
                                    (cal.get(Calendar.DATE) == tagDate.getDate() && cal.get(Calendar.MONTH) == tagDate.getMonth())) {
                                ArrayList<MensaItem> items = readEntry(parser);
                                entries.put(tempDate, items);
                            }
                        }
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

    private ArrayList<MensaItem> readEntry(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, null, TAG);
        ArrayList<MensaItem> mensaItems = new ArrayList<MensaItem>();
//        String name = parser.getName();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                MensaItem item = parseMensaItem(parser);
                mensaItems.add(item);
            }
        return mensaItems;
    }


    private MensaItem parseMensaItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, ITEM_TAG);
        MensaItem model = new MensaItem();
        Boolean components_open = true;
        while (parser.next() != XmlPullParser.END_TAG ) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getEventType() != XmlPullParser.END_TAG) {
                components_open = false;
            }
            String name = parser.getName();
            if (name.equals(TITLE)) {
                model.setTitle(getElementValue(parser, TITLE));
            } else if (name.equals(CATEGORY)) {
                model.setCategory(getElementValue(parser, CATEGORY));
            } else if (name.equals(DESCRIPTION)) {
                model.setDesc(getElementValue(parser, DESCRIPTION));
            } else if (name.equals(KENNZEICHNUNGEN)) {
                model.setKennzeichnungen(getElementValue(parser,KENNZEICHNUNGEN));
            } else if(name.equals(BEILAGEN)){
                model.setBeilagen(getElementValue(parser, BEILAGEN));
            } else if(name.equals(PREIS1)){
                model.setPreis1(getElementValue(parser,PREIS1));
            } else if(name.equals(PREIS2)){
                model.setPreis2(getElementValue(parser, PREIS2));
            } else if(name.equals(PREIS3)){
                model.setPreis3(getElementValue(parser, PREIS3));
            } else if(name.equals(COLOR)) {
                model.setColor(getElementValue(parser, COLOR));
            } else {
                skip(parser);
            }
        }
        model.setTag(new Date(date));
        return model;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String getElementValue(XmlPullParser parser,String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return title;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        result = result.replace("&quot;","\"") ;
        return result;
    }
}
