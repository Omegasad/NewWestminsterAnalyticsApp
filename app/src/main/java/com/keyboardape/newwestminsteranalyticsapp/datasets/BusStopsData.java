package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.datautilities.DataManager;
import com.keyboardape.newwestminsteranalyticsapp.parsers.JsonStreamParserAsync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bus Stops Data Set.
 */
public class BusStopsData extends Data {

    private final static String              TABLE_NAME;
    private final static String              DATA_SOURCE_URL;
    private final static DataType            DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;

    static {
        TABLE_NAME       = "bus_stops";
        DATA_SOURCE_URL  = "http://opendata.newwestcity.ca/downloads/bus-stops/BUS_STOPS.json";
        DATA_SET_TYPE    = DataType.BUS_STOPS;

        // Must use LinkedHashMap or other Map that keeps insertion order
        TABLE_COLUMNS = new LinkedHashMap<>();
        TABLE_COLUMNS.put("id",        "INTEGER PRIMARY KEY AUTOINCREMENT");
        TABLE_COLUMNS.put("latitude",  "REAL NOT NULL");
        TABLE_COLUMNS.put("longitude", "REAL NOT NULL");
        TABLE_COLUMNS.put("stopNum",   "INTEGER NOT NULL");
    }

    private DataManager mDataManager;

    /**
     * Constructor.
     */
    public BusStopsData() {
        super(DATA_SET_TYPE ,TABLE_NAME ,TABLE_COLUMNS);
        mDataManager = DataManager.GetInstance();
    }

    /**
     * Returns the total unit or row for the data set.
     * @return Totoal number of units or rows
     */
    @Override
    public long calcTotalUnits() {
        SQLiteDatabase db = mDataManager.getReadableDatabase();
        String queryTotalUnits = "SELECT COUNT(*) FROM " + TABLE_NAME + ";";
        Cursor cursor = db.rawQuery(queryTotalUnits, null);
        long totalUnits = 0;
        if (cursor.moveToFirst()) {
            totalUnits = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        return totalUnits;
    }

    /**
     * Downloads data set into database asynchronously.
     */
    @Override
    protected void downloadDataAsync(final OnDownloadCompleteCallback callback) {
        final SQLiteDatabase db = DataManager.GetInstance().getWritableDatabase();
        new JsonStreamParserAsync(new JsonStreamParserAsync.Callbacks() {
            /**
             * Called every time a JSON object is parsed from URL.
             * RUNS ON BACKGROUND THREAD.
             * @param o JSONObject to be processed
             */
            @Override
            public void onNewJsonObjectFromStream(JSONObject o) {
                try {
                    if (o.getString("STATUS").equals("ACTIVE")) {
                        JSONArray coordinate = o.getJSONObject("json_geometry").getJSONArray("coordinates");
                        ContentValues c = new ContentValues();
                        c.put("longitude", coordinate.getDouble(0));
                        c.put("latitude", coordinate.getDouble(1));
                        c.put("stopNum", o.getInt("BUSSTOPNUM"));
                        db.insert(TABLE_NAME, null, c);
                    }
                } catch (Exception e) {
                    // Failed to parse this specific JSONObject
                    Log.e(BusStopsData.class.getSimpleName(), e.getMessage() + "::" + o.toString());
                }
            }
            /**
             * Called when Json Stream finishes parsing, success or fail.
             * @param isSuccessful true if successfully parsed
             */
            @Override
            public void onJsonStreamParseComplete(boolean isSuccessful) {
                db.close();
                callback.onDownloadComplete(DATA_SET_TYPE, isSuccessful);
            }
        }, DATA_SOURCE_URL).execute();
    }
}
