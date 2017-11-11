package com.keyboardape.newwestminsteranalyticsapp.downloaders;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.keyboardape.newwestminsteranalyticsapp.db.DataSet;

import org.json.JSONObject;

import java.util.List;

/**
 * Downloads Skytrain Stations data into SQLite Database.
 */
public class DownloadBusinessLicensesAsync extends JsonDownloaderAsync {

    /** Geocoder: transforms address into LatLng. */
    private Geocoder mGeocoder;

    /**
     * Constructor.
     * @param context of caller
     * @param db to download into
     * @param callbacks functions
     */
    public DownloadBusinessLicensesAsync(Context context, SQLiteDatabase db, Callbacks callbacks) {
        super(DataSet.BUSINESS_LICENSES, db, callbacks);
        mGeocoder = new Geocoder(context);
    }

    /**
     * Converts JSONObject into ContentValues while discarding unwanted data.
     * @param o JSONObject to be converted
     * @return ContentValues
     */
    @Override
    protected ContentValues convertToContentValuesOrNull(JSONObject o) {
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

            return c;
        } catch (Exception e) {
            // Failed to parse this specific JSONObject
            Log.e(DownloadBusinessLicensesAsync.class.getSimpleName(), o.toString());
        }
        return null;
    }

    /**
     * Parses a string to int, or returns 0.
     * @param integer to be parsed
     * @return int value of string or 0
     */
    private int parseToIntOrZero(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {}
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
        } catch (Exception e) {}
        return null;
    }
}