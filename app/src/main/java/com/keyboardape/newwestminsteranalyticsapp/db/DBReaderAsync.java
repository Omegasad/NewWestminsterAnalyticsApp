package com.keyboardape.newwestminsteranalyticsapp.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.keyboardape.newwestminsteranalyticsapp.data.DataSet;

public abstract class DBReaderAsync extends AsyncTask<Void, Void, Void> {

    private SQLiteDatabase mDB;
    private DataSet mDataSet;
    private Cursor mCursor;

    public DBReaderAsync(SQLiteDatabase db, DataSet dataSet) {
        mDB = db;
        mDataSet = dataSet;
        mCursor = null;
    }

    /**
     * Called on main thread before parsing JSON URL.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**3
     * Work done in background thread.
     * @param aVoid not used
     * @return null
     */
    @Override
    protected Void doInBackground(Void... aVoid) {
        mCursor = mDB.rawQuery(mDataSet.SQL_GET_TABLE, null);
        readFromCursor(mCursor);
        return null;
    }

    /**
     * Called on main thread after execution of main task.
     * @param aVoid not used
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mCursor != null) {
            mCursor.close();
        }
        onReadFinished();
    }

    /**
     * Processes the cursor.
     * @param cursor from a readable SQLiteDatabase
     */
    abstract protected void readFromCursor(Cursor cursor);

    /**
     * Called when done reading, success or fail.
     */
    abstract protected void onReadFinished();
}