package com.keyboardape.newwestminsteranalyticsapp.maplayers;

/**
 * Valid MapLayer types.
 */
public enum MapLayerType {

    POPULATION_DENSITY
    ,BUILDING_AGE
    ,HIGH_RISES
    ,BUSINESS_DENSITY
    ,PUBLIC_TRANSIT
    ;

    public MapLayer getLayer() {
        return MapLayer.GetLayer(this);
    }

}