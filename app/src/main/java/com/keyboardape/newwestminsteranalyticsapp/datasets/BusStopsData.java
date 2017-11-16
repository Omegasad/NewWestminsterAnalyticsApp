package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.utilities.DataManager;
import com.keyboardape.newwestminsteranalyticsapp.utilities.JSONStreamParserAsync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Bus Stops DataSet.
 *
 * Downloaded from New West Open Data:
 * http://opendata.newwestcity.ca/datasets/bus-stops
 */
public class BusStopsData extends DataSet {

    private final static String              TABLE_NAME;
    private final static String              DATA_SOURCE_URL;
    private final static DataSetType         DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;

    static {
        TABLE_NAME      = "bus_stops";
        DATA_SOURCE_URL = "http://opendata.newwestcity.ca/downloads/bus-stops/BUS_STOPS.json";
        DATA_SET_TYPE   = DataSetType.BUS_STOPS;

        TABLE_COLUMNS = new HashMap<>();
        TABLE_COLUMNS.put("ID",         "INTEGER PRIMARY KEY AUTOINCREMENT");
        // Original Data
        TABLE_COLUMNS.put("OBJECTID_1", "INTEGER"); // e.g. 1
        TABLE_COLUMNS.put("OBJECTID",   "INTEGER"); // e.g. 1
        TABLE_COLUMNS.put("BUSSTOPNUM", "INTEGER"); // e.g. 53500
        TABLE_COLUMNS.put("RULEID",     "INTEGER"); // e.g. 1
        TABLE_COLUMNS.put("ONSTREET",   "TEXT");    // e.g. "5 St"
        TABLE_COLUMNS.put("ATSTREET",   "TEXT");    // e.g. "7 Av"
        TABLE_COLUMNS.put("DIRECTION",  "TEXT");    // e.g. "WB"
        TABLE_COLUMNS.put("POSITION",   "TEXT");    // e.g. "FS"
        TABLE_COLUMNS.put("STATUS",     "TEXT");    // e.g. "ACTIVE"
        TABLE_COLUMNS.put("ACCESSIBLE", "TEXT");    // e.g. "NO"
        TABLE_COLUMNS.put("CITY_NAME",  "TEXT");    // e.g. "NEW WESTMINSTER"
        TABLE_COLUMNS.put("LATITUDE",   "REAL");    // e.g.   49.2233306124747
        TABLE_COLUMNS.put("LONGITUDE",  "REAL");    // e.g. -122.912645675014
        // Discarded Data:
        //      - UNITNUM       (values are usually null)
    }

    public BusStopsData() {
        super(DATA_SET_TYPE ,TABLE_NAME ,TABLE_COLUMNS);
    }

    // ---------------------------------------------------------------------------------------------
    //                                         DOWNLOAD DATA
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void downloadDataToDBAsync(final OnDataSetUpdatedCallback callback) {
        final SQLiteDatabase db = DataManager.GetInstance().getWritableDatabase();
        new JSONStreamParserAsync(new JSONStreamParserAsync.Callbacks() {
            @Override
            public void onNewJsonObjectFromStream(JSONObject o) {
                try {
                    if (o.getString("STATUS").equals("ACTIVE")) {
                        ContentValues c = new ContentValues();

                        // Original Data
                        c.put("OBJECTID_1", ParseToIntOrNull(o.getString("OBJECTID_1")));
                        c.put("OBJECTID",   ParseToIntOrNull(o.getString("OBJECTID")));
                        c.put("BUSSTOPNUM", ParseToIntOrNull(o.getString("BUSSTOPNUM")));
                        c.put("RULEID",     ParseToIntOrNull(o.getString("RuleID")));
                        c.put("ONSTREET",   ParseToStringOrNull(o.getString("ONSTREET")));
                        c.put("ATSTREET",   ParseToStringOrNull(o.getString("ATSTREET")));
                        c.put("DIRECTION",  ParseToStringOrNull(o.getString("DIRECTION")));
                        c.put("POSITION",   ParseToStringOrNull(o.getString("POSITION")));
                        c.put("STATUS",     ParseToStringOrNull(o.getString("STATUS")));
                        c.put("ACCESSIBLE", ParseToStringOrNull(o.getString("ACCESSIBLE")));
                        c.put("CITY_NAME",  ParseToStringOrNull(o.getString("CITY_NAME")));

                        JSONArray coordinates = GetAverageCoordinatesFromJsonGeometryOrNull(o);
                        c.put("LATITUDE", (coordinates == null) ? null : coordinates.getDouble(1));
                        c.put("LONGITUDE",(coordinates == null) ? null : coordinates.getDouble(0));

                        db.insert(TABLE_NAME, null, c);
                    }
                } catch (Exception e) {
                    Log.e(BusStopsData.class.getSimpleName(), e.getMessage() + "::" + o.toString());
                }
            }
            @Override
            public void onJsonStreamParsed(boolean isSuccessful) {
                db.close();
                callback.onDataSetUpdated(DATA_SET_TYPE, isSuccessful);
            }
        }, DATA_SOURCE_URL).execute();
    }
}
