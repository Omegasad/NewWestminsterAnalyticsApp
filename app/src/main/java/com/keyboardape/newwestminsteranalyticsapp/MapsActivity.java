package com.keyboardape.newwestminsteranalyticsapp;

import android.support.v4.app.FragmentManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayer;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerType;

import java.util.List;

public class      MapsActivity
       extends    AppCompatActivity
       implements OnMapReadyCallback,
                  GoogleMap.OnMapClickListener {

    private static final LatLngBounds PANNING_BOUNDARY;
    private static final MapLayerType DEFAULT_MAP_LAYER_TYPE;
    private static final float        INITIAL_ZOOM_LEVEL;
    private static final float        MIN_ZOOM_LEVEL;
    private static final float        MAX_ZOOM_LEVEL;

    static {
        PANNING_BOUNDARY = new LatLngBounds(
                new LatLng(49.162589, -122.957891),
                new LatLng(49.239221, -122.887576)
                );
        DEFAULT_MAP_LAYER_TYPE = MapLayerType.POPULATION_DENSITY;
        INITIAL_ZOOM_LEVEL     = 13f;
        MIN_ZOOM_LEVEL         = 13f;
        MAX_ZOOM_LEVEL         = 14.5f;
    }

    private MapLayerType          mLastMapLayerType = null;
    private MapLayerType          mCurrentMapLayerType = null;

    private GoogleMap             mGMap;
    private Geocoder              mGeocoder;
    private MapLayersListFragment mMapLayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Setup toolbar and title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_maps));

        // Initialize data manager if not already initialized
        DataSet.Initialize(this);
        MapLayer.Initialize();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Map Layer Buttons Fragment
        mMapLayerFragment = new MapLayersListFragment();
        mMapLayerFragment.setActiveLayer(DEFAULT_MAP_LAYER_TYPE);
        FloatingActionButton openFabLayersBtn = (FloatingActionButton) findViewById(R.id.fab);
        openFabLayersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMapLayerFragment.isVisible()) {
                    hideMapLayersList();
                } else {
                    showMapLayersList();
                }
            }
        });
        openFabLayersBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!mMapLayerFragment.isVisible()) {
                    loadLayer(mLastMapLayerType);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        MapLayer.SetGoogleMap(googleMap);

        mGMap = googleMap;
        mGeocoder = new Geocoder(this);

        LatLng l = getLatLngFromAddress("NEW WESTMINSTER BC CANADA");
        mGMap.setOnMapClickListener(this);
        mGMap.setLatLngBoundsForCameraTarget(PANNING_BOUNDARY);
        mGMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l, INITIAL_ZOOM_LEVEL));
        mGMap.setMinZoomPreference(MIN_ZOOM_LEVEL);
        mGMap.setMaxZoomPreference(MAX_ZOOM_LEVEL);
        loadLayer(DEFAULT_MAP_LAYER_TYPE);
    }

    @Override
    public void onMapClick(LatLng point) {
        if (mCurrentMapLayerType != null) {
            mCurrentMapLayerType.getLayer().onMapClick(point);
        }
    }

    public void showMapLayersList() {
        FloatingActionButton openFabLayersBtn = (FloatingActionButton) findViewById(R.id.fab);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.overlay_fragment_container, mMapLayerFragment)
                .commit();
        mGMap.getUiSettings().setAllGesturesEnabled(false);
        openFabLayersBtn.setImageResource(R.drawable.ic_close_black_24dp);
    }

    public void hideMapLayersList() {
        FloatingActionButton openFabLayersBtn = (FloatingActionButton) findViewById(R.id.fab);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .remove(mMapLayerFragment)
                .commit();
        mGMap.getUiSettings().setAllGesturesEnabled(true);
        openFabLayersBtn.setImageResource(R.drawable.ic_layers_black_24dp);
    }

    public void loadLayer(MapLayerType mapLayerType) {
        if (mapLayerType == null || mapLayerType == mCurrentMapLayerType) {
            return;
        }

        // Update current/last layer types
        mLastMapLayerType = mCurrentMapLayerType;
        mCurrentMapLayerType = mapLayerType;
        mMapLayerFragment.setActiveLayer(mCurrentMapLayerType);

        // Hide last layer, show current layer
        if (mLastMapLayerType != null) {
            mLastMapLayerType.getLayer().hideLayer();
        }
        mCurrentMapLayerType.getLayer().showLayer();

        // Update activity subtitle
        getSupportActionBar().setSubtitle(getString(mCurrentMapLayerType.getLayer().getRStringIDLayerName()));
    }

    private LatLng getLatLngFromAddress(String address) {
        try {
            List<Address> addr = mGeocoder.getFromLocationName(address, 1);
            if (addr != null) {
                Address location = addr.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }
}
