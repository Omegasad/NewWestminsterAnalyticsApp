package com.keyboardape.newwestminsteranalyticsapp.maps;

import android.content.ContentValues;

import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.datasets.Data;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataType;
import com.keyboardape.newwestminsteranalyticsapp.datautilities.DataManager;
import com.keyboardape.newwestminsteranalyticsapp.datautilities.LayerOptionsTracker;

import java.util.List;

/**
 * Map Layer.
 */
public abstract class MapLayer {

    static protected long AllLayersTotalUnits = 0;

    protected DataType      mDataType;
    protected ContentValues mLayerOptions;

    protected DataManager   mDataManager;
    protected Data          mData;

    /**
     * Constructor.
     * @param type of Data Set
     * @param mDefaultLayerOptions of Data Set
     */
    public MapLayer(DataType type, ContentValues mDefaultLayerOptions) {
        mDataType     = type;
        mLayerOptions = LayerOptionsTracker.GetStatsOrNull(type);
        if (mLayerOptions == null) {
            mLayerOptions = mDefaultLayerOptions;
        }
        mLayerOptions.put("totalUnits", DataManager.GetDataSetOrNull(type).calcTotalUnits());

        mDataManager = DataManager.GetInstance();
        mData        = DataManager.GetDataSetOrNull(type);

        AllLayersTotalUnits += getTotalUnits();
    }

    /**
     * Gets map data asynchronously.
     * @param callback functions when data is ready
     */
    abstract public void getMapDataAsync(final OnMapLayerDataReadyCallback callback);

    /* ********************************************************************************************
     *                                             GETTERS                                        *
     ******************************************************************************************** */

    /**
     * Returns the relative weight of this data set compared to all data sets.
     * @return layer weight as float
     */
    public float getLayerWeight() {
        return mLayerOptions.getAsFloat("layerWeight");
    }

    /**
     * Returns the total units of this data set (e.g. total number of residents for population density).
     * @return as a long
     */
    public long getTotalUnits() {
        return mLayerOptions.getAsLong("totalUnits");
    }

    /**
     * Returns true if this data set is additive (i.e. makes one area visibly "hotter" rather than "colder").
     * @return as a boolean
     */
    public boolean isAdditive() {
        return mLayerOptions.getAsBoolean("isAdditive");
    }

    /**
     * Returns true if this data set is shown.
     * @return as a boolean
     */
    public boolean isShown() {
        return mLayerOptions.getAsBoolean("isShown");
    }

    /* ********************************************************************************************
     *                                           SETTERS                                          *
     ******************************************************************************************** */

    /**
     * Save layer options to database.
     */
    public void saveLayerOptions() {
        LayerOptionsTracker.UpdateStats(mDataType, new LayerOptionsTracker.LayerData(mLayerOptions));
    }

    /**
     * Set's the layer weight.
     * @param layerWeight of data set
     */
    public void setLayerWeight(float layerWeight) {
        if (mLayerOptions.containsKey("layerWeight")) {
            mLayerOptions.remove("layerWeight");
        }
        mLayerOptions.put("layerWeight", layerWeight);
    }

    /**
     * Sets the total units of this data set.
     * @param totalUnits of data set
     */
    public void setTotalUnits(long totalUnits) {
        if (mLayerOptions.containsKey("totalUnits")) {
            mLayerOptions.remove("totalUnits");
        }
        mLayerOptions.put("totalUnits", totalUnits);
    }

    /**
     * Sets whether this data set is additive or not.
     * @param isAdditive of data set
     */
    public void setIsAdditive(boolean isAdditive) {
        if (mLayerOptions.containsKey("isAdditive")) {
            mLayerOptions.remove("isAdditive");
        }
        mLayerOptions.put("isAdditive", isAdditive);
    }

    /**
     * Sets whether this data set is shown or not.
     * @param isShown of data set
     */
    public void setIsShown(boolean isShown) {
        if (mLayerOptions.containsKey("isShown")) {
            mLayerOptions.remove("isShown");
        }
        mLayerOptions.put("isShown", isShown);
    }

    /* ********************************************************************************************
     *                                   CALLBACK INTERFACE                                       *
     ******************************************************************************************** */

    /**
     * Callback function when map layer data is ready.
     */
    public interface OnMapLayerDataReadyCallback {

        /**
         * Called when map layer data is ready.
         * @param dataType of map layer
         * @param dataOrNull of map layer
         */
        void onMapLayerDataReady(DataType dataType, List<WeightedLatLng> dataOrNull);
    }
}
