package com.keyboardape.newwestminsteranalyticsapp.downloaders;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.data.DataSet;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Downloads Skytrain Stations data into SQLite Database.
 */
public class DownloadBusStopsAsync extends JsonDownloaderAsync {

    public DownloadBusStopsAsync(SQLiteDatabase db, Callbacks callbacks) {
        super(DataSet.BUS_STOPS, db, callbacks);
    }

    @Override
    protected ContentValues convertToContentValuesOrNull(JSONObject o) {

        try {
            if (o.getString("STATUS").equals("ACTIVE")) {
                JSONArray coordinate = o.getJSONObject("json_geometry").getJSONArray("coordinates");
                ContentValues c = new ContentValues();
                c.put("longitude", coordinate.getDouble(0));
                c.put("latitude", coordinate.getDouble(1));
                c.put("stopNum", o.getInt("BUSSTOPNUM"));
                return c;
            }
        } catch (Exception e) {
            // Failed to parse this specific JSONObject
            Log.e(DownloadBusStopsAsync.class.getSimpleName(), o.toString());
        }
        return null;
    }
}