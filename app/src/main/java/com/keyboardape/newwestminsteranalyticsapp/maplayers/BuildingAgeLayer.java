package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.maplayerinfo.BuildingAgeFragment;
import com.keyboardape.newwestminsteranalyticsapp.maplayerinfo.MapLayerInfoFragment;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Building Age MapLayer.
 */
public class BuildingAgeLayer extends MapLayer {

    // ---------------------------------------------------------------------------------------------
    //                                             STATIC
    // ---------------------------------------------------------------------------------------------

    private final static int          R_STRING_ID_LAYER_NAME;
    private final static int          R_DRAWABLE_ID_ICON;
    private final static MapLayerType LAYER_TYPE;
    private final static int          HEATMAP_RADIUS;

    // Used to reduce the intensity of older buildings to getAbsoluteValues
    // a better visual representation of data
    private final static int          YEAR_REDUCTION;

    static {
        R_STRING_ID_LAYER_NAME = R.string.layer_building_age;
        R_DRAWABLE_ID_ICON     = R.drawable.ic_account_balance_black_24dp;
        LAYER_TYPE             = MapLayerType.BUILDING_AGE;
        HEATMAP_RADIUS         = 10;

        YEAR_REDUCTION = 10;
    }

    // ---------------------------------------------------------------------------------------------
    //                                           INSTANCE
    // ---------------------------------------------------------------------------------------------

    private Polygon mPolygon = null;

    public BuildingAgeLayer() {
        super(LAYER_TYPE, R_STRING_ID_LAYER_NAME, R_DRAWABLE_ID_ICON, HEATMAP_RADIUS);
    }

    @Override
    public void showLayer() {
        super.showLayer();
        if (mPolygon != null) {
            mPolygon.setVisible(true);
        }
    }

    @Override
    public void hideLayer() {
        super.hideLayer();
        if (mPolygon != null) {
            mPolygon.setVisible(false);
        }
    }

    @Override
    public boolean onMapClick(LatLng p) {
        if (mPolygon != null) {
            mPolygon.remove();
        }

        // Calculate multiplier to show same rectangle size on screen
        // disregarding the zoom level
        float zoomMultiplier = GMap.getCameraPosition().zoom * 2.3f;
        for (int i = 32 - (int) GMap.getCameraPosition().zoom; i > 0; --i) {
            zoomMultiplier *= 1.383;
        }
        for (int i = (int) GMap.getCameraPosition().zoom; i > 0; --i) {
            zoomMultiplier *= .55;
        }
        float xOffset = 0.002f * zoomMultiplier;
        float yOffset = 0.0013f * zoomMultiplier;

        // Calculate rectangle coordinates
        LatLng[] coord = new LatLng[4];
        coord[0] = new LatLng(p.latitude - yOffset, p.longitude - xOffset);
        coord[1] = new LatLng(p.latitude - yOffset, p.longitude + xOffset);
        coord[2] = new LatLng(p.latitude + yOffset, p.longitude + xOffset);
        coord[3] = new LatLng(p.latitude + yOffset, p.longitude - xOffset);

        // Set coordinates for Map Layer Info Fragment
        BuildingAgeFragment frag = (BuildingAgeFragment) MapLayerInfoFragment.GetFragmentOrNull(MapLayerType.BUILDING_AGE);
        frag.setCoordinates(coord);

        // Draw rectangle on map
        mPolygon = GMap.addPolygon(new PolygonOptions()
            .add(coord[0], coord[1], coord[2], coord[3], coord[0])
            .strokeColor(Color.RED)
            .strokeWidth(6));

        // Center rectangle on screen
        LatLng moveTo = new LatLng(p.latitude - (2.8 * yOffset), p.longitude);
        GMap.animateCamera(CameraUpdateFactory.newLatLng(moveTo));

        // Automatically launch Map Layer Info Fragment
        return true;
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
                    Log.e(BuildingAgeLayer.class.getSimpleName(), e.getMessage());
                    data = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                callback.onMapLayerDataReady(LAYER_TYPE, data);
            }
        }, sqlQuery).execute();
    }

    private int getAggregate(String selectStatement) {
        String buildingAgeTableName = DataSetType.BUILDING_AGE.getDataSet().getTableName();
        String sql = "SELECT " + selectStatement + " FROM " + buildingAgeTableName + " "
                + "WHERE BLDGAGE IS NOT NULL "
                + "AND LATITUDE IS NOT NULL "
                + "AND LONGITUDE IS NOT NULL";
        SQLiteDatabase db = DBHelper.GetInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        int aggregate = cursor.getInt(0);
        cursor.close();
        db.close();
        return aggregate;
    }
}
