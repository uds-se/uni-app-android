package de.unisaarland.UniApp.events.model;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/2/13
 * Time: 12:33 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IEventsResultDelegate {
    /*
    * call back method of class who implement this interface will be called when
    * event model list will be populated after parsing the event xml file.
    * */
    public void eventsList(ArrayList<EventsModel> newsList);
}
