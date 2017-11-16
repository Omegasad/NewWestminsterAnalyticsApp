package com.keyboardape.newwestminsteranalyticsapp.maplayers;

public enum MapLayerType {

    POPULATION_DENSITY
    ,BUILDING_AGE
    ,HIGH_RISES
    ;

    public MapLayer getLayer() {
        return MapLayer.Get(this);
    }

}