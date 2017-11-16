package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.utilities.DataManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * DataSet.
 *
 * All data set are downloaded from New West Open DataSet:
 * http://opendata.newwestcity.ca/datasets
 */
public abstract class DataSet {

    private final DataSetType         mDataSetType;
    private final String              mTableName;
    private final Map<String, String> mTableColumns;

    // ---------------------------------------------------------------------------------------------
    //                                   STATIC PARSING HELPERS
    // ---------------------------------------------------------------------------------------------

    public static String ParseToStringOrNull(String string) {
        return (string.length() == 0 || string.equalsIgnoreCase("null"))
            ? null
            : string;
    }

    public static Integer ParseToIntOrNull(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }

    public static Double ParseToDoubleOrNull(String aDouble) {
        try {
            return Double.parseDouble(aDouble);
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }

    public static JSONArray GetAverageCoordinatesFromJsonGeometryOrNull(JSONObject o) {
        try {
            JSONObject geoJson = o.getJSONObject("json_geometry");
            String geoJsonType = geoJson.getString("type");

            JSONArray coordinates = geoJson.getJSONArray("coordinates");
            if (geoJsonType.equals("Polygon")) {
                coordinates = coordinates.getJSONArray(0);
            } else if (geoJsonType.equals("MultiPolygon")) {
                coordinates = coordinates.getJSONArray(0).getJSONArray(0);
            }

            // Get the average of all longitude/latitude coordinates
            int numCoord2 = 0;
            int numCoord1 = 0;
            double coord1 = 0;
            double coord2 = 0;
            try {
                int i = 0;
                while (true) {
                    JSONArray coordinate = coordinates.getJSONArray(i++);
                    coord1 += coordinate.getDouble(0);
                    ++numCoord1;
                    coord2 += coordinate.getDouble(1);
                    ++numCoord2;
                }
            } catch (Exception e) {
                // Expected to end with exception 100% of time when there's no more coordinates
            }

            return new JSONArray(new double[] {coord1 / numCoord1, coord2 / numCoord2});
        } catch (Exception e) {
            Log.e(DataSet.class.getSimpleName(), e.getMessage() + "::" + o.toString());
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------
    //                                  CONSTRUCTOR, GETTERS, SETTERS
    // ---------------------------------------------------------------------------------------------

    public DataSet(DataSetType dataSetType, String tableName, Map<String, String> tableColumns) {
        mDataSetType  = dataSetType;
        mTableName    = tableName;
        mTableColumns = tableColumns;
    }

    public DataSetType getDataSetType() {
        return mDataSetType;
    }

    public String getTableName() {
        return mTableName;
    }

    public boolean isRequireUpdate() {
        ContentValues c = DataSetTracker.GetStatsOrNull(mDataSetType);
        return (c == null) || c.getAsBoolean("isRequireUpdate");
    }

    public void setRequireUpdate(boolean isRequireUpdate) {
        DataSetTracker.SetRequireUpdate(mDataSetType, isRequireUpdate);
    }

    // ---------------------------------------------------------------------------------------------
    //                                       UPDATE DATA SET
    // ---------------------------------------------------------------------------------------------

    abstract protected void downloadDataToDBAsync(OnDataSetUpdatedCallback callback);

    public void updateDataAsync(final OnDataSetUpdatedCallback callback) {
        dropAndCreateTable();
        downloadDataToDBAsync(new OnDataSetUpdatedCallback() {
            @Override
            public void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful) {
                DataSetTracker.SetRequireUpdate(mDataSetType, !isUpdateSuccessful);
                callback.onDataSetUpdated(dataSetType, isUpdateSuccessful);
            }
        });
    }

    private void dropAndCreateTable() {
        SQLiteDatabase db = DataManager.GetInstance().getWritableDatabase();
        String csvColumnNamesWithAttributes = concatenateToCSV(mTableColumns);
        String queryDelete = "DROP TABLE IF EXISTS " + mTableName + ";";
        String queryCreate = "CREATE TABLE IF NOT EXISTS " + mTableName + "(" + csvColumnNamesWithAttributes + ");";
        db.execSQL(queryDelete);
        db.execSQL(queryCreate);
        db.close();
    }

    private String concatenateToCSV(Map<String, String> map) {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            str.append(entry.getKey())
                    .append(' ')
                    .append(entry.getValue())
                    .append(',');
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    // ---------------------------------------------------------------------------------------------
    //                               INTERFACE : ON DATA SET UPDATED CALLBACK
    // ---------------------------------------------------------------------------------------------/

    public interface OnDataSetUpdatedCallback {
        void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful);
    }
}
