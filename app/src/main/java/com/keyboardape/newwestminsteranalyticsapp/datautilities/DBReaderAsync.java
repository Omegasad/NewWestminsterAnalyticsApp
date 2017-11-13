package com.keyboardape.newwestminsteranalyticsapp.datautilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Database Reader Async Task.
 */
public class DBReaderAsync extends AsyncTask<Void, Void, Void> {

    private DataManager mDataManager;
    private Callbacks   mCallbacks;
    private String      mSQLQuery;

    /**
     * Constructor.
     * @param callbacks functions
     * @param sqlQuery to be executed
     */
    public DBReaderAsync(Callbacks callbacks, String sqlQuery) {
        mDataManager = DataManager.GetInstance();
        mCallbacks   = callbacks;
        mSQLQuery    = sqlQuery;
    }

    /**
     * Called on main thread before parsing JSON URL.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Work done in background thread.
     * @param aVoid not used
     * @return null
     */
    @Override
    protected Void doInBackground(Void... aVoid) {
        SQLiteDatabase db = mDataManager.getReadableDatabase();
        Cursor cursor = db.rawQuery(mSQLQuery, null);
        mCallbacks.onDBCursorReady(cursor);
        cursor.close();
        db.close();
        return null;
    }

    /**
     * Called on main thread after execution of main task.
     * @param aVoid not used
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mCallbacks.onDBReadComplete();
    }

    /* ********************************************************************************************
     *                                   CALLBACK INTERFACE                                       *
     ******************************************************************************************** */

    /**
     * Callback functions.
     */
    public interface Callbacks {

        /**
         * Called when DB cursor is ready. Runs in background thread.
         * @param cursor DB Cursor
         */
        void onDBCursorReady(Cursor cursor);

        /**
         * Called when database read completes, success or fail.
         */
        void onDBReadComplete();
    }
}