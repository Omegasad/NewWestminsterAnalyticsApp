package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.MapsActivity;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.datasets.BuildingAttributesData;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Building Age MapLayer.
 */
public class BuildingAgeLayer extends MapLayer {

    private final static MapLayerType MAP_LAYER_TYPE = MapLayerType.BUILDING_AGE;
    private final static int MAP_LAYER_NAME_RESOURCE_ID = R.string.layer_building_age;

    // Used to reduce the intensity of older buildings to get
    // a better visual representation of data
    private final static int YEAR_REDUCTION = 15;

    public BuildingAgeLayer() {
        super(MAP_LAYER_TYPE, MAP_LAYER_NAME_RESOURCE_ID);
    }

    @Override
    public MapsActivity.MapOptions getMapOptions() {
        return new MapsActivity.MapOptions()
                .setHeatmapRadius(15);
    }

    @Override
    public void getMapDataAsync(final OnMapLayerDataReadyCallback callback) {
        int yearReduction = getAggregate("MAX(BLDGAGE) - " + YEAR_REDUCTION);
        String buildingAgeTableName = DataSetType.BUILDING_AGE.getDataSet().getTableName();
        String sqlQuery = "SELECT LATITUDE, LONGITUDE, BLDGAGE "
                + "FROM " + buildingAgeTableName + " "
                + "WHERE BLDGAGE IS NOT NULL "
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
                            float intensity = (cursor.getInt(2) - yearReduction);
                            WeightedLatLng wlatlng = new WeightedLatLng(latlng, intensity);
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

    private int getAggregate(String selectStatement) {
        String buildingAgeTableName = DataSetType.BUILDING_AGE.getDataSet().getTableName();
        String sql = "SELECT " + selectStatement + " FROM " + buildingAgeTableName + " "
                + "WHERE BLDGAGE IS NOT NULL "
                + "AND LATITUDE IS NOT NULL "
                + "AND LONGITUDE IS NOT NULL";
        SQLiteDatabase db = DataManager.GetInstance().getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        int aggregate = cursor.getInt(0);
        cursor.close();
        db.close();
        return aggregate;
    }
}
