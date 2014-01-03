package de.unisaarland.UniApp.news.model;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/1/13
 * Time: 12:29 AM
 * To change this template use File | Settings | File Templates.
 */
public interface INewsResultDelegate {
    /*
    * call back method of class who implement this interface will be called when
    * news model list will be populated after parsing the news xml file.
    * */
    public void newsList(ArrayList<NewsModel> newsList);
}
