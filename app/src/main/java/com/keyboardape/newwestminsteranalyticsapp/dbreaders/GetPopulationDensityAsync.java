package com.keyboardape.newwestminsteranalyticsapp.dbreaders;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.db.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.dbreaders.DBReaderAsync;

import java.util.ArrayList;
import java.util.List;

public class GetPopulationDensityAsync extends DBReaderAsync {

    public interface Callbacks {
        void onReadPopulationDensityComplete(List<WeightedLatLng> dataOrNull);
    }

    /** Callback functions for when reading succeeds and fails. */
    private Callbacks mCallbacks;

    /** Data. */
    private List<WeightedLatLng> mData;

    /**
     * Constructor.
     * @param db SQLiteDatabase reader
     * @param callbacks functions
     */
    public GetPopulationDensityAsync(SQLiteDatabase db, Callbacks callbacks) {
        super(db, DataSet.POPULATION_DENSITY);
        mCallbacks = callbacks;
        mData = null;
    }

    /**
     * Reads from the cursor.
     * @param cursor of a readable SQLiteDatabase
     */
    @Override
    protected void readFromCursor(Cursor cursor) {
        try {
            if (cursor.moveToFirst()) {
                mData = new ArrayList<>();
                do {
                    LatLng latlng = new LatLng(cursor.getDouble(1), cursor.getDouble(2));
                    WeightedLatLng wlatlng = new WeightedLatLng(latlng, cursor.getInt(3));
                    mData.add(wlatlng);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {}
    }

    /**
     * Called when done reading, success or fail.
     */
    @Override
    protected void onReadFinished() {
        mCallbacks.onReadPopulationDensityComplete(mData);
    }
}