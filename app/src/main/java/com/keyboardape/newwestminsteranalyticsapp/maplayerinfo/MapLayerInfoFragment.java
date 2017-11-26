package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MapLayerInfoFragment.
 *
 * Displayed in MapsActivity for some MapLayers.
 */
public abstract class MapLayerInfoFragment extends Fragment {

    private static boolean                                                  IsInitialized;
    private static Map<MapLayerType, MapLayerInfoFragment>                  LayerInfoFragmentInstances;
    private static Map<MapLayerType, Class<? extends MapLayerInfoFragment>> LayerInfoFragmentClasses;

    static {
        IsInitialized = false;
        LayerInfoFragmentInstances = new LinkedHashMap<>();

        LayerInfoFragmentClasses = new LinkedHashMap<>();
        LayerInfoFragmentClasses.put(MapLayerType.POPULATION_DENSITY, PopulationDensityFragment.class);
        LayerInfoFragmentClasses.put(MapLayerType.BUILDING_AGE,       BuildingAgeFragment.class);
    }

    public static void Initialize() {
        if (!IsInitialized) {
            IsInitialized = true;
            try {
                for (MapLayerType type : LayerInfoFragmentClasses.keySet()) {
                    LayerInfoFragmentInstances.put(type, LayerInfoFragmentClasses.get(type).newInstance());
                }
            } catch (Exception e) {
                Log.e(MapLayerInfoFragment.class.getSimpleName(), e.getMessage());
            }
        }
    }

    public static MapLayerInfoFragment GetFragmentOrNull(MapLayerType mapLayerType) {
        return LayerInfoFragmentInstances.get(mapLayerType);
    }

    // ---------------------------------------------------------------------------------------------
    //                                         INSTANCE
    // ---------------------------------------------------------------------------------------------

    public MapLayerInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_map_layer_info_population_dentisy, container, false);
//        ListView list = (ListView) v.findViewById(R.id.map_layer_info_list);
//        return v;
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
