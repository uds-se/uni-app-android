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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import de.unisaarland.UniApp.SettingsActivity;
import de.unisaarland.UniApp.bus.model.PointOfInterest;


public class DatabaseHandler {
    private static final String TAG = DatabaseHandler.class.getSimpleName();

    private SQLiteDatabase database = null;
    private final Context context;
    // path where the data base should be copied from the assets folder on first run and name of the database used in project
    private static final String DATABASE_NAME = "pointOfInterest.sqlite";
    private static final String ASSET_DB_PATH = "databases/pointOfInterest.sql";
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

        String[] columns = new String[] {"_id","title","subtitle","canshowleftcallout",
                "canshowrightcallout","color","website","lat","longi","ID","categorieID"};
        Cursor cursor = campusQuery("pointOfInterest", Arrays.copyOfRange(columns, 1, columns.length),
                "(title LIKE ?) OR (subtitle LIKE ?)  OR (searchkey LIKE ?) OR ( title LIKE ? ) OR (subtitle LIKE ?)" +
                        "  OR (searchkey LIKE ?)",
                new String[]{sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd},null,null,"title ASC");

        Object[] temp = new Object[11];
        MatrixCursor mcursor = new MatrixCursor(columns);
        while (cursor.moveToNext()) {
            temp[0] = "0";
            for (int i = 0; i < 8; ++i)
                temp[i+1] = cursor.getString(i);
            for (int i = 8; i < 10; ++i)
                temp[i+1] = cursor.getInt(i);
            mcursor.addRow(temp);
        }
        cursor.close();

        return mcursor;
    }

    public List<PointOfInterest> getPointsOfInterestForIDs(List<Integer> ids) {
        if (ids.isEmpty()) {
            Log.w(TAG, new NoSuchElementException("empty ids"));
            return Collections.emptyList();
        }

        StringBuilder queryBuilder = new StringBuilder("ID IN (");
        for (int i = 0; i < ids.size(); ++i)
            queryBuilder.append(i == 0 ? "" : ", ").append(Integer.toString(ids.get(i)));
        String query = queryBuilder.append(")").toString();
        Cursor cursor = campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                        "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, query,
                null, null, null, null);

        List<PointOfInterest> result = new ArrayList<PointOfInterest>();
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
            recreateDatabaseFile();
            SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
            e.putString("db-version", versionName);
            e.commit();
        }

        File dbPath = new File(DB_PATH, DATABASE_NAME);
        if (!dbPath.exists()) {
            Log.w(TAG, "Database file does not exist.");
            recreateDatabaseFile();
        }

        try {
            Log.i(TAG, "Opening database.");
            database = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.w(TAG, "Error opening database", e);
            recreateDatabaseFile();
            // if this throws an exception (on the newly created DB), just pass it on.
            // then something is really broken.
            database = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        }

        return database;
    }

    private void recreateDatabaseFile() {
        close();
        Log.w(TAG, "Recreating database from asset SQL");

        File outFile = new File(DB_PATH, DATABASE_NAME);
        if (!outFile.getParentFile().exists())
            outFile.getParentFile().mkdirs();
        outFile.delete();
        if (outFile.exists())
            throw new AssertionError("Cannot remote old DB file.");

        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(outFile, null);
        try {
            InputStream sqlInput = context.getAssets().open(ASSET_DB_PATH);
            BufferedReader sqlReader = new BufferedReader(new InputStreamReader(sqlInput));
            // statements need to be separated in individual lines, and end in a semicolon.
            // they can, however, spread several lines. they are joined here.
            StringBuilder statement = new StringBuilder();
            String line;
            while ((line = sqlReader.readLine()) != null) {
                if (!line.endsWith(";")) {
                    statement.append(line).append(" ");
                    continue;
                }
                if (statement.length() != 0) {
                    line = statement.append(line).toString();
                    statement.setLength(0);
                }
                db.execSQL(line);
            }
            sqlReader.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException when trying to read the database", e);
            AssertionError err = new AssertionError("Error reading the database: " + e);
            err.setStackTrace(e.getStackTrace());
            throw err;
        }
        db.close();

        Log.w(TAG, "Finished creating database from asset SQL.");
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
            recreateDatabaseFile();
            // now retry without catching any sqlite errors
            return getDatabase().query(table, columns, selection, selectionArgs, groupBy, having, "title");
        }
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        try {
            return getDatabase().rawQuery(sql, selectionArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error on query", e);
            recreateDatabaseFile();
            // now retry without catching any sqlite errors
            return getDatabase().rawQuery(sql, selectionArgs);
        }
    }
}
