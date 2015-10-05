package de.unisaarland.UniApp.events.model;

import java.util.ArrayList;
import java.util.List;

public interface IEventsResultDelegate {
    /**
     * call back method of class who implement this interface will be called when
     * event model list will be populated after parsing the event xml file.
     */
    void eventsList(List<EventModel> eventList);

    /**
     * call back method if errors occured during retrieval or parsing of the events XML.
     */
    void onFailure(String message);
}
