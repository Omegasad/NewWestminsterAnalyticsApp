package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.content.ContentValues;

import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.MapsActivity;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map MapLayer.
 */
public abstract class MapLayer {

    public static Map<MapLayerType, MapLayer> MapLayers;

    static {
        MapLayers = new HashMap<>();
        MapLayers.put(MapLayerType.POPULATION_DENSITY, new PopulationDensityLayer());
        MapLayers.put(MapLayerType.BUILDING_AGE, new BuildingAgeLayer());
    }

    public static MapLayer Get(MapLayerType mapLayerType) {
        return MapLayers.get(mapLayerType);
    }

    protected MapLayerType mMapLayerType;

    public MapLayer(MapLayerType mapLayerType/*, LayerOptions mDefaultLayerOptions*/) {
        mMapLayerType = mapLayerType;
    }

    abstract public MapsActivity.MapOptions getMapOptions();
    abstract public void getMapDataAsync(final OnMapLayerDataReadyCallback callback);

    /* ********************************************************************************************
     *                                   CALLBACK INTERFACE                                       *
     ******************************************************************************************** */

    public interface OnMapLayerDataReadyCallback {
        void onMapLayerDataReady(MapLayerType mapLayerType, List<WeightedLatLng> dataOrNull);
    }
}
