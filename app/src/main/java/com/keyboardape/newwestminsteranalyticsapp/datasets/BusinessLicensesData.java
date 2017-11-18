package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DataManager;
import com.keyboardape.newwestminsteranalyticsapp.utilities.JSONStreamParserAsync;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business Licenses DataSet.
 *
 * Downloaded from New West Open Data:
 * http://opendata.newwestcity.ca/datasets/business-licenses-all
 */
public class BusinessLicensesData extends DataSet {

    private final static String              TABLE_NAME;
    private final static String              DATA_SOURCE_URL;
    private final static DataSetType         DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;

    static {
        TABLE_NAME      = "business_licenses";
        DATA_SOURCE_URL = "http://opendata.newwestcity.ca/downloads/business-licenses-approved-2016/BL_APPROVED.json";
        DATA_SET_TYPE   = DataSetType.BUSINESS_LICENSES;

        TABLE_COLUMNS = new HashMap<>();
        TABLE_COLUMNS.put("ID",                  "INTEGER PRIMARY KEY AUTOINCREMENT");
        // Original Data
        TABLE_COLUMNS.put("TYPE",                "TEXT");    // e.g. "RESIDENT"
        TABLE_COLUMNS.put("TRADE_NAME",          "TEXT");    // e.g. "UTHERVERSE DIGITAL INC."
        TABLE_COLUMNS.put("LICENCEE_NAME",       "TEXT");    // e.g. "UTHERVERSE DIGITAL INC."
        TABLE_COLUMNS.put("CIVIC_ADDRESS",       "TEXT");    // e.g. "416 COLUMBIA ST\r\nNEW WESTMINSTER BC  V3L 1B1"
        TABLE_COLUMNS.put("LICENCE_DESCRIPTION", "TEXT");    // e.g. "SERVICES/ONLINE/GAMING"
        TABLE_COLUMNS.put("SIC_GROUP",           "TEXT");    // e.g. "Computer systems design and related services"
        TABLE_COLUMNS.put("LICENCE",             "INTEGER"); // e.g. 131146
        TABLE_COLUMNS.put("YEAR_OPENED",         "INTEGER"); // e.g. 2017
        // Modified Data
        TABLE_COLUMNS.put("SIC",                 "INTEGER"); // e.g. "5415"     -> 5415
        TABLE_COLUMNS.put("APPROVED_DATE",       "INTEGER"); // e.g. "20171020" -> 1508482800000 (Epoch Timestamp Milliseconds)
        TABLE_COLUMNS.put("NWID",                "INTEGER"); // e.g. "00269001" -> 00269001
        // Derived Data
        TABLE_COLUMNS.put("LATITUDE",            "REAL");    // e.g.   49.2233306124747
        TABLE_COLUMNS.put("LONGITUDE",           "REAL");    // e.g. -122.912645675014
    }

    private Geocoder mGeocoder;

    public BusinessLicensesData(Context context) {
        super(DATA_SET_TYPE ,TABLE_NAME ,TABLE_COLUMNS);
        mGeocoder = new Geocoder(context);
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
                    ContentValues c = new ContentValues();

                    // Original Data
                    String type         = ParseToStringOrNull(o.getString("TYPE"));
                    String civicAddress = ParseToStringOrNull(o.getString("CIVIC_ADDRESS"));
                    c.put("TYPE",                type);
                    c.put("TRADE_NAME",          ParseToStringOrNull(o.getString("TRADE_NAME")));
                    c.put("LICENCEE_NAME",       ParseToStringOrNull(o.getString("LICENCEE_NAME")));
                    c.put("CIVIC_ADDRESS",       civicAddress);
                    c.put("LICENCE_DESCRIPTION", ParseToStringOrNull(o.getString("LICENCE_DESCRIPTION")));
                    c.put("SIC_GROUP",           ParseToStringOrNull(o.getString("SIC_GROUP")));
                    c.put("LICENCE",             ParseToIntOrNull(o.getString("LICENCE")));
                    c.put("YEAR_OPENED",         ParseToIntOrNull(o.getString("YEAR_OPENED")));

                    // Modified Data
                    c.put("NWID",                ParseToIntOrNull(o.getString("NWID")));
                    c.put("SIC",                 ParseToIntOrNull(o.getString("SIC")));
                    c.put("APPROVED_DATE",       parseToMillisecondsOrNull(o.getString("APPROVED_DATE")));

                    // Derived Data
                    // Get latitude and longitude from CIVIC_ADDRESS through Google's Geocoder
                    LatLng latlng;
                    Double latitude = null;
                    Double longitude = null;
                    if (type.equals("RESIDENT") && (latlng = getCoordinatesOrNull(civicAddress)) != null) {
                        latitude  = latlng.latitude;
                        longitude = latlng.longitude;
                    }
                    c.put("LATITUDE",  latitude);
                    c.put("LONGITUDE", longitude);

                    db.insert(TABLE_NAME, null, c);
                } catch (Exception e) {
                    Log.e(BusinessLicensesData.class.getSimpleName(), e.getMessage() + "::" + o.toString());
                }
            }
            @Override
            public void onJsonStreamParsed(boolean isSuccessful) {
                db.close();
                callback.onDataSetUpdated(DATA_SET_TYPE, isSuccessful);
            }
        }, DATA_SOURCE_URL).execute();
    }

    // ---------------------------------------------------------------------------------------------
    //                                     HELPER FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    private Long parseToMillisecondsOrNull(String YYYYMMDD) {
        try {
            return new SimpleDateFormat("yyyyMMdd").parse(YYYYMMDD).getTime();
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }

    private LatLng getCoordinatesOrNull(String address) {
        try {
            List<Address> addresses = mGeocoder.getFromLocationName(address, 1);
            Address firstAddress = addresses.get(0);
            return new LatLng(firstAddress.getLatitude(), firstAddress.getLongitude());
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }
}