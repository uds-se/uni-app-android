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
    public void newsList(ArrayList<NewsModel> newsList);
}
