package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DataSet.
 *
 * All data set are downloaded from New West Open DataSet:
 * http://opendata.newwestcity.ca/datasets
 */
public abstract class DataSet {

    // ---------------------------------------------------------------------------------------------
    //                                  STATIC : INITIALIZATION
    // ---------------------------------------------------------------------------------------------

    private static boolean                                    IsInitialized;
    private static Map<DataSetType, DataSet>                  DataSetInstances;
    private static Map<DataSetType, Class<? extends DataSet>> DataSetClasses;

    static {
        IsInitialized = false;
        DataSetInstances = new LinkedHashMap<>();
        DataSetClasses = new LinkedHashMap<>();
        DataSetClasses.put(DataSetType.BUS_STOPS,           BusStopsData.class);
        DataSetClasses.put(DataSetType.SKYTRAIN_STATIONS,   SkytrainStationsData.class);
        DataSetClasses.put(DataSetType.BUILDING_ATTRIBUTES, BuildingAttributesData.class);
        DataSetClasses.put(DataSetType.BUSINESS_LICENSES,   BusinessLicensesData.class);
        DataSetClasses.put(DataSetType.MAJOR_SHOPPING,      MajorShoppingData.class);
        DataSetClasses.put(DataSetType.BUILDING_AGE,        BuildingAgeData.class);
        DataSetClasses.put(DataSetType.HIGH_RISES,          HighRisesData.class);
        DataSetClasses.put(DataSetType.AGE_DEMOGRAPHICS,    AgeDemographicsData.class);
    }

    public static synchronized void Initialize(Context context) {
        if (!IsInitialized) {
            IsInitialized = true;

            // Special data set requires context for Geocoding
            DataSetInstances.put(DataSetType.BUSINESS_LICENSES, new BusinessLicensesData(context));

            try {
                for (DataSetType type : GetAllDataSetTypes()) {
                    if (DataSetInstances.get(type) == null) {
                        DataSetInstances.put(type, DataSetClasses.get(type).newInstance());
                    }
                }
            } catch (Exception e) {
                Log.e(DataSet.class.getSimpleName(), e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    //                                     STATIC : GETTERS
    // ---------------------------------------------------------------------------------------------

    public static DataSet GetDataSet(DataSetType dataSetType) {
        return DataSetInstances.get(dataSetType);
    }

    public static DataSetType[] GetAllDataSetTypes() {
        return DataSetClasses.keySet().toArray(new DataSetType[DataSetClasses.size()]);
    }

    public static DataSet[] GetAllDataSets() {
        return DataSetInstances.values().toArray(new DataSet[DataSetInstances.size()]);
    }

    // ---------------------------------------------------------------------------------------------
    //                                  STATIC : DATA PARSING HELPERS
    // ---------------------------------------------------------------------------------------------

    protected static String ParseToStringOrNull(String string) {
        return (string.length() == 0 || string.equalsIgnoreCase("null"))
            ? null
            : string;
    }

    protected static Integer ParseToIntOrNull(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }

    protected static Double ParseToDoubleOrNull(String aDouble) {
        try {
            return Double.parseDouble(aDouble);
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }

    protected static JSONArray GetAverageCoordinatesFromJsonGeometryOrNull(JSONObject o) {
        try {
            JSONObject geoJson = o.getJSONObject("json_geometry");
            String geoJsonType = geoJson.getString("type");

            // different shapes have different # of array layers
            JSONArray coordinates = geoJson.getJSONArray("coordinates"); // Line shape
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
    //                                         INSTANCE
    // ---------------------------------------------------------------------------------------------

    private final DataSetType         mDataSetType;
    private final String              mTableName;
    private final Map<String, String> mTableColumns;
    private final int                 mRStringIDName;

    private boolean mIsUpdating;
    private boolean mIsUpToDate;

    public DataSet(DataSetType dataSetType,
                   String tableName,
                   Map<String, String> tableColumns,
                   int rStringIDName) {
        mDataSetType  = dataSetType;
        mTableName    = tableName;
        mTableColumns = tableColumns;
        mRStringIDName = rStringIDName;

        mIsUpdating = false;
        mIsUpToDate = false;
    }

    public DataSetType getDataSetType() {
        return mDataSetType;
    }

    public String getTableName() {
        return mTableName;
    }

    public int getRStringIDDataSetName() {
        return mRStringIDName;
    }

    public boolean isRequireUpdate() {
        ContentValues c = DataSetTracker.GetStatsOrNull(mDataSetType);
        mIsUpToDate = !((c == null) || c.getAsBoolean("isRequireUpdate"));
        return !mIsUpToDate;
    }

    public boolean isUpdating() {
        return mIsUpdating;
    }

    public boolean isUpToDate() {
        return mIsUpToDate;
    }

    public void setRequireUpdate(boolean isRequireUpdate) {
        DataSetTracker.SetRequireUpdate(mDataSetType, isRequireUpdate, 0);
    }

    // ---------------------------------------------------------------------------------------------
    //                                       UPDATE DATA SET
    // ---------------------------------------------------------------------------------------------

    abstract protected void downloadDataToDBAsync(OnDataSetUpdatedCallbackInternal callback);

    public void updateDataAsync(final OnDataSetUpdatedCallback callback) {
        mIsUpdating = true;
        recreateDBTable();
        downloadDataToDBAsync(new OnDataSetUpdatedCallbackInternal() {
            @Override
            public void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful, long dataLastUpdated) {
                mIsUpdating = false;
                mIsUpToDate = true;
                DataSetTracker.SetRequireUpdate(mDataSetType, !isUpdateSuccessful, dataLastUpdated);
                callback.onDataSetUpdated(dataSetType, isUpdateSuccessful);
            }
        });
    }

    private void recreateDBTable() {
        SQLiteDatabase db = DBHelper.GetInstance().getWritableDatabase();
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

    protected interface OnDataSetUpdatedCallbackInternal {
        void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful, long dataLastUpdated);
    }
}
