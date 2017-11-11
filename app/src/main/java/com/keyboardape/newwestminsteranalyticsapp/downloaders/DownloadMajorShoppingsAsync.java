package com.keyboardape.newwestminsteranalyticsapp.downloaders;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.db.DataSet;

import org.json.JSONObject;

/**
 * Downloads Major Shoppings data into SQLite Database.
 */
public class DownloadMajorShoppingsAsync extends JsonDownloaderAsync {

    /**
     * Constructor.
     * @param db to download into
     * @param callbacks functions
     */
    public DownloadMajorShoppingsAsync(SQLiteDatabase db, Callbacks callbacks) {
        super(DataSet.MAJOR_SHOPPINGS, db, callbacks);
    }

    /**
     * Converts JSONObject into ContentValues while discarding unwanted data.
     * @param o JSONObject to be converted
     * @return ContentValues
     */
    @Override
    protected ContentValues convertToContentValuesOrNull(JSONObject o) {
        try {
            ContentValues c = new ContentValues();
            c.put("buildingName", o.getString("BLDGNAM"));
            c.put("longitude", parseToDoubleOrZero("X"));
            c.put("latitude", parseToDoubleOrZero("Y"));
            return c;
        } catch (Exception e) {
            // Failed to parse this specific JSONObject
            Log.e(DownloadMajorShoppingsAsync.class.getSimpleName(), o.toString());
        }
        return null;
    }

    /*
     * Parses a string to double, or returns 0.
     * @param double to be parsed
     * @return double value of string or 0
     */
    private double parseToDoubleOrZero(String aDouble) {
        try {
            return Double.parseDouble(aDouble);
        } catch (Exception e) {}
        return 0.;
    }
}