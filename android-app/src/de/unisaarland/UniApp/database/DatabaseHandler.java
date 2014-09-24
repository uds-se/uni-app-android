package de.unisaarland.UniApp.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import de.unisaarland.UniApp.SettingsActivity;
import de.unisaarland.UniApp.bus.model.PointOfInterest;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/2/13
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseHandler extends SQLiteOpenHelper{
    private SQLiteDatabase database = null;
    private Context context = null;
    // path where the data base dhould be copied from the assets folder on first run and name of the database used in project
    private final String DATABASE_NAME = "pointOfInterest.sqlite3";
    private final String DB_PATH = "/data/data/de.unisaarland.UniApp/databases/";


    public ArrayList<PointOfInterest> getPointsOfInterestForCategoryWithID(int ID){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null ;
        try{
            cursor =campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                            "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, "categorieID = ?",
                    new String[]{Integer.toString(ID)}, null, null, null);
            if(cursor!=null)
            {
                while (cursor.moveToNext()) {
                    PointOfInterest poi = new PointOfInterest();
                    poi.setTitle(cursor.getString(0));
                    poi.setSubtitle(cursor.getString(1)) ;
                    poi.setCanShowLeftCallOut(cursor.getInt(2));
                    poi.setCanShowRightCallOut(cursor.getInt(3));
                    poi.setColor(cursor.getInt(4));
                    poi.setWebsite(cursor.getString(5));
                    poi.setLatitude(cursor.getFloat(6));
                    poi.setLongitude(cursor.getFloat(7));
                    poi.setID(cursor.getInt(8));
                    result.add(poi);
                }
                cursor.close();
            }
        }catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<String> getAllCategoryTitles(){
        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = null ;
        try{
            cursor = campusQuery("categorie",new String[]{"title"},null,null,null,null,null);
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0));
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestPartialMatched(){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null ;
        try{
            cursor =campusQuery("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                    "canshowrightcallout","color","website","lat","longi","ID","categorieID"},null,
                    null,null,null,null);
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    PointOfInterest poi = new PointOfInterest();
                    poi.setTitle(cursor.getString(0));
                    poi.setSubtitle(cursor.getString(1)) ;
                    poi.setCanShowLeftCallOut(cursor.getInt(2));
                    poi.setCanShowRightCallOut(cursor.getInt(3));
                    poi.setColor(cursor.getInt(4));
                    poi.setWebsite(cursor.getString(5));
                    poi.setLatitude(cursor.getFloat(6));
                    poi.setLongitude(cursor.getFloat(7));
                    poi.setID(cursor.getInt(8));
                    poi.setCategoryID(cursor.getInt(9));
                    result.add(poi);
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public Cursor getAllData(){

        Cursor cursor =  campusQuery("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                        "canshowrightcallout","color","website","lat","longi","ID","categorieID"},null,
                null,null,null,null);
        String[] columns = new String[] {"_id","title","subtitle","canshowleftcallout",
                "canshowrightcallout","color","website","lat","longi","ID","categorieID"};
        int i = 0;
        String[] temp = new String[11];
        MatrixCursor mcursor = new MatrixCursor(columns);
        if(cursor!=null) {
            while (cursor.moveToNext()) {
                temp[0] = Integer.toString(i);
                temp[1] = cursor.getString(0);
                temp[2] = cursor.getString(1);
                temp[3] = cursor.getString(2);
                temp[4] = cursor.getString(3);
                temp[5] = cursor.getString(4);
                temp[6] = cursor.getString(5);
                temp[7] = cursor.getString(6);
                temp[8] = cursor.getString(7);
                temp[9] = cursor.getString(8);
                temp[10] = cursor.getString(9);
                mcursor.addRow(temp);
            }
            cursor.close();
        }

        return mcursor;
    }

    public ArrayList<String> getPointsOfInterestPartialMatchedTitles(){
        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = null ;
        try{
            cursor =campusQuery("pointOfInterest",new String[]{"title"},null,
                    null,null,null,null);
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0));
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestPartialMatchedForSearchKey(String searchKey){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null;
        String sKeyWithPercAtEnd = searchKey + "%";

        String sKeyWithPerAtBegEnd = "% " + searchKey + "%";

        cursor =campusQuery("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                        "canshowrightcallout","color","website","lat","longi","ID","categorieID"},
                "(title LIKE ?) OR (subtitle LIKE ?)  OR (searchkey LIKE ?) OR ( title LIKE ? ) OR (subtitle LIKE ?)" +
                        "  OR (searchkey LIKE ?)",
                new String[]{sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd},null,null,"title ASC");
        if(cursor!=null) {
            try {

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        PointOfInterest poi = new PointOfInterest();
                        poi.setTitle(cursor.getString(0));
                        poi.setSubtitle(cursor.getString(1));
                        poi.setCanShowLeftCallOut(cursor.getInt(2));
                        poi.setCanShowRightCallOut(cursor.getInt(3));
                        poi.setColor(cursor.getInt(4));
                        poi.setWebsite(cursor.getString(5));
                        poi.setLatitude(cursor.getFloat(6));
                        poi.setLongitude(cursor.getFloat(7));
                        poi.setID(cursor.getInt(8));
                        poi.setCategoryID(cursor.getInt(9));
                        result.add(poi);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e("MyTag", e.getMessage());
                return null;
            }
        }
        return result;
    }



    public Cursor getCursorPointsOfInterestPartialMatchedForSearchKey(String searchKey){
        String sKeyWithPercAtEnd = searchKey + "%";

        String sKeyWithPerAtBegEnd = "% " + searchKey + "%";

        Cursor cursor =campusQuery("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                        "canshowrightcallout","color","website","lat","longi","ID","categorieID"},
                "(title LIKE ?) OR (subtitle LIKE ?)  OR (searchkey LIKE ?) OR ( title LIKE ? ) OR (subtitle LIKE ?)" +
                        "  OR (searchkey LIKE ?)",
                new String[]{sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd},null,null,"title ASC");
        int i = 0;
        String[] columns = new String[] {"_id","title","subtitle","canshowleftcallout",
                "canshowrightcallout","color","website","lat","longi","ID","categorieID"};
        Object[] temp = new Object[11];
        MatrixCursor mcursor = new MatrixCursor(columns);
        if(cursor!=null) {
            while (cursor.moveToNext()) {
                temp[0] = Integer.toString(i);
                temp[1] = cursor.getString(0);
                temp[2] = cursor.getString(1);
                temp[3] = cursor.getString(2);
                temp[4] = cursor.getString(3);
                temp[5] = cursor.getString(4);
                temp[6] = cursor.getString(5);
                temp[7] = cursor.getString(6);
                temp[8] = cursor.getString(7);
                temp[9] = cursor.getInt(8);
                temp[10] = cursor.getInt(9);
                mcursor.addRow(temp);
            }
            cursor.close();
        }

        return mcursor;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestForIDs(ArrayList<Integer> ids) {
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null;
        if (ids.size() >= 1) {
            for (int i = 0; i < ids.size(); i++) {
                String idList = String.format("%d", ids.get(i));
                try {
                    cursor = campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                            "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, "ID = ?",
                            new String[]{idList}, null, null, null);

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            PointOfInterest poi = new PointOfInterest();
                            poi.setTitle(cursor.getString(0));
                            poi.setSubtitle(cursor.getString(1));
                            poi.setCanShowLeftCallOut(cursor.getInt(2));
                            poi.setCanShowRightCallOut(cursor.getInt(3));
                            poi.setColor(cursor.getInt(4));
                            poi.setWebsite(cursor.getString(5));
                            poi.setLatitude(cursor.getFloat(6));
                            poi.setLongitude(cursor.getFloat(7));
                            poi.setID(cursor.getInt(8));
                            result.add(poi);
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e("MyTag", e.getMessage());
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestForTitle(String title) {
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null;
        try {
            cursor = campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                    "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, "title = ? ",new String[]{title}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    PointOfInterest poi = new PointOfInterest();
                    poi.setTitle(cursor.getString(0));
                    poi.setSubtitle(cursor.getString(1));
                    poi.setCanShowLeftCallOut(cursor.getInt(2));
                    poi.setCanShowRightCallOut(cursor.getInt(3));
                    poi.setColor(cursor.getInt(4));
                    poi.setWebsite(cursor.getString(5));
                    poi.setLatitude(cursor.getFloat(6));
                    poi.setLongitude(cursor.getFloat(7));
                    poi.setID(cursor.getInt(8));
                    result.add(poi);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("MyTag", e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<Integer> getAllCategoryIDs(){
        ArrayList<Integer> result = new ArrayList<Integer>();
        Cursor cursor = null ;
        try{
            cursor =campusQuery("categorie",new String[]{"ID"},null,null,null,null,null);
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getInt(0));
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    /*
    * copy the database from assets folder to the path given above if not already copied.
    * */
    public void crateDatabase() throws IOException {
        boolean vtVarMi = isDatabaseExist();

        if (!vtVarMi) {
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DATABASE_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DATABASE_NAME;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private boolean isDatabaseExist() {
        SQLiteDatabase control = null;

        try {
            String myPath = DB_PATH + DATABASE_NAME;
            control = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            control = null;
        }

        if (control != null) {
            control.close();
        }
        return control != null ? true : false;
    }

    private boolean openDB(){
        if (database != null && database.isOpen() ) {
            return true;
        }
        else{
            try{
                database =SQLiteDatabase.openDatabase(DB_PATH+DATABASE_NAME, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                        //SQLiteDatabase.NO_LOCALIZED_COLLATORS
            }catch (Exception e){
                Log.e("MyTag",e.getMessage());
                return false;
            }
        }
        return true;
    }

    public DatabaseHandler(Context context) {
        super(context,"pointOfInterest.sqlite3",null,1);
        this.context = context;
        try {
            crateDatabase();
            openDB();
        } catch (IOException e) {
            Log.e("MyTag",e.getMessage());
        }
    }

    // close current instance of database.
    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    // This method queries the database for entries regarding the campus which was selecten in the settings
    public Cursor campusQuery(String table, String[] columns, String selection,
                              String[] selectionArgs, String groupBy, String having,
                              String orderBy){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String campus = settings.getString(SettingsActivity.KEY_CAMPUS_CHOOSER, "saar");
        selection =  selection + " AND campus = ?" ;
        //Appending search term
        ArrayList<String> tempList= new ArrayList<String>(Arrays.asList(selectionArgs));
        tempList.add(campus);
        selectionArgs = tempList.toArray(new String[]{});
        return database.query(table,columns,selection,selectionArgs,groupBy,having,orderBy);
    }
}
