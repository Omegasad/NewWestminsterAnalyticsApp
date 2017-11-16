package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerType;

import java.util.HashMap;
import java.util.Map;

public class MapLayersFragment extends Fragment implements View.OnClickListener {

    private Map<MapLayerType, LinearLayout>         mLayerRows;
    private Map<MapLayerType, FloatingActionButton> mLayerButtons;
    private MapLayerType                            mActiveLayerType;

    public MapLayersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_layers, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mLayerRows = new HashMap<>();
        mLayerRows.put(MapLayerType.POPULATION_DENSITY, (LinearLayout) view.findViewById(R.id.populationDensityLayerRow));
        mLayerRows.put(MapLayerType.BUILDING_AGE, (LinearLayout) view.findViewById(R.id.buildingAgeLayerRow));
        mLayerRows.put(MapLayerType.HIGH_RISES, (LinearLayout) view.findViewById(R.id.highRisesLayerRow));

        mLayerButtons = new HashMap<>();
        mLayerButtons.put(MapLayerType.POPULATION_DENSITY, (FloatingActionButton) view.findViewById(R.id.populationDensityLayerBtn));
        mLayerButtons.put(MapLayerType.BUILDING_AGE, (FloatingActionButton) view.findViewById(R.id.buildingAgeLayerBtn));
        mLayerButtons.put(MapLayerType.HIGH_RISES, (FloatingActionButton) view.findViewById(R.id.highRisesLayerBtn));

        // Highlight active layer gray
        mLayerButtons.get(mActiveLayerType).setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

        // Set onclick listners
        for (Map.Entry<MapLayerType, LinearLayout> entry : mLayerRows.entrySet()) {
            entry.getValue().setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        for (Map.Entry<MapLayerType, LinearLayout> entry : mLayerRows.entrySet()) {
            if (entry.getValue() == v) {
                MapsActivity activity = (MapsActivity) getActivity();
                MapLayerType type = entry.getKey();
                activity.loadLayer(type);
                activity.toggleMapLayerFAB();
            }
        }
    }

    public void setActiveLayer(MapLayerType mapLayerType) {
        mActiveLayerType = mapLayerType;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
