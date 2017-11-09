package com.keyboardape.newwestminsteranalyticsapp.downloaders;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.keyboardape.newwestminsteranalyticsapp.data.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.downloaders.JsonStreamParserAsync;

import org.json.JSONObject;

/**
 * Downloads data into SQLite Database using JsonStreamParserAsync.
 */
public abstract class JsonDownloaderAsync extends JsonStreamParserAsync {

    public interface Callbacks {
        void onDownloadSuccess(DataSet dataSet);
        void onDownloadFailed(DataSet dataSet);
    }

    /** Data set. */
    private DataSet mDataSet;

    /** Writable SQLite Database. */
    private SQLiteDatabase mDB;

    /** Callback functions for when download succeeds or fails. */
    private Callbacks mCallbacks;

    /**
     * Constructor.
     * @param dataSet to data
     * @param db SQLiteDatabase that is writable
     * @param callbacks functions when download succeeds or fails
     */
    public JsonDownloaderAsync(DataSet dataSet, SQLiteDatabase db, Callbacks callbacks) {
        super(dataSet.DATA_URL);

        if (db.isReadOnly()) {
            throw new RuntimeException("Database must have write access!");
        }
        mDataSet = dataSet;
        mDB = db;
        mCallbacks = callbacks;
    }

    /**
     * Called on main thread before parsing JSON URL.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDB.execSQL(mDataSet.SQL_DELETE_TABLE);
        mDB.execSQL(mDataSet.SQL_CREATE_TABLE);
    }

    /**
     * Called if stream can not be loaded.
     * RUNS ON MAIN/UI THREAD.
     */
    protected void onParseFailed() {
        mCallbacks.onDownloadFailed(mDataSet);
    }

    /**
     * Called if stream has been parsed and processed successfully.
     * RUNS ON MAIN/UI THREAD.
     */
    protected void onParseSuccess() {
        mDB.execSQL(mDataSet.getSQLToUpdateTracker());
        mCallbacks.onDownloadSuccess(mDataSet);
    }

    /**
     * Called every time a JSON object is parsed from URL.
     * RUNS ON BACKGROUND THREAD.
     * @param o JSONObject to be processed
     */
    protected void processJsonObject(JSONObject o) {
        ContentValues c = convertToContentValuesOrNull(o);
        if (c != null) {
            mDB.insert(mDataSet.TABLE_NAME, null, c);
        }
    }

    /**
     * Converts JSONObject to ContentValues for inserting into database.
     * @param o JSONObject to be converted
     * @return ContentValues or null
     */
    abstract protected ContentValues convertToContentValuesOrNull(JSONObject o);
}