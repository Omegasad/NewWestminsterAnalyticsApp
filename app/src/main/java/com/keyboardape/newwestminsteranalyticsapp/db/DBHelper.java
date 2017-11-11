package com.keyboardape.newwestminsteranalyticsapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper. Used whenever reading and writing to database.
 */
public class DBHelper extends SQLiteOpenHelper {

    /** Database Name. */
    private static final String DB_NAME = "datasets.sqlite";

    /** Database Version. */
    private static final int DB_VERSION = 1;

    /**
     * Constructor.
     * @param context of the caller.
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Create database if non-existent.
     * @param db written to
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, 1);
    }

    /**
     * Upgrade database. Called when DB_VERSION changes upwards.
     * @param db to upgrade
     * @param oldVersion of db
     * @param newVersion of db
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    /**
     * Updates database based on oldVersion and newVersion.
     * @param db to update
     * @param oldVersion of db
     * @param newVersion of db
     */
    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 1) {
                db.execSQL(DataSet.TRACKER.SQL_CREATE_TABLE);
                addTableToTracker(db, DataSet.POPULATION_DENSITY.TABLE_NAME);
                addTableToTracker(db, DataSet.SKYTRAIN_STATIONS.TABLE_NAME);
                addTableToTracker(db, DataSet.BUS_STOPS.TABLE_NAME);
                addTableToTracker(db, DataSet.BUSINESS_LICENSES.TABLE_NAME);
                addTableToTracker(db, DataSet.MAJOR_SHOPPINGS.TABLE_NAME);
            }
        } catch (Exception e) {}
    }

    /**
     * Adds a table to be tracked by the tracking table.
     * @param db Writable SQLiteDatabase
     * @param tableName to be tracked
     */
    private void addTableToTracker(SQLiteDatabase db, String tableName) {
        ContentValues c = new ContentValues();
        c.put("tableName", tableName);
        c.put("isNeedUpdate", DBConsts.TRUE);
        c.put("lastUpdated", DBConsts.FALSE);
        db.insert(DataSet.TRACKER.TABLE_NAME, null, c);
    }
}