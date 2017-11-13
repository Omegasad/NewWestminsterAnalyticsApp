//package com.keyboardape.newwestminsteranalyticsapp.maps;
//
//import com.keyboardape.newwestminsteranalyticsapp.datasets.DataType;
//import com.keyboardape.newwestminsteranalyticsapp.datautilities.DataManager;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class MapLayersLoader {
//
//    private static final Map<DataType, Class<? extends MapLayer>> LAYER_CLASSES;
//
//    static {
//        LAYER_CLASSES = new HashMap<>();
//        LAYER_CLASSES.put(DataType.POPULATION_DENSITY, PopulationDensityLayer.class);
//    }
//
//    private DataManager mDataManager;
//    private Map<DataType, MapLayer> mLayers;
//
//    public MapLayersLoader() {
//        mDataManager = DataManager.GetInstance();
//        mLayers = new HashMap<>();
//
////        String tableName = mDataManager.GetDataSetOrNull(DataType.POPULATION_DENSITY).getTableName();
////        MapLayer popDensity = new MapLayer(DataType.POPULATION_DENSITY);
////        popDensity.setLayerWeight(0.2f);
////        popDensity.setIsAdditive(true);
////        popDensity.setTotalUnits((long)DataSetTracker.GetStatsOrNull(DataType.POPULATION_DENSITY).get("totalUnits"));
////
////        addLayer(DataType.POPULATION_DENSITY, popDensity);
//    }
//
//    public void addLayer(DataType type, MapLayer layer) {
//        if (!mLayers.containsKey(type)) {
//            mLayers.put(type, layer);
//        }
//    }
//
//    public void getAllMapDataAsync(MapLayer.OnMapLayerDataReadyCallback callback) {
////        mLayers.get(DataType.POPULATION_DENSITY).getMapDataAsync(callback, DataManager.GetSumTotalUnits());
//    }
//}
