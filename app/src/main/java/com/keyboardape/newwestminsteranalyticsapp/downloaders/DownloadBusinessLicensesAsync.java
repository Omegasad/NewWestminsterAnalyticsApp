//package com.keyboardape.newwestminsteranalyticsapp.downloaders;
//
//import android.content.ContentValues;
//import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;
//
//import com.keyboardape.newwestminsteranalyticsapp.data.DataSet;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
///**
// * Downloads Skytrain Stations data into SQLite Database.
// */
//public class DownloadBusinessLicensesAsync extends JsonDownloaderAsync {
//
//    public DownloadBusinessLicensesAsync(SQLiteDatabase db, Callbacks callbacks) {
//        super(DataSet.BUSINESS_LICENSES, db, callbacks);
//    }
//
//    @Override
//    protected ContentValues convertToContentValuesOrNull(JSONObject o) {
//
//        try {
//            ContentValues c = new ContentValues();
//            c.put("type", o.getString("TYPE"));
//            c.put("tradename", o.getString("TRADE_NAME"));
//            c.put("licenceeName", o.getString("LICENCEE_NAME"));
//            c.put("address", o.getString("CIVIC_ADDRESS"));
//            c.put("description", o.getString("LICENCE_DESCRIPTION"));
//            c.put("yearOpened", o.getInt("YEAR_OPENED"));
//            c.put("sicNum", parseToIntOrZero(o.getString("SIC")));
//            c.put("sicGroup", o.getString("SIC_GROUP"));
//            c.put("longitude", );
//            c.put("latitude", );
//            return c;
//        } catch (Exception e) {
//            // Failed to parse this specific JSONObject
//            Log.e(DownloadBusinessLicensesAsync.class.getSimpleName(), o.toString());
//        }
//        return null;
//    }
//
//
//    /*
//     * Parses a string to int, or returns 0.
//     * @param integer to be parsed
//     * @return int value of string or 0
//     */
//    private int parseToIntOrZero(String integer) {
//        try {
//            return Integer.parseInt(integer);
//        } catch (Exception e) {}
//        return 0;
//    }
//}