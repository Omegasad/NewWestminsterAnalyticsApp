package com.keyboardape.newwestminsteranalyticsapp.maps;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataType;
import com.keyboardape.newwestminsteranalyticsapp.datasets.PopulationDensityData;
import com.keyboardape.newwestminsteranalyticsapp.datautilities.DBReaderAsync;

import java.util.ArrayList;
import java.util.List;

public class PopulationDensityLayer extends MapLayer {

    private final static DataType      DATA_TYPE;
    private final static ContentValues DEFAULT_LAYER_OPTIONS;

    static {
        DATA_TYPE = DataType.POPULATION_DENSITY;
        DEFAULT_LAYER_OPTIONS = new ContentValues();
        DEFAULT_LAYER_OPTIONS.put("isShown", true);
        DEFAULT_LAYER_OPTIONS.put("isAdditive", true);
        DEFAULT_LAYER_OPTIONS.put("layerWeight", 0.2f);
    }

    public PopulationDensityLayer() {
        super(DATA_TYPE, DEFAULT_LAYER_OPTIONS);
    }

    @Override
    public void getMapDataAsync(final OnMapLayerDataReadyCallback callback) {
        String sqlQuery = "SELECT latitude, longitude, numResidents FROM " + mData.getTableName() + " ";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            List<WeightedLatLng> data;
            /**
             * Called when DB cursor is ready. Runs in background thread.
             * @param cursor DB Cursor
             */
            @Override
            public void onDBCursorReady(Cursor cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        data = new ArrayList<>();
                        float intensityMultiplicationFactor = getLayerWeight() * AllLayersTotalUnits / getTotalUnits();
                        if (!isAdditive()) {
                            intensityMultiplicationFactor *= -1;
                        }
                        do {
                            LatLng latlng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                            double intensity = intensityMultiplicationFactor * cursor.getInt(2);
                            WeightedLatLng wlatlng = new WeightedLatLng(latlng, intensity);
                            data.add(wlatlng);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(PopulationDensityData.class.getSimpleName(), e.getMessage());
                    data = null;
                }
            }
            /**
             * Called when database read completes, success or fail.
             */
            @Override
            public void onDBReadComplete() {
                callback.onMapLayerDataReady(mDataType, data);
            }
        }, sqlQuery).execute();
    }
}
