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
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayer;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerType;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DataManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, MapLayer.OnMapLayerDataReadyCallback {

    private static final LatLngBounds BOUNDARY;
    private static final MapLayerType DEFAULT_MAP_LAYER;
    private static final int          DEFAULT_HEATMAP_RADIUS;
    private static final float        INITIAL_ZOOM_LEVEL;
    private static final float        MIN_ZOOM_LEVEL;
    private static final float        MAX_ZOOM_LEVEL;

    static {
        BOUNDARY = new LatLngBounds(new LatLng(49.162589, -122.957891), new LatLng(49.239221, -122.887576));
        DEFAULT_MAP_LAYER = MapLayerType.POPULATION_DENSITY;
        DEFAULT_HEATMAP_RADIUS = 32;
        INITIAL_ZOOM_LEVEL = 13f;
        MIN_ZOOM_LEVEL = 13f;
        MAX_ZOOM_LEVEL = 14.5f;
    }

    private Map<MapLayerType, TileOverlay> mTileOverlays;
    private MapLayerType      mLastMapLayer = null;
    private MapLayerType      mCurrentMapLayer = null;
    private Integer           mHeatmapRadius = DEFAULT_HEATMAP_RADIUS;

    private GoogleMap         mMap;
    private Geocoder          mCoder;
    private MapLayersFragment mMapLayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Setup toolbar and title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_maps));

        // Initialize data manager if not already initialized
        DataManager.Initialize(this);

        mTileOverlays = new HashMap<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // FAB Layer Buttons
        mMapLayerFragment = new MapLayersFragment();
        mMapLayerFragment.setActiveLayer(MapLayerType.POPULATION_DENSITY);
        FloatingActionButton openFabLayersBtn = (FloatingActionButton) findViewById(R.id.fab);
        openFabLayersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMapLayerFragment();
            }
        });
        openFabLayersBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!mMapLayerFragment.isVisible()) {
                    loadLayer(mLastMapLayer);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mMap = googleMap;
        mCoder = new Geocoder(this);

        LatLng l = getLatLngFromAddress("NEW WESTMINSTER BC CANADA");
        mMap.setLatLngBoundsForCameraTarget(BOUNDARY);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l, INITIAL_ZOOM_LEVEL));
        mMap.setMinZoomPreference(MIN_ZOOM_LEVEL);
        mMap.setMaxZoomPreference(MAX_ZOOM_LEVEL);
        loadLayer(DEFAULT_MAP_LAYER);
    }

    @Override
    public void onMapLayerDataReady(MapLayerType mapLayerType, List<WeightedLatLng> dataOrNull) {
        if (dataOrNull != null) {
            TileOverlay tileOverlay;
            if ((tileOverlay = mTileOverlays.get(mLastMapLayer)) != null) {
                tileOverlay.setVisible(false);
            }
            if ((tileOverlay = mTileOverlays.get(mapLayerType)) == null) {
                HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                        .weightedData(dataOrNull)
                        .radius(mHeatmapRadius)
                        .build();
                tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().fadeIn(false).tileProvider(provider));
                mTileOverlays.put(mapLayerType, tileOverlay);
            } else {
                tileOverlay.setVisible(true);
            }
        }
    }

    public void toggleMapLayerFragment() {
        FloatingActionButton openFabLayersBtn = (FloatingActionButton) findViewById(R.id.fab);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (mMapLayerFragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .remove(mMapLayerFragment)
                    .commit();
            openFabLayersBtn.setImageResource(R.drawable.ic_layers_black_24dp);
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.overlay_fragment_container, mMapLayerFragment)
                    .commit();
            openFabLayersBtn.setImageResource(R.drawable.ic_close_black_24dp);
        }
    }

    public void loadLayer(MapLayerType mapLayerType) {
        if (mapLayerType == null || mapLayerType == mCurrentMapLayer) {
            return;
        }

        MapOptions mapOptions = mapLayerType.getLayer().getMapOptions();
        mLastMapLayer         = mCurrentMapLayer;
        mCurrentMapLayer      = mapLayerType;
        mHeatmapRadius        = mapOptions.HeatmapRadius;

        mMapLayerFragment.setActiveLayer(mapLayerType);
        getSupportActionBar().setSubtitle(mapLayerType.getLayer().getResourceIDForLayerName());
        mapLayerType.getLayer().getMapDataAsync(this);
    }

    private LatLng getLatLngFromAddress(String address) {
        try {
            List<Address> addr = mCoder.getFromLocationName(address, 1);
            if (addr != null) {
                Address location = addr.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }

    public static class MapOptions {
        public int HeatmapRadius = DEFAULT_HEATMAP_RADIUS;
        public MapOptions setHeatmapRadius(int i) {
            HeatmapRadius = i;
            return this;
        }
    }
}
