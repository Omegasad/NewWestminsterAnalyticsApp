package com.keyboardape.newwestminsteranalyticsapp.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.datasets.BuildingAgeData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.BusStopsData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.BusinessLicensesData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetTracker;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.datasets.HighRisesData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.MajorShoppingData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.BuildingAttributesData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.SkytrainStationsData;
//import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton Data Manager shared across all Android Activities.
 */
public final class DataManager extends SQLiteOpenHelper {

    private final static String DB_NAME = "datasets.sqlite";
    private final static int    DB_VERSION = 1;

    private static DataManager                                DataManagerInstance;
    private static Map<DataSetType, DataSet>                  DataSetsInstance;
    private static Map<DataSetType, Class<? extends DataSet>> DataSetClasses;

    static {
        DataManagerInstance = null;
        DataSetsInstance = new HashMap<>();
        DataSetClasses = new HashMap<>();
        DataSetClasses.put(DataSetType.BUILDING_ATTRIBUTES, BuildingAttributesData.class);
        DataSetClasses.put(DataSetType.BUSINESS_LICENSES,   BusinessLicensesData.class);
        DataSetClasses.put(DataSetType.BUS_STOPS,           BusStopsData.class);
        DataSetClasses.put(DataSetType.MAJOR_SHOPPING,      MajorShoppingData.class);
        DataSetClasses.put(DataSetType.SKYTRAIN_STATIONS,   SkytrainStationsData.class);
        DataSetClasses.put(DataSetType.BUILDING_AGE,        BuildingAgeData.class);
        DataSetClasses.put(DataSetType.HIGH_RISES,          HighRisesData.class);
    }

    /* ********************************************************************************************
     *                                     STATIC FUNCTIONS                                       *
     ******************************************************************************************** */

    /**
     * Initializes a DataManager instance if not already initializes.
     * @param context of caller Activity
     */
    public static synchronized void Initialize(Context context) {
        if (DataManagerInstance == null) {
            DataManagerInstance = new DataManager(context.getApplicationContext());

            // Business Licenses needs GeoCoder which needs a Context to initialize
            DataSet businessLicenses = new BusinessLicensesData(context.getApplicationContext());
            DataSetsInstance.put(DataSetType.BUSINESS_LICENSES, businessLicenses);
        }
    }

    /**
     * Returns a Singleton instance of DataManager.
     * @return DataManager
     */
    public static DataManager GetInstance() {
        return DataManagerInstance;
    }

    /**
     * Returns the specified DataSet instance. Will create one if non-existent.
     * @param dataSetType to get
     * @return DataSet
     */
    public static synchronized DataSet GetDataSet(DataSetType dataSetType) {
        DataSet dataSet;
        if ((dataSet = DataSetsInstance.get(dataSetType)) == null) {
            try {
                dataSet = DataSetClasses.get(dataSetType).newInstance();
                DataSetsInstance.put(dataSetType, dataSet);
            } catch (Exception e) {
                Log.e(DataManager.class.getSimpleName(), e.getMessage());
            }
        }
        return dataSet;
    }

    /* ********************************************************************************************
     *                               INSTANCE / SQLiteOpenHelper                                  *
     ******************************************************************************************** */

    /**
     * Prevent outside initialization; Singleton.
     * @param context of the caller.
     */
    private DataManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Create database if non-existent.
     * @param db written to
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, 1);
    }

    /**
     * Upgrade database. Called when DB_VERSION changes upwards.
     * @param db to upgrade
     * @param oldVersion of db
     * @param newVersion of db
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    /**
     * Updates database based on oldVersion and newVersion.
     * @param db to SetRequireUpdate
     * @param oldVersion of db
     * @param newVersion of db
     */
    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 1) {
                String createTracker =
                        "CREATE TABLE IF NOT EXISTS " + DataSetTracker.TABLE_NAME + "(" +
                        "tableName       TEXT    PRIMARY KEY UNIQUE NOT NULL," +
                        "isRequireUpdate INTEGER NOT NULL," +
                        "lastUpdated     INTEGER NOT NULL);";
                db.execSQL(createTracker);
            }
        } catch (Exception e) {
            Log.e(DataManager.class.getSimpleName(), e.getMessage());
        }
    }
}
