package com.keyboardape.newwestminsteranalyticsapp.datasets;

/**
 * Valid types of DataSet Set.
 */
public enum DataSetType {

    BUILDING_ATTRIBUTES
    ,SKYTRAIN_STATIONS
    ,BUS_STOPS
    ,BUSINESS_LICENSES
    ,MAJOR_SHOPPING
    ,BUILDING_AGE
    ,HIGH_RISES
    ,AGE_DEMOGRAPHICS
    ;

    public DataSet getDataSet() {
        return DataSet.GetDataSet(this);
    }

}
