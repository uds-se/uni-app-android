package de.unisaarland.UniApp.news.model;

import java.util.ArrayList;
import java.util.List;


public interface INewsResultDelegate {
    /*
    * call back method of class who implement this interface will be called when
    * news model list will be populated after parsing the news xml file.
    * */
    void newsList(List<NewsModel> newsList);

    void onFailure(String message);
}
