package com.keyboardape.newwestminsteranalyticsapp.datautilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.keyboardape.newwestminsteranalyticsapp.datasets.BusStopsData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.BusinessLicensesData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.Data;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataType;
import com.keyboardape.newwestminsteranalyticsapp.datasets.MajorShoppingsData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.PopulationDensityData;
import com.keyboardape.newwestminsteranalyticsapp.datasets.SkytrainStationsData;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton Data Manager shared across all Android Activities.
 */
public final class DataManager extends SQLiteOpenHelper {

    private final static String                         DB_NAME;
    private final static int                            DB_VERSION;

    private static DataManager                          DataManagerInstance;
    private static Map<DataType, Data>                  DataSetsInstance;
    private static Map<DataType, Class<? extends Data>> DataSetClasses;

    static {
        DB_NAME             = "datasets.sqlite";
        DB_VERSION          = 2;

        DataManagerInstance = null;
        DataSetsInstance    = new HashMap<>();

        DataSetClasses = new HashMap<>();
        DataSetClasses.put(DataType.POPULATION_DENSITY, PopulationDensityData.class);
        DataSetClasses.put(DataType.BUSINESS_LICENSES, BusinessLicensesData.class);
        DataSetClasses.put(DataType.BUS_STOPS, BusStopsData.class);
        DataSetClasses.put(DataType.MAJOR_SHOPPINGS, MajorShoppingsData.class);
        DataSetClasses.put(DataType.SKYTRAIN_STATIONS, SkytrainStationsData.class);
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

            // Business Licenses needs GeoCoder which needs a Context
            Data businessLicenses = new BusinessLicensesData(context.getApplicationContext());
            DataSetsInstance.put(DataType.BUSINESS_LICENSES, businessLicenses);
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
     * Returns the specified Data instance.
     * @param dataType to get
     * @return Data or null
     */
    public static synchronized Data GetDataSetOrNull(DataType dataType) {
        Data data;
        if ((data = DataSetsInstance.get(dataType)) == null) {
            try {
                data = DataSetClasses.get(dataType).newInstance();
                DataSetsInstance.put(dataType, data);
                return data;
            } catch (Exception e) {
                return null;
            }
        }
        return data;
    }

    /* ********************************************************************************************
     *                               INSTANCE / SQLiteOpenHelper                                  *
     ******************************************************************************************** */

    /**
     * Private Constructor; SINGLETON.
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
     * @param db to UpdateStats
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
                String createLayerOptions =
                        "CREATE TABLE IF NOT EXISTS " + LayerOptionsTracker.TABLE_NAME + "(" +
                        "tableName       TEXT    PRIMARY KEY UNIQUE NOT NULL," +
                        "isAdditive      INTEGER NOT NULL," +
                        "layerWeight     REAL    NOT NULL," +
                        "totalUnits      INTEGER NOT NULL," +
                        "isShown         INTEGER NOT NULL);";
                db.execSQL(createTracker);
                db.execSQL(createLayerOptions);
            }
        } catch (Exception e) {}
    }
}
