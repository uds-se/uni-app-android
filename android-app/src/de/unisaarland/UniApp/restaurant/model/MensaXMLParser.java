package de.unisaarland.UniApp.restaurant.model;

import android.graphics.Color;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisaarland.UniApp.utils.Util;
import de.unisaarland.UniApp.utils.XMLExtractor;


public class MensaXMLParser extends XMLExtractor<Map<Long, List<MensaItem>>> {

    private static final String START_TAG = "speiseplan";
    private static final String TAG = "tag";
    private static final String ITEM_TAG = "item";

    private static final String TITLE = "title";
    private static final String CATEGORY = "category";
    private static final String DESCRIPTION = "description";
    private static final String PREIS1 = "preis1";
    private static final String PREIS2 = "preis2";
    private static final String PREIS3 = "preis3";
    private static final String COLOR = "color";
    private static final String TIMESTAMP = "timestamp";

    @Override
    public Map<Long, List<MensaItem>> extractFromXML(XmlPullParser parser)
            throws IOException, XmlPullParserException, ParseException {
        Map<Long, List<MensaItem>> items = new HashMap<>();

        parser.require(XmlPullParser.START_DOCUMENT, null, null);
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, START_TAG);

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;
            if (parser.getName().equals(TAG)) {
                String tempDate = parser.getAttributeValue(null, TIMESTAMP);
                long date = Long.parseLong(tempDate) * 1000;

                Date tagDate = Util.getStartOfDay(date);

                List<MensaItem> tagItems = readItems(parser);
                items.put(tagDate.getTime(), tagItems);
            }
        }

        return items;
    }

    private List<MensaItem> readItems(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, null, TAG);
        List<MensaItem> mensaItems = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;
            if (parser.getName().equals(ITEM_TAG))
                mensaItems.add(parseMensaItem(parser));
            else
                skipTag(parser);
        }
        return mensaItems;
    }


    private MensaItem parseMensaItem(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, ITEM_TAG);

        String category = null;
        String desc = null;
        String title = null;
        Date tag = null;
        String kennzeichnungen = null;
        String beilagen = null;
        int preis1 = 0;
        int preis2 = 0;
        int preis3 = 0;
        int color = 0;

        while (parser.next() != XmlPullParser.END_TAG ) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;
            String name = parser.getName();
            if (name.equals(TITLE)) {
                title = getElementValue(parser, TITLE);
            } else if (name.equals(CATEGORY)) {
                category = getElementValue(parser, CATEGORY);
            } else if (name.equals(DESCRIPTION)) {
                desc = getElementValue(parser, DESCRIPTION);
            } else if (name.equals(PREIS1)) {
                preis1 = parsePreis(getElementValue(parser, PREIS1));
            } else if (name.equals(PREIS2)) {
                preis2 = parsePreis(getElementValue(parser, PREIS2));
            } else if (name.equals(PREIS3)) {
                preis3 = parsePreis(getElementValue(parser, PREIS3));
            } else if (name.equals(COLOR)) {
                color = parseColor(getElementValue(parser, COLOR));
            } else {
                skipTag(parser);
            }
        }

        String[] labels = extractLabels(title + desc);
        return new MensaItem(category, desc, title, tag, labels, preis1, preis2,
                preis3, color);
    }

    /**
     * Returns the list of all labels extracted from occurances of "(A,B,C)" where each component is
     * at most two characters long.
     */
    private String[] extractLabels(String desc) {
        Set<String> labels = new HashSet<>();
        int pos = 0;
        while (pos < desc.length()) {
            int openParen = desc.indexOf('(', pos);
            int closeParen = desc.indexOf(')', openParen+1);
            if (openParen == -1 || closeParen == -1)
                break;
            boolean valid = true;
            String[] parts = desc.substring(openParen+1, closeParen).split(",");
            for (String part : parts)
                if (part.trim().length() > 2)
                    valid = false;
            if (valid)
                for (String part : parts)
                    labels.add(part);
            pos = closeParen + 1;
        }
        String[] arr = labels.toArray(new String[labels.size()]);
        Arrays.sort(arr);
        return arr;
    }

    /**
     * Parses strings like "2,07" into 207.
     */
    private int parsePreis(String str) {
        int commaPos = str.indexOf(',');
        if (commaPos < 1 || commaPos+1 >= str.length())
            return 0;
        try {
            int euro = Integer.parseInt(str.substring(0, commaPos));
            int cent = Integer.parseInt(str.substring(commaPos+1));
            return 100 * euro + cent;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseColor(String str) {
        String[] parts = str.split(",");
        if (parts.length != 3)
            return 0;
        int red = 0;
        int green = 0;
        int blue = 0;
        try {
            red = Integer.parseInt(parts[0]);
            green = Integer.parseInt(parts[1]);
            blue = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return 0;
        }
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255)
            return 0;
        if (red == 0 && green == 0 && blue == 0)
            red = green = blue = 1;
        return Color.rgb(red, green, blue);
    }

    private String getElementValue(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return title;
    }

    private String readText(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        StringBuilder res = new StringBuilder();
        while (parser.next() == XmlPullParser.TEXT)
            res.append(parser.getText());
        return res.toString().trim();
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
