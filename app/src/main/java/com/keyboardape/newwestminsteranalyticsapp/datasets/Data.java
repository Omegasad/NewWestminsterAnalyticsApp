package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.keyboardape.newwestminsteranalyticsapp.datautilities.DataManager;
import com.keyboardape.newwestminsteranalyticsapp.datautilities.DataSetTracker;

import java.util.Map;

/**
 * Data Set.
 */
public abstract class Data {

    private final DataManager           mDataManager;
    private final DataType              mDataType;
    private final String                mTableName;
    private final Map<String, String>   mTableColumns;

    /**
     * Constructor.
     * @param dataType type of data set
     * @param tableName in database
     * @param tableColumns in database
     */
    public Data(DataType dataType,
                String               tableName,
                Map<String, String>  tableColumns) {

        mDataManager = DataManager.GetInstance();
        mDataType = dataType;
        mTableName = tableName;
        mTableColumns = tableColumns;
    }

    /**
     * Returns the table name of the data set;
     * @return table name as a String
     */
    public String getTableName() {
        return mTableName;
    }

    /* ********************************************************************************************
     *                                  DOWNLOAD / UPGRADE DATA SET                               *
     ******************************************************************************************** */

    /**
     * Returns true if data set requires UpdateStats.
     * @return true if data set requires UpdateStats.
     */
    public boolean isRequireUpdate() {
        ContentValues c = DataSetTracker.GetStatsOrNull(mDataType);
        if (c == null) {
            return true;
        }
        return (boolean) c.get("isRequireUpdate");
    }

    /**
     * Updates data asynchronously.
     */
    public void updateDataAsync(final OnDownloadCompleteCallback callback) {
        recreateTable();
        downloadDataAsync(new OnDownloadCompleteCallback() {
            @Override
            public void onDownloadComplete(DataType dataType, boolean isSuccessful) {

                // Update DataSetTracker
                DataSetTracker.TrackerData data = new DataSetTracker.TrackerData();
                data.setIsRequireUpdate(!isSuccessful);
                DataSetTracker.UpdateStats(mDataType, data);

                // Update MapLayerOptions


                callback.onDownloadComplete(dataType, isSuccessful);
            }
        });
    }

    /**
     * Returns the total unit or row for the data set. (e.g. number of residents for population density)
     * @return Totoal number of units or rows
     */
    abstract public long calcTotalUnits();

    /**
     * Downloads data set into database asynchronously.
     */
    abstract protected void downloadDataAsync(OnDownloadCompleteCallback callback);

    /**
     * Delete and re-create database table for this data set.
     */
    private void recreateTable() {
        SQLiteDatabase db = mDataManager.getWritableDatabase();
        String csvColumnNamesWithAttributes = concatenateToCSV(mTableColumns);
        String queryDelete = "DROP TABLE IF EXISTS " + mTableName + ";";
        String queryCreate = "CREATE TABLE IF NOT EXISTS " + mTableName + "(" + csvColumnNamesWithAttributes + ");";
        db.execSQL(queryDelete);
        db.execSQL(queryCreate);
        db.close();
    }

    /**
     * Concatenates a Map into a comma separated value string.
     * @param map to be concatenated
     * @return CSV of Map as a String
     */
    private String concatenateToCSV(Map<String, String> map) {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            str.append(entry.getKey())
                    .append(' ')
                    .append(entry.getValue())
                    .append(',');
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    /* ********************************************************************************************
     *                                   CALLBACK INTERFACE                                       *
     ******************************************************************************************** */

    /**
     * Callback function when download completes.
     */
    public interface OnDownloadCompleteCallback {

        /**
         * Called when data is downloaded.
         * @param dataType downloaded
         * @param isSuccessful true if successful
         */
        void onDownloadComplete(DataType dataType, boolean isSuccessful);
    }
}
