package de.unisaarland.UniApp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import java.util.Date;

/**
 * This class implements a general cache for any objects like news lists,
 * news descriptions, mensa plans, etc..
 * It provides means to update content, access cached content, and remove old content.
 */
public class ContentCache {

    private static final String TAG = ContentCache.class.getSimpleName();

    private static final int version = 1;

    private static final String dropTable =
            "DROP TABLE IF EXISTS cache";
    private static final String createTable =
            "CREATE TABLE cache (key TEXT NOT NULL PRIMARY KEY, time INTEGER NOT NULL, data BLOB);" +
            "CREATE INDEX time_idx ON cache (time)";

    private class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name) {
            super(context, name, null, 1, new DatabaseErrorHandler() {
                @Override
                public void onCorruption(SQLiteDatabase db) {
                    clearDatabase(db);
                }
            });
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            clearDatabase(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            clearDatabase(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            clearDatabase(db);
        }
    }

    private final OpenHelper openHelper;
    private final int discardContentAfterSeconds;

    public ContentCache(Context context, String name, int discardContentAfterSeconds) {
        this.openHelper = new OpenHelper(context, name);
        this.discardContentAfterSeconds = discardContentAfterSeconds;
    }

    public void clearDatabase() {
        clearDatabase(getWritableDB());
    }

    private void clearDatabase(SQLiteDatabase db) {
        Log.w(TAG, "(re-)creating database '" + openHelper.getDatabaseName() + "'");
        db.beginTransaction();
        execQueries(db, dropTable);
        execQueries(db, createTable);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void removeOldContent(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        long removeIfOlderThan = now - 1000 * discardContentAfterSeconds;
        long removeIfNewerThan = now + 1000 * discardContentAfterSeconds;
        int deleted = db.delete("cache", "time<? OR time>?", new String[] {
                Long.toString(removeIfOlderThan), Long.toString(removeIfNewerThan)
        });
        Log.w(TAG, "removed "+deleted+" old entries from database '" + openHelper.getDatabaseName() + "'");
    }

    private void execQueries(SQLiteDatabase db, String queries) {
        for (String query: queries.split(";"))
            if (!query.isEmpty())
                db.execSQL(query);
    }

    private SQLiteDatabase getWritableDB() {
        return openHelper.getWritableDatabase();
    }

    private SQLiteDatabase getReadableDB() {
        return openHelper.getReadableDatabase();
    }

    public byte[] getContent(String name) {
        Pair<Date, byte[]> cont = getContentWithAge(name);
        return cont == null ? null : cont.second;
    }

    public Pair<Date, byte[]> getContentWithAge(String name) {
        SQLiteDatabase db = getReadableDB();
        Cursor res = db.query("cache", new String[]{"time", "data"}, "key=?", new String[]{name}, null, null, null, "1");
        if (!res.moveToNext())
            return null;
        long time = res.getLong(0);
        if (Math.abs(System.currentTimeMillis() - time) > 1000*discardContentAfterSeconds) {
            removeOldContent(db);
            return null;
        }
        return new Pair<>(new Date(time), res.getBlob(1));
    }

    public void storeContent(String name, byte[] data) {
        storeContentWithAge(name, data, null);
    }

    /**
     * Store content with explicit age. Use current time if age is null.
     */
    public void storeContentWithAge(String name, byte[] data, Date age) {
        SQLiteDatabase db = getWritableDB();
        removeOldContent(db);
        ContentValues insertValues = new ContentValues();
        insertValues.put("key", name);
        insertValues.put("data", data);
        long time = age == null ? System.currentTimeMillis() : age.getTime();
        insertValues.put("time", time);
        db.replaceOrThrow("cache", null, insertValues);
    }

}
