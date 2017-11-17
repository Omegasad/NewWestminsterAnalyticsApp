package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.MapsActivity;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.datasets.BuildingAttributesData;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Population Density MapLayer.
 */
public class PopulationDensityLayer extends MapLayer {

    private final static MapLayerType MAP_LAYER_TYPE = MapLayerType.POPULATION_DENSITY;
    private final static int MAP_LAYER_NAME_RESOURCE_ID = R.string.layer_population_density;

    public PopulationDensityLayer() {
        super(MAP_LAYER_TYPE, MAP_LAYER_NAME_RESOURCE_ID);
    }

    @Override
    public MapsActivity.MapOptions getMapOptions() {
        return new MapsActivity.MapOptions()
                .setHeatmapRadius(32);
    }

    @Override
    public void getMapDataAsync(final OnMapLayerDataReadyCallback callback) {
        String buildingAttributesTableName = DataSetType.BUILDING_ATTRIBUTES.getDataSet().getTableName();
        String sqlQuery = "SELECT LATITUDE, LONGITUDE, NUM_RES "
                + "FROM " + buildingAttributesTableName + " "
                + "WHERE NUM_RES IS NOT NULL "
                + "AND LATITUDE IS NOT NULL "
                + "AND LONGITUDE IS NOT NULL";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            List<WeightedLatLng> data;
            @Override
            public void onDBCursorReady(Cursor cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        data = new ArrayList<>();
                        do {
                            LatLng latlng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                            WeightedLatLng wlatlng = new WeightedLatLng(latlng, cursor.getInt(2));
                            data.add(wlatlng);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(BuildingAttributesData.class.getSimpleName(), e.getMessage());
                    data = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                callback.onMapLayerDataReady(MAP_LAYER_TYPE, data);
            }
        }, sqlQuery).execute();
    }
}
