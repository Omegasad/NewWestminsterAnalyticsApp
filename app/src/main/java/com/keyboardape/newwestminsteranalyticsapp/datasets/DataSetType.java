package com.keyboardape.newwestminsteranalyticsapp.datasets;

import com.keyboardape.newwestminsteranalyticsapp.utilities.DataManager;

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
    ;

    public DataSet getDataSet() {
        return DataManager.GetDataSet(this);
    }

}
