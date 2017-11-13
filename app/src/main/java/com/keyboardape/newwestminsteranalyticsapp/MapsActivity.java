package com.keyboardape.newwestminsteranalyticsapp;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.datautilities.DataManager;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataType;
import com.keyboardape.newwestminsteranalyticsapp.maps.MapLayer;
import com.keyboardape.newwestminsteranalyticsapp.maps.PopulationDensityLayer;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, MapLayer.OnMapLayerDataReadyCallback {

    private static final LatLngBounds BOUNDARY = new LatLngBounds(
            new LatLng(49.162589, -122.957891), new LatLng(49.239221, -122.887576));
    private static final float DEFAULT_ZOOM_LEVEL = 13f;
    private static final int DEFAULT_HEATMAP_RADIUS = 32;

    private GoogleMap mMap;
    private Geocoder  mCoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Setup toolbar and title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_maps));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize data manager if not already initialized
        DataManager.Initialize(this);
    }

    /**
     * Initialize map and heatmap components.
     * @param googleMap GoogleMap to initialize
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mCoder = new Geocoder(this);

        // Center camera on New Westminster
        LatLng l = getLatLngFromAddress("NEW WESTMINSTER BC CANADA");
        mMap.setLatLngBoundsForCameraTarget(BOUNDARY);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l, DEFAULT_ZOOM_LEVEL));

        new PopulationDensityLayer().getMapDataAsync(this);
    }

    /**
     * Called when data is read from the database, success or fail.
     * @param dataType type of data set
     * @param dataOrNull data object or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onMapLayerDataReady(DataType dataType, List<WeightedLatLng> dataOrNull) {
        if (dataOrNull != null) {
            HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                    .weightedData(dataOrNull)
                    .radius(DEFAULT_HEATMAP_RADIUS)
                    .build();
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
        }
    }

    /**
     * Returns a LatLng (latitude and longitude) given an address.
     * @param address to be geocoded into LatLng
     * @return LatLng
     */
    private LatLng getLatLngFromAddress(String address) {
        try {
            List<Address> addr = mCoder.getFromLocationName(address, 1);
            if (addr != null) {
                Address location = addr.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {}
        return null;
    }
}
