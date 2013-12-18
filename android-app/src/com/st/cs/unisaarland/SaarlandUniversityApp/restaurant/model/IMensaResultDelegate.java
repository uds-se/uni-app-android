package com.st.cs.unisaarland.SaarlandUniversityApp.restaurant.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/6/13
 * Time: 12:53 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IMensaResultDelegate {
    public void mensaItemsList(HashMap<String,ArrayList<MensaItem>> daysDictionary);
}
