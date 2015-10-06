package de.unisaarland.UniApp.news.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.unisaarland.UniApp.R;

public class NewsXMLParser {

    private final String TAG = NewsXMLParser.class.getSimpleName();

    private static final String TITLE = "title";
    private static final String PUBLICATION_DATE = "pubDate";
    private static final String LINK = "link";
    private static final String DESCRIPTION = "content:encoded";
    private static final String START_TAG = "rss";
    private static final String ITEM_TAG = "item";

    public void startParsing(final InputStream data,
                             final INewsResultDelegate delegate,
                             final Context context) {
        new AsyncTask<Void, Void, List<NewsModel>>() {
            private String errorMessage = null;
            @Override
            protected List<NewsModel> doInBackground(Void... params) {
                List<NewsModel> news = new ArrayList<NewsModel>();

                XmlPullParser parser = Xml.newPullParser();
                try {
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(data, null);
                } catch (XmlPullParserException e) {
                    throw new AssertionError(e);
                }

                try {
                    parser.require(XmlPullParser.START_DOCUMENT, null, null);
                    parser.next();
                    while (parser.next() != XmlPullParser.END_DOCUMENT) {
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }
                        if (parser.getName().equals(ITEM_TAG)) {
                            news.add(readEntry(parser));
                        }
                    }
                } catch (XmlPullParserException | ParseException e) {
                    errorMessage = "Error parsing events: " + e.getLocalizedMessage();
                    Log.w(TAG, "event parse error", e);
                    return null;
                } catch (IOException e) {
                    errorMessage = "Error retrieving events: " + e.getLocalizedMessage();
                    Log.w(TAG, "event retrieve error", e);
                    return null;
                }
                return news;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected void onPostExecute(List<NewsModel> news) {
                if (news == null) {
                    if (errorMessage == null)
                        errorMessage = "Unkown error retrieving news";
                    delegate.onFailure(errorMessage);
                } else if (news.isEmpty()) {
                    delegate.onFailure(
                            context.getResources().getString(R.string.noNewsText));
                } else {
                    delegate.newsList(news);
                }
            }
        }.execute();
    }

    private NewsModel readEntry(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, null, ITEM_TAG);
        String title = null;
        String description = null;
        Date date = null;
        String link = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TITLE)) {
                title = getElementValue(parser,TITLE);
            } else if (name.equals(PUBLICATION_DATE)) {
                String input = getElementValue(parser, PUBLICATION_DATE);
                String datestring = input.toString();
                SimpleDateFormat parserSDF = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                date = parserSDF.parse(datestring);
            } else if (name.equals(LINK)) {
                link = getElementValue(parser,LINK);
            } else if(name.equals(DESCRIPTION)){
                description = getElementValue(parser,DESCRIPTION);
            } else {
                skipTag(parser);
            }
        }
        if (title == null || description == null || date == null || link == null)
            throw new ParseException("Incomplete event", parser.getLineNumber());
        return new NewsModel(title, description, date, link);
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

    private void skipTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG)
            throw new IllegalStateException("Should be at start of a tag, is "+parser.getEventType());
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
}
