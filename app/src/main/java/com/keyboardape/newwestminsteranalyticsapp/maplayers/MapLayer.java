package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapLayer.
 */
public abstract class MapLayer {

    // ---------------------------------------------------------------------------------------------
    //                                   STATIC : INITIALIZATION
    // ---------------------------------------------------------------------------------------------

    private static boolean                                      IsInitialized;
    private static GoogleMap                                    GMap;
    private static Map<MapLayerType, MapLayer>                  LayerInstances;
    private static Map<MapLayerType, Class<? extends MapLayer>> LayerClasses;

    static {
        GMap = null;
        LayerInstances = new HashMap<>();
        LayerClasses = new HashMap<>();
        LayerClasses.put(MapLayerType.POPULATION_DENSITY, PopulationDensityLayer.class);
        LayerClasses.put(MapLayerType.BUILDING_AGE,       BuildingAgeLayer.class);
        LayerClasses.put(MapLayerType.HIGH_RISES,         HighRisesLayer.class);
    }

    public static synchronized void Initialize() {
        if (!IsInitialized) {
            IsInitialized = true;
            try {
                for (MapLayerType type : GetAllMapLayerTypes()) {
                    LayerInstances.put(type, LayerClasses.get(type).newInstance());
                }
            } catch (Exception e) {
                Log.e(MapLayer.class.getSimpleName(), e.getMessage());
            }
        }
    }

    public static void SetGoogleMap(GoogleMap gMap) {
        GMap = gMap;
    }

    // ---------------------------------------------------------------------------------------------
    //                                     STATIC : GETTERS
    // ---------------------------------------------------------------------------------------------

    public static MapLayer GetLayer(MapLayerType mapLayerType) {
        return LayerInstances.get(mapLayerType);
    }

    public static MapLayerType[] GetAllMapLayerTypes() {
        return LayerClasses.keySet().toArray(new MapLayerType[LayerClasses.size()]);
    }

    public static MapLayer[] GetAllMapLayers() {
        return LayerInstances.values().toArray(new MapLayer[LayerInstances.size()]);
    }

    // ---------------------------------------------------------------------------------------------
    //                                         INSTANCE
    // ---------------------------------------------------------------------------------------------

    private MapLayerType mLayerType;
    private int          mRStringIDLayerName;
    private int          mRDrawableIDIcon;
    private int          mHeatmapRadius;
    private TileOverlay  mTileOverlay;

    public MapLayer(MapLayerType layerType, int rStringIDLayerName, int rDrawableIDIcon, int heatmapRadius) {
        mLayerType          = layerType;
        mRStringIDLayerName = rStringIDLayerName;
        mRDrawableIDIcon    = rDrawableIDIcon;
        mHeatmapRadius      = heatmapRadius;
        mTileOverlay        = null;
    }

    public MapLayerType getMapLayerType() {
        return mLayerType;
    }

    public int getRStringIDLayerName() {
        return mRStringIDLayerName;
    }

    public int getRDrawableIDIcon() {
        return mRDrawableIDIcon;
    }

    public void showLayer() {
        if (mTileOverlay == null) {
            getMapDataAsync(new OnMapLayerDataReadyCallback() {
                @Override
                public void onMapLayerDataReady(MapLayerType mapLayerType, List<WeightedLatLng> dataOrNull) {
                    HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                            .weightedData(dataOrNull)
                            .radius(mHeatmapRadius)
                            .build();
                    mTileOverlay = GMap.addTileOverlay(
                            new TileOverlayOptions()
                                    .fadeIn(false)
                                    .tileProvider(provider)
                            );
                }
            });
        } else {
            mTileOverlay.setVisible(true);
        }
    }

    public void hideLayer() {
        if (mTileOverlay != null) {
            mTileOverlay.setVisible(false);
        }
    }

    abstract public void onMapClick(LatLng point);

    abstract public void getMapDataAsync(final OnMapLayerDataReadyCallback callback);

    // ---------------------------------------------------------------------------------------------
    //                                  CALLBACK: ON MAP LAYER DATA READY
    // ---------------------------------------------------------------------------------------------

    protected interface OnMapLayerDataReadyCallback {
        void onMapLayerDataReady(MapLayerType mapLayerType, List<WeightedLatLng> dataOrNull);
    }
}
