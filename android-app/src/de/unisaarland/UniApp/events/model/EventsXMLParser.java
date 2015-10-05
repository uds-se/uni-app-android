package de.unisaarland.UniApp.events.model;

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


public class EventsXMLParser {

    private final String TAG = EventsXMLParser.class.getSimpleName();

    private final String TITLE = "title";
    private final String PUBLICATION_DATE = "pubDate";
    private final String LINK = "link";
    private final String DESCRIPTION = "content:encoded";
    private final String START_TAG = "rss";
    private final String ITEM_TAG = "item";

    public void startParsing(final InputStream data,
                             final IEventsResultDelegate eventsResultDelegate,
                             final Context context) {
        new AsyncTask<Void, Void, List<EventModel>>() {
            private String errorMessage = null;
            @Override
            protected List<EventModel> doInBackground(Void... params) {
                List<EventModel> events = new ArrayList<EventModel>();

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
                            events.add(readEntry(parser));
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
                return events;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected void onPostExecute(List<EventModel> events) {
                if (events == null) {
                    if (errorMessage == null)
                        errorMessage = "Unknown error retrieving events";
                    eventsResultDelegate.onFailure(errorMessage);
                } else if (events.isEmpty()) {
                    eventsResultDelegate.onFailure(
                            context.getResources().getString(R.string.noEventsText));
                } else {
                    eventsResultDelegate.eventsList(events);
                }
            }
        }.execute();
    }

    private EventModel readEntry(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
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
                title = getElementValue(parser, TITLE);
            } else if (name.equals(PUBLICATION_DATE)) {
                String input = getElementValue(parser, PUBLICATION_DATE);
                SimpleDateFormat parserSDF = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                date = parserSDF.parse(input);
            } else if (name.equals(LINK)) {
                link = getElementValue(parser, LINK);
            } else if (name.equals(DESCRIPTION)) {
                description = getElementValue(parser, DESCRIPTION);
            } else {
                skipTag(parser);
            }
        }
        if (title == null || description == null || date == null || link == null)
            throw new ParseException("Incomplete event", parser.getLineNumber());
        return new EventModel(title, description, date, link);
    }

    private String getElementValue(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        parser.next();
        return title;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        StringBuilder res = new StringBuilder();
        while (parser.next() == XmlPullParser.TEXT)
            res.append(parser.getText());
        return res.toString();
    }

    private void skipTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        assert (parser.getEventType() == XmlPullParser.START_TAG);
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
