package de.unisaarland.UniApp.utils;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

public interface XMLExtractor<ResultType> {

    ResultType extractFromXML(XmlPullParser parser) throws IOException, XmlPullParserException, ParseException;

}
