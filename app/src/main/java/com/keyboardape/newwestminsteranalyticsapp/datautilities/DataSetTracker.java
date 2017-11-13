package com.keyboardape.newwestminsteranalyticsapp.datautilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.keyboardape.newwestminsteranalyticsapp.datasets.DataType;

public class DataSetTracker {

    public static final String TABLE_NAME = "tracker";

    /**
     * Returns stats for specified data type.
     * @param dataType to get stats for
     * @return as ContentValues
     */
    public static ContentValues GetStatsOrNull(DataType dataType) {
        ContentValues c = null;
        String tableName = DataManager.GetDataSetOrNull(dataType).getTableName();
        String selectQuery = "SELECT isRequireUpdate, lastUpdated " +
                "FROM " + TABLE_NAME + " WHERE tableName = '" + tableName + "';";
        SQLiteDatabase db = DataManager.GetInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                c = new ContentValues();
                c.put("isRequireUpdate", cursor.getInt(0) == 1);
                c.put("lastUpdated", cursor.getLong(1));
            }
        } catch (Exception e) {
            // No need to handle, expected if non-existent
        } finally {
            cursor.close();
            db.close();
        }
        return c;
    }

    /**
     * Updates or creates stats for specified data type.
     * @param dataType to update stats on
     * @param trackerData tracker stats
     */
    public static void UpdateStats(DataType dataType, TrackerData trackerData) {
        String tableName = DataManager.GetDataSetOrNull(dataType).getTableName();
        String selectQuery = "SELECT tableName, isRequireUpdate, lastUpdated " +
                             "FROM " + TABLE_NAME + " " +
                             "WHERE tableName = '" + tableName + "';";
        SQLiteDatabase db = DataManager.GetInstance().getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Insert/UpdateStats data
        ContentValues c = trackerData.getData();
        c.put("lastUpdated", System.currentTimeMillis());
        if (cursor.getCount() == 0) {
            c.put("tableName", tableName);
            db.insert(TABLE_NAME, null, c);
        } else {
            db.update(TABLE_NAME, c, "tableName = '" + tableName + "'", null);
        }

        cursor.close();
        db.close();
    }

    /* ********************************************************************************************
     *                                         TRACKER DATA CLASS                                 *
     ******************************************************************************************** */

    /**
     * Tracker stats.
     */
    public static class TrackerData {
        private ContentValues mData = new ContentValues();

        /**
         * Returns stats data.
         * @return as ContentValues
         */
        public ContentValues getData() {
            return mData;
        }

        /**
         * Set whether data set requires updating/re-downloading.
         * @param isRequireUpdate boolean
         */
        public void setIsRequireUpdate(boolean isRequireUpdate) {
            int requireUpdate = (isRequireUpdate) ? 1 : 0;
            mData.put("isRequireUpdate", requireUpdate);
        }
    }
}
