package com.keyboardape.newwestminsteranalyticsapp.data;

import com.keyboardape.newwestminsteranalyticsapp.db.DBConsts;

/**
 * Data Set that maps to the actual SQL tables.
 */
public enum DataSet {

    TRACKER(
        // tableName
        "tracker",
        // dataURL
        "",
        // csvColumnNames
        "tableName,isNeedUpdate,lastUpdated",
        // csvColumnNamesWithAttributes
        "tableName      TEXT    PRIMARY KEY UNIQUE NOT NULL," +
        "isNeedUpdate   INTEGER NOT NULL," +
        "lastUpdated    INTEGER NOT NULL")

    ,POPULATION_DENSITY(
        // tableName
        "population_density",
        // dataURL
        "http://opendata.newwestcity.ca/downloads/building-attributes/BUILDING_ATTRIBUTES.json",
        // csvColumnNames
        "id,latitude,longitude,numResidents",
        // csvColumnNamesWithAttributes
        "id             INTEGER PRIMARY KEY AUTOINCREMENT," +
        "longitude      REAL    NOT NULL," +
        "latitude       REAL    NOT NULL," +
        "numResidents   INTEGER NOT NULL")

    ,SKYTRAIN_STATIONS(
        // tableName
        "skytrain_stations",
        // dataURL
        "http://opendata.newwestcity.ca/downloads/skytrain-stations/SKYTRAIN_STATIONS.json",
        // csvColumnNames
        "id,latitude,longitude,stationName",
        // csvColumnNamesWithAttributes
        "id             INTEGER PRIMARY KEY AUTOINCREMENT," +
        "longitude      REAL    NOT NULL," +
        "latitude       REAL    NOT NULL," +
        "stationName    TEXT    NOT NULL")

    ,BUS_STOPS(
        // tableName
        "bus_stops",
        // dataURL
        "http://opendata.newwestcity.ca/downloads/bus-stops/BUS_STOPS.json",
        // csvColumnNames
        "id,latitude,longitude,stopNum",
        // csvColumnNamesWithAttributes
        "id             INTEGER PRIMARY KEY AUTOINCREMENT," +
        "longitude      REAL    NOT NULL," +
        "latitude       REAL    NOT NULL," +
        "stopNum        INTEGER NOT NULL")

    ,BUSINESS_LICENSES(
        // tableName
        "business_licenses",
        // dataURL
        "http://opendata.newwestcity.ca/downloads/business-licenses-approved-2016/BL_APPROVED.json",
        // csvColumnNames
        "id,type,tradeName,licenceeName,address,description,yearOpened,sicNum,sicGroup,longitude,latitude",
        // csvColumnNamesWithAttributes
        "id             INTEGER PRIMARY KEY AUTOINCREMENT," +
        "type           TEXT    NOT NULL," +
        "tradeName      TEXT    NOT NULL," +
        "licenceeName    TEXT    NOT NULL," +
        "address        TEXT    NOT NULL," +
        "description    TEXT    NOT NULL," +
        "yearOpened     INTEGER NOT NULL," +
        "sicNum         INTEGER NOT NULL," +
        "sicGroup       TEXT    NOT NULL," +
        "longitude      REAL    NOT NULL," +
        "latitude       REAL    NOT NULL")

    ;

    public final String TABLE_NAME;
    public final String SQL_GET_TABLE;
    public final String SQL_CREATE_TABLE;
    public final String SQL_DELETE_TABLE;
    public final String DATA_URL;
    DataSet(String tableName, String dataURL, String csvColumnNames, String csvColumnNamesWithAttributes) {
        TABLE_NAME = tableName;
        DATA_URL = dataURL;
        SQL_GET_TABLE = "SELECT " + csvColumnNames + " FROM " + tableName + " ";
        SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + csvColumnNamesWithAttributes + ");";
        SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + tableName + ";";
    }
    public String getSQLToUpdateTracker() {
        return "UPDATE " + DataSet.TRACKER.TABLE_NAME + " " +
            "SET isNeedUpdate = " + DBConsts.FALSE + ", " +
            "    lastUpdated = " + System.currentTimeMillis() + " " +
            "WHERE tablename = '" + TABLE_NAME + "';";
    }
    @Override
    public String toString() {
        return TABLE_NAME;
    }
}