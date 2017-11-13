package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.keyboardape.newwestminsteranalyticsapp.datautilities.DataManager;
import com.keyboardape.newwestminsteranalyticsapp.parsers.JsonStreamParserAsync;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business Licenses Data Set.
 */
public class BusinessLicensesData extends Data {

    private final static String              TABLE_NAME;
    private final static String              DATA_SOURCE_URL;
    private final static DataType            DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;

    static {
        TABLE_NAME       = "business_licenses";
        DATA_SOURCE_URL  = "http://opendata.newwestcity.ca/downloads/business-licenses-approved-2016/BL_APPROVED.json";
        DATA_SET_TYPE    = DataType.BUSINESS_LICENSES;

        // Must use LinkedHashMap or other Map that keeps insertion order
        TABLE_COLUMNS = new LinkedHashMap<>();
        TABLE_COLUMNS.put("id",           "INTEGER PRIMARY KEY AUTOINCREMENT");
        TABLE_COLUMNS.put("type",         "TEXT NOT NULL");
        TABLE_COLUMNS.put("tradeName",    "TEXT NOT NULL");
        TABLE_COLUMNS.put("licenceeName", "TEXT NOT NULL");
        TABLE_COLUMNS.put("address",      "TEXT NOT NULL");
        TABLE_COLUMNS.put("description",  "TEXT NOT NULL");
        TABLE_COLUMNS.put("yearOpened",   "INTEGER NOT NULL");
        TABLE_COLUMNS.put("sicNum",       "INTEGER NOT NULL");
        TABLE_COLUMNS.put("sicGroup",     "TEXT NOT NULL");
        TABLE_COLUMNS.put("latitude",     "REAL NOT NULL");
        TABLE_COLUMNS.put("longitude",    "REAL NOT NULL");
    }

    private Geocoder mGeocoder;
    private DataManager mDataManager;

    /**
     * Constructor.
     */
    public BusinessLicensesData(Context context) {
        super(DATA_SET_TYPE ,TABLE_NAME ,TABLE_COLUMNS);
        mGeocoder = new Geocoder(context);
        mDataManager = DataManager.GetInstance();
    }

    /**
     * Returns the total unit or row for the data set.
     * @return Totoal number of units or rows
     */
    @Override
    public long calcTotalUnits() {
        SQLiteDatabase db = mDataManager.getReadableDatabase();
        String queryTotalUnits = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE latitude <> 0 AND longitude <> 0;";
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
                    String type = o.getString("TYPE");
                    String address = o.getString("CIVIC_ADDRESS");

                    ContentValues c = new ContentValues();
                    c.put("type", type);
                    c.put("tradename", o.getString("TRADE_NAME"));
                    c.put("licenceeName", o.getString("LICENCEE_NAME"));
                    c.put("address", address);
                    c.put("description", o.getString("LICENCE_DESCRIPTION"));
                    c.put("yearOpened", o.getInt("YEAR_OPENED"));
                    c.put("sicNum", parseToIntOrZero(o.getString("SIC")));
                    c.put("sicGroup", o.getString("SIC_GROUP"));

                    LatLng latlng;
                    Double longitude = 0.;
                    Double latitude = 0.;
                    if (type.equals("RESIDENT") && (latlng = getLatLngOrNull(address)) != null) {
                        latitude = latlng.latitude;
                        longitude = latlng.longitude;
                    }
                    c.put("longitude", longitude);
                    c.put("latitude", latitude);
                    db.insert(TABLE_NAME, null, c);
                } catch (Exception e) {
                    // Failed to parse this specific JSONObject
                    Log.e(BusinessLicensesData.class.getSimpleName(), e.getMessage() + "::" + o.toString());
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

    /**
     * Parses a string to int, or returns 0.
     * @param integer to be parsed
     * @return int value of string or 0
     */
    private int parseToIntOrZero(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {} // Return 0, no need to handle exception
        return 0;
    }

    /**
     * Returns a LatLng (latitude and longitude) given an address.
     * @param address to be geocoded into LatLng
     * @return LatLng
     */
    private LatLng getLatLngOrNull(String address) {
        try {
            List<Address> addr = mGeocoder.getFromLocationName(address, 1);
            if (addr != null) {
                Address location = addr.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {} // Return null, no need to handle exception
        return null;
    }
}
