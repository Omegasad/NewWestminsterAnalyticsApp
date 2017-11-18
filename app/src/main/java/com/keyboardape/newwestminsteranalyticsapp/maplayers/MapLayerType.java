package com.keyboardape.newwestminsteranalyticsapp.maplayers;

public enum MapLayerType {

    POPULATION_DENSITY
    ,BUILDING_AGE
    ,HIGH_RISES
    ,BUSINESS_DENSITY
    ;

    public MapLayer getLayer() {
        return MapLayer.GetLayer(this);
    }

}