package de.unisaarland.UniApp.news.model;

import android.os.AsyncTask;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 11/30/13
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewsXMLParser {
    private ArrayList<NewsModel> entries = null;
    private final String TITLE = "title";
    private final String PUBLICATION_DATE = "pubDate";
    private final String LINK = "link";
    private final String DESCRIPTION = "content:encoded";
    private final String START_TAG = "rss";
    private final String ITEM_TAG = "item";
    private INewsResultDelegate newsResultDelegate = null;

    public NewsXMLParser(INewsResultDelegate newsResultDelegate) {
        this.newsResultDelegate = newsResultDelegate;
    }

    public List<NewsModel> parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        try {
            readFeed(parser);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
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
                        if (parser.getName().equals(ITEM_TAG)) {
                            entries.add(readEntry(parser));
                        } else {
                            // do nothing
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
                newsResultDelegate.newsList(entries);
            }
        };
    }

    private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        entries = new ArrayList<NewsModel>();
        getTask(parser).execute();
    }

    private NewsModel readEntry(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, null, ITEM_TAG);
        NewsModel model = new NewsModel();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TITLE)) {
                model.setNewsTitle(getElementValue(parser,TITLE));
            } else if (name.equals(PUBLICATION_DATE)) {
                String input = getElementValue(parser, PUBLICATION_DATE);
                model.setPublicationDate(input.substring(0,17));
            } else if (name.equals(LINK)) {
                String link = getElementValue(parser,LINK);
                model.setLink(link);
            } else if(name.equals(DESCRIPTION)){
                model.setNewsDescription(getElementValue(parser,DESCRIPTION));
            }else{
                readText(parser);
            }
        }
        return model;
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
        return result;
    }
}
