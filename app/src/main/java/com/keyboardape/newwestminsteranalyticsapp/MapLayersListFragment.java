package com.keyboardape.newwestminsteranalyticsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayer;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerAdapter;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerType;

public class MapLayersListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private MapLayerType mActiveLayerType;

    public MapLayersListFragment() {
        mActiveLayerType = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_layers_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView mapLayersList = (ListView) view.findViewById(R.id.mapLayersList);
        mapLayersList.setAdapter(new MapLayerAdapter(getContext(), MapLayer.GetAllMapLayers(), mActiveLayerType));
        mapLayersList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MapsActivity activity = (MapsActivity) getActivity();
        activity.loadLayer(getMapLayerTypeFromListPosition(position));
        activity.hideMapLayersList();
    }

    private MapLayerType getMapLayerTypeFromListPosition(int position) {
        return MapLayer.GetAllMapLayerTypes()[position];
    }

    public void setActiveLayer(MapLayerType mapLayerType) {
        mActiveLayerType = mapLayerType;
    }
}
