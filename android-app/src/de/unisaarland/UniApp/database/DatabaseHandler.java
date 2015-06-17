package de.unisaarland.UniApp.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

import de.unisaarland.UniApp.SettingsActivity;
import de.unisaarland.UniApp.bus.model.PointOfInterest;


public class DatabaseHandler {
    private static final String TAG = DatabaseHandler.class.getSimpleName();

    private SQLiteDatabase database = null;
    private final Context context;
    // path where the data base should be copied from the assets folder on first run and name of the database used in project
    private static final String DATABASE_NAME = "pointOfInterest.sqlite3";
    private static final String ASSET_DB_PATH = "databases/";
    private final File DB_PATH;

    public DatabaseHandler(Context context) {
        this.context = context;
        this.DB_PATH = new File(context.getApplicationInfo().dataDir, "databases");
    }

    public ArrayList<PointOfInterest> getPointsOfInterestForCategoryWithID(int ID){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                        "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, "categorieID = ?",
                new String[]{Integer.toString(ID)}, null, null, null);

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

        return result;
    }

    public ArrayList<String> getAllCategoryTitles(){
        ArrayList<String> result = new ArrayList<String>();
        // cursor = database.query("categorie",new String[]{"title"},null,null,null,null,null);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String campus = settings.getString(SettingsActivity.KEY_CAMPUS_CHOOSER, "saar");
        Cursor cursor = rawQuery("select categorie.title from categorie, pointOfInterest where categorie.iD = pointOfInterest.categorieID and pointOfInterest.campus = ? group by categorie.title", new String[]{campus});
        while (cursor.moveToNext())
            result.add(cursor.getString(0));
        cursor.close();
        return result;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestPartialMatched(){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                        "canshowrightcallout", "color", "website", "lat", "longi", "ID", "categorieID"}, null,
                null, null, null, null);

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
        return result;
    }

    public Cursor getAllData() {
        Cursor cursor =  campusQuery("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                        "canshowrightcallout","color","website","lat","longi","ID","categorieID"},null,
                null,null,null,null);
        String[] columns = new String[] {"_id","title","subtitle","canshowleftcallout",
                "canshowrightcallout","color","website","lat","longi","ID","categorieID"};
        String[] temp = new String[11];
        MatrixCursor mcursor = new MatrixCursor(columns);

        while (cursor.moveToNext()) {
            temp[0] = "0";
            for (int i = 0; i < 10; ++i)
                temp[i+1] = cursor.getString(i);
            mcursor.addRow(temp);
        }
        cursor.close();

        return mcursor;
    }

    public ArrayList<String> getPointsOfInterestPartialMatchedTitles(){
        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = campusQuery("pointOfInterest",new String[]{"title"},null,
                    null,null,null,null);
        while (cursor.moveToNext()) {
            result.add(cursor.getString(0));
        }
        cursor.close();

        return result;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestPartialMatchedForSearchKey(String searchKey){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        String sKeyWithPercAtEnd = searchKey + "%";

        String sKeyWithPerAtBegEnd = "% " + searchKey + "%";

        Cursor cursor =campusQuery("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                        "canshowrightcallout","color","website","lat","longi","ID","categorieID"},
                "(title LIKE ?) OR (subtitle LIKE ?)  OR (searchkey LIKE ?) OR ( title LIKE ? ) OR (subtitle LIKE ?)" +
                        "  OR (searchkey LIKE ?)",
                new String[]{sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd},null,null,"title ASC");

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
        String[] columns = new String[] {"_id","title","subtitle","canshowleftcallout",
                "canshowrightcallout","color","website","lat","longi","ID","categorieID"};
        Object[] temp = new Object[11];
        MatrixCursor mcursor = new MatrixCursor(columns);
        while (cursor.moveToNext()) {
            temp[0] = "0";
            for (int i = 0; i < 9; ++i)
                temp[i+1] = cursor.getString(i);
            for (int i = 9; i < 11; ++i)
                temp[i+1] = cursor.getInt(i);
            mcursor.addRow(temp);
        }
        cursor.close();

        return mcursor;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestForIDs(ArrayList<Integer> ids) {
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        if (ids.isEmpty()) {
            Log.w(TAG, new NoSuchElementException("empty ids"));
            return result;
        }

        StringBuilder queryBuilder = new StringBuilder("ID IN (");
        for (int i = 0; i < ids.size(); ++i)
            queryBuilder.append(i == 0 ? "" : ", ").append(Integer.toString(ids.get(i)));
        String query = queryBuilder.append(")").toString();
        Cursor cursor = campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, query,
                null, null, null, null);

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

        return result;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestForTitle(String title) {
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, "title = ? ", new String[]{title}, null, null, null);
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
        return result;
    }

    public ArrayList<Integer> getAllCategoryIDs(){
        ArrayList<Integer> result = new ArrayList<Integer>();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String campus = settings.getString(SettingsActivity.KEY_CAMPUS_CHOOSER, "saar");
        Cursor cursor = rawQuery("select categorie.id from categorie, pointOfInterest where categorie.iD = pointOfInterest.categorieID and pointOfInterest.campus = ? group by categorie.title", new String[]{campus});
        while (cursor.moveToNext())
            result.add(cursor.getInt(0));
        cursor.close();
        return result;
    }

    private SQLiteDatabase getDatabase() {
        if (database != null && database.isOpen())
            return database;

        String versionName = "0";

        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            AssertionError err = new AssertionError("Cannot get package version name: " + e);
            err.setStackTrace(e.getStackTrace());
            throw err;
        }
        String dbVersion = PreferenceManager.getDefaultSharedPreferences(context).getString("db-version", "-");

        if (!versionName.equals(dbVersion)) {
            Log.w(TAG, "Database version (" + dbVersion + ") does not match app version (" + versionName + ")");
            copyDatabaseToDataDir();
            SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
            e.putString("db-version", versionName);
            e.commit();
        }

        File dbPath = new File(DB_PATH, DATABASE_NAME);
        if (!dbPath.exists()) {
            Log.w(TAG, "Database file does not exist.");
            copyDatabaseToDataDir();
        }

        try {
            Log.i(TAG, "Opening database.");
            database = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.w(TAG, "Error opening database", e);
            copyDatabaseToDataDir();
            try {
                database = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            } catch (SQLiteException e2) {
                Log.e(TAG, "Still cannot open database after a fresh copy!!", e2);
            }
        }

        return database;
    }

    private void copyDatabaseToDataDir() {
        Log.w(TAG, "Copying database from assets to data dir");
        close();
        try {
            InputStream myInput = context.getAssets().open(ASSET_DB_PATH + DATABASE_NAME);

            File outFile = new File(DB_PATH, DATABASE_NAME);
            if (!outFile.getParentFile().exists())
                outFile.getParentFile().mkdirs();
            OutputStream outFileStr = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                outFileStr.write(buffer, 0, length);
            }

            outFileStr.close();
            myInput.close();
            close();
        } catch (IOException e) {
            Log.e(TAG, "IOException when trying to copy the database to data dir.");
            AssertionError err = new AssertionError("Error copying database to data dir: " + e);
            err.setStackTrace(e.getStackTrace());
            throw err;
        }
    }

    // close current instance of database.
    public synchronized void close() {
        try {
            if (database != null)
                database.close();
        } finally {
            database = null;
        }
    }


    // This method queries the database for entries regarding the campus which was selected in the settings
    public Cursor campusQuery(String table, String[] columns, String selection,
                              String[] selectionArgs, String groupBy, String having,
                              String orderBy) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String campus = settings.getString(SettingsActivity.KEY_CAMPUS_CHOOSER, "saar");
        selection = selection == null ? "campus = ?" : "(" +selection + ") AND (campus = ?)";
        selectionArgs = selectionArgs == null ? new String[1]
                : Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
        selectionArgs[selectionArgs.length - 1] = campus;

        try {
            return getDatabase().query(table, columns, selection, selectionArgs, groupBy, having, "title");
        } catch (SQLiteException e) {
            Log.e(TAG, "Error on query", e);
            copyDatabaseToDataDir();
            // now retry without catching any sqlite errors
            return getDatabase().query(table, columns, selection, selectionArgs, groupBy, having, "title");
        }
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        try {
            return getDatabase().rawQuery(sql, selectionArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error on query", e);
            copyDatabaseToDataDir();
            // now retry without catching any sqlite errors
            return getDatabase().rawQuery(sql, selectionArgs);
        }
    }
}
