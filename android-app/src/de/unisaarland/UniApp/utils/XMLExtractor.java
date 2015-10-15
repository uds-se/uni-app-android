package de.unisaarland.UniApp.utils;


import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public abstract class XMLExtractor<ResultType> implements ContentExtractor<ResultType> {

    @Override
    public ResultType extract(InputStream data) throws ParseException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(data, null);
        } catch (XmlPullParserException e) {
            throw new AssertionError(e);
        }

        try {
            return extractFromXML(parser);
        } catch (XmlPullParserException e) {
            ParseException pe = new ParseException(e.getMessage(), e.getLineNumber());
            pe.setStackTrace(e.getStackTrace());
            throw pe;
        }
    }

    public abstract ResultType extractFromXML(XmlPullParser parser)
            throws IOException, XmlPullParserException, ParseException;

}
