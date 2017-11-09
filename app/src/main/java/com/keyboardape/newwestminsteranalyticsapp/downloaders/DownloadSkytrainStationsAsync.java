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
public class DownloadSkytrainStationsAsync extends JsonDownloaderAsync {

    public DownloadSkytrainStationsAsync(SQLiteDatabase db, Callbacks callbacks) {
        super(DataSet.SKYTRAIN_STATIONS, db, callbacks);
    }

    @Override
    protected ContentValues convertToContentValuesOrNull(JSONObject o) {

        try {
            JSONObject geoJson = o.getJSONObject("json_geometry");
            JSONArray coordinates = geoJson.getJSONArray("coordinates").getJSONArray(0);
            String stationName = o.getString("NAME");

            // on a blue moon, we'd get a MultiPolygon
            if (geoJson.getString("type").equals("MultiPolygon")) {
                coordinates = geoJson.getJSONArray("coordinates").getJSONArray(0).getJSONArray(0);
            }

            // Get the average of all longitude/latitude coordinates
            int numLatitudes = 0;
            int numLongitudes = 0;
            double latitudes = 0;
            double longitudes = 0;
            try {
                int i = 0;
                while (true) {
                    JSONArray coordinate = coordinates.getJSONArray(i++);
                    latitudes += coordinate.getDouble(1);
                    ++numLatitudes;
                    longitudes += coordinate.getDouble(0);
                    ++numLongitudes;
                }
            } catch (Exception e) {}

            // convert to ContentValues
            ContentValues c = new ContentValues();
            c.put("stationName", stationName);
            c.put("longitude", longitudes / numLongitudes);
            c.put("latitude", latitudes / numLatitudes);
            return c;
        } catch (Exception e) {
            // Failed to parse this specific JSONObject
            Log.e(DownloadSkytrainStationsAsync.class.getSimpleName(), o.toString());
        }
        return null;
    }
}