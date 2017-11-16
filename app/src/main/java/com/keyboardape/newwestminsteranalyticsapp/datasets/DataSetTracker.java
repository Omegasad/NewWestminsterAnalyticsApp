package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.utilities.DataManager;

public class DataSetTracker {

    public static final String TABLE_NAME = "dataset_tracker";

    public static ContentValues GetStatsOrNull(DataSetType dataSetType) {
        String tableName = dataSetType.getDataSet().getTableName();
        String selectQuery = "SELECT isRequireUpdate, lastUpdated " +
                "FROM " + TABLE_NAME + " WHERE tableName = '" + tableName + "';";

        ContentValues c = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DataManager.GetInstance().getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                c = new ContentValues();
                c.put("isRequireUpdate", cursor.getInt(0) == 1);
                c.put("lastUpdated", cursor.getLong(1));
            }
        } catch (Exception e) {
            // No need to handle, expected if non-existent
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return c;
    }

    public static void SetRequireUpdate(DataSetType dataSetType, boolean isRequireUpdate) {
        String tableName = dataSetType.getDataSet().getTableName();
        String selectQuery = "SELECT tableName, isRequireUpdate, lastUpdated " +
                             "FROM " + TABLE_NAME + " " +
                             "WHERE tableName = '" + tableName + "';";

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DataManager.GetInstance().getWritableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            // Set stats
            ContentValues c = new ContentValues();
            c.put("isRequireUpdate", (isRequireUpdate) ? 1 : 0);
            c.put("lastUpdated", System.currentTimeMillis());

            // Insert or update database row
            if (cursor.getCount() == 0) {
                c.put("tableName", tableName);
                db.insert(TABLE_NAME, null, c);
            } else {
                db.update(TABLE_NAME, c, "tableName = '" + tableName + "'", null);
            }
        } catch (Exception e) {
            Log.e(DataSetTracker.class.getSimpleName(), e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    private DataSetTracker() {
    }
}
