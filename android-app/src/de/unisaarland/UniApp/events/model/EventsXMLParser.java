package de.unisaarland.UniApp.events.model;

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

import de.unisaarland.UniApp.utils.XMLExtractor;


public class EventsXMLParser extends XMLExtractor<List<EventModel>> {

    private final String TAG = EventsXMLParser.class.getSimpleName();

    private final String TITLE = "title";
    private final String PUBLICATION_DATE = "pubDate";
    private final String LINK = "link";
    private final String DESCRIPTION = "content:encoded";
    private final String START_TAG = "rss";
    private final String ITEM_TAG = "item";

    @Override
    public List<EventModel> extractFromXML(XmlPullParser parser)
            throws IOException, XmlPullParserException, ParseException {
        List<EventModel> events = new ArrayList<>();

        parser.require(XmlPullParser.START_DOCUMENT, null, null);
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, START_TAG);

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;
            if (parser.getName().equals(ITEM_TAG))
                events.add(readEntry(parser));
        }
        return events;
    }

    private EventModel readEntry(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {
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

    private String getElementValue(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        parser.next();
        return title;
    }

    private String readText(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        StringBuilder res = new StringBuilder();
        while (parser.next() == XmlPullParser.TEXT)
            res.append(parser.getText());
        return res.toString();
    }

    private void skipTag(XmlPullParser parser)
            throws XmlPullParserException, IOException {
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
