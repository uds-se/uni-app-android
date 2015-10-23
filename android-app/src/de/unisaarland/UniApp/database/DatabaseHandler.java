package de.unisaarland.UniApp.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

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

import de.unisaarland.UniApp.R;
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

    public List<PointOfInterest> getPOIs(String selection, String[] selectionArgs) {
        List<PointOfInterest> result = new ArrayList<>();
        Cursor cursor = campusQuery("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                        "canshowrightcallout", "website", "color", "lat", "longi", "ID", "categorieID"},
                selection, selectionArgs, null, null);

        while (cursor.moveToNext()) {
            PointOfInterest poi = new PointOfInterest(cursor.getString(0), cursor.getString(1),
                    cursor.getInt(2), cursor.getInt(3), cursor.getString(4), cursor.getInt(5),
                    cursor.getFloat(6), cursor.getFloat(7), cursor.getInt(8), cursor.getInt(9));
            result.add(poi);
        }
        cursor.close();

        return result;
    }

    public List<PointOfInterest> getPOIsForCategoryWithID(int catId){
        return getPOIs("categorieID = ?", new String[]{Integer.toString(catId)});
    }

    public Cursor getAllData() {
        String[] columns = new String[]{"ID as _id","title","subtitle","canshowleftcallout",
                "canshowrightcallout","color","website","lat","longi","ID","categorieID"};
        return campusQuery("pointOfInterest", columns, null, null, null, null);
    }

    public ArrayList<String> getPointsOfInterestPartialMatchedTitles(){
        ArrayList<String> result = new ArrayList<>();
        Cursor cursor = campusQuery("pointOfInterest",new String[]{"title"},null,
                    null,null,null);
        while (cursor.moveToNext()) {
            result.add(cursor.getString(0));
        }
        cursor.close();

        return result;
    }

    public Cursor getCursorPointsOfInterestPartialMatchedForSearchKey(String searchKey){
        String sKeyWithPercAtEnd = searchKey + "%";

        String sKeyWithPerAtBegEnd = "% " + searchKey + "%";

        String[] columns = new String[] {"ID as _id","title","subtitle","canshowleftcallout",
                "canshowrightcallout","color","website","lat","longi","ID","categorieID"};
        String query = "(title LIKE ?) OR (subtitle LIKE ?)  OR (searchkey LIKE ?) OR (title LIKE ?)" +
                " OR (subtitle LIKE ?) OR (searchkey LIKE ?)";
        String[] args = new String[] {sKeyWithPercAtEnd, sKeyWithPercAtEnd, sKeyWithPercAtEnd,
                sKeyWithPerAtBegEnd, sKeyWithPerAtBegEnd, sKeyWithPerAtBegEnd};
        return campusQuery("pointOfInterest", columns, query, args, null, null);
    }

    public List<PointOfInterest> getPOIsForIDs(List<Integer> ids) {
        if (ids.isEmpty()) {
            Log.w(TAG, new NoSuchElementException("empty ids"));
            return Collections.emptyList();
        }

        StringBuilder queryBuilder = new StringBuilder("ID IN (");
        for (int i = 0; i < ids.size(); ++i)
            queryBuilder.append(i == 0 ? "" : ", ").append(Integer.toString(ids.get(i)));
        String query = queryBuilder.append(")").toString();
        return getPOIs(query, null);
    }

    public List<PointOfInterest> getPointsOfInterestForTitle(String title) {
        return getPOIs("title = ? ", new String[]{title});
    }

    public List<Pair<String, Integer>> getAllCategories(){
        List<Pair<String, Integer>> result = new ArrayList<>();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String campus = settings.getString(context.getString(R.string.pref_campus), null);
        Cursor cursor = rawQuery("select distinct categorie.id, categorie.title from categorie, pointOfInterest "+
                "where categorie.iD = pointOfInterest.categorieID and pointOfInterest.campus = ?",
                new String[]{campus});

        while (cursor.moveToNext()) {
            result.add(new Pair<>(cursor.getString(1), cursor.getInt(0)));
        }
        cursor.close();
        return result;
    }

    private SQLiteDatabase getDatabase() {
        if (database != null && database.isOpen())
            return database;

        String versionName;

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
            if (!outFile.getParentFile().mkdirs())
                throw new AssertionError("cannot create directory '" + outFile.getParentFile() + "'");

        if (outFile.exists() && !outFile.delete())
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
        boolean readonly = outFile.setReadOnly();
        if (!readonly)
            Log.w(TAG, "Database file '" + outFile + "' cannot be set read-only.");

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
                              String[] selectionArgs, String groupBy, String having) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String campus = settings.getString(context.getString(R.string.pref_campus), null);
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
