package com.keyboardape.newwestminsteranalyticsapp.datautilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.keyboardape.newwestminsteranalyticsapp.datasets.DataType;

public class LayerOptionsTracker {

    public static final String TABLE_NAME = "map_layers";

    /**
     * Return sum of all tables total units.
     * @return total units or -1 if can't be read
     */
    public static long GetSumTotalUnits() {
        String query = "SELECT SUM(totalUnits) FROM " + TABLE_NAME;
        SQLiteDatabase db = DataManager.GetInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        long totalUnits = -1;
        try {
            if (cursor.moveToFirst()) {
                totalUnits = cursor.getLong(0);
            }
        } catch (Exception e) {
            // No need to handle, expected if non-existent
        } finally {
            cursor.close();
            db.close();
        }
        return totalUnits;
    }

    /**
     * Returns stats for specified data type.
     * @param dataType to get stats for
     * @return as ContentValues
     */
    public static ContentValues GetStatsOrNull(DataType dataType) {
        ContentValues c = null;
        String tableName = DataManager.GetDataSetOrNull(dataType).getTableName();
        String selectQuery = "SELECT isAdditive, layerWeight, totalUnits, isShown " +
                "FROM " + TABLE_NAME + " WHERE tableName = '" + tableName + "';";
        SQLiteDatabase db = DataManager.GetInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                c = new ContentValues();
                c.put("isAdditive", cursor.getInt(0) == 1);
                c.put("layerWeight", cursor.getFloat(1));
                c.put("totalUnits", cursor.getLong(2));
                c.put("isShown", cursor.getInt(3) == 1);
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
     * @param layerData tracker stats
     */
    public static void UpdateStats(DataType dataType, LayerData layerData) {
        String tableName = DataManager.GetDataSetOrNull(dataType).getTableName();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE tableName = '" + tableName + "';";
        SQLiteDatabase db = DataManager.GetInstance().getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Insert/UpdateStats data
        ContentValues c = layerData.getData();
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
     *                                        LAYER DATA CLASS                                    *
     ******************************************************************************************** */

    /**
     * Tracker stats.
     */
    public static class LayerData {
        private ContentValues mData = new ContentValues();

        /**
         * Empty constructor.
         */
        public LayerData() {
        }

        /**
         * Initialize with existing data
         * @param data layer options
         */
        public LayerData(ContentValues data) {
            mData = data;
        }

        /**
         * Returns stats data.
         * @return as ContentValues
         */
        public ContentValues getData() {
            return mData;
        }

        /**
         * Set whether data is additive or subtractive when viewed in Heatmaps.
         * @param isShown boolean
         */
        public void setIsShown(boolean isShown) {
            int shown = (isShown) ? 1 : 0;
            mData.put("isShown", shown);
        }

        /**
         * Set whether data is additive or subtractive when viewed in Heatmaps.
         * @param isAdditive boolean
         */
        public void setIsAdditive(boolean isAdditive) {
            int additive = (isAdditive) ? 1 : 0;
            mData.put("isAdditive", additive);
        }

        /**
         * Set layer weight when viewed in Heatmaps.
         * @param layerWeight float
         */
        public void setLayerWeight(float layerWeight) {
            mData.put("layerWeight", layerWeight);
        }

        /**
         * Set total number of units (used in calculation for heatmaps)
         * @param totalUnits long
         */
        public void setTotalUnits(long totalUnits) {
            mData.put("totalUnits", totalUnits);
        }
    }
}
