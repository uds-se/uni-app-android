package de.unisaarland.UniApp.utils;


import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public interface ContentExtractor<ResultType> {

    ResultType extract(InputStream data) throws ParseException, IOException;

}
