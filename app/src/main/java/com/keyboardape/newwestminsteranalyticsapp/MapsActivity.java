package com.keyboardape.newwestminsteranalyticsapp;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.maps.GetWeightedCoordinatesTask;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String JSON_URL = "http://opendata.newwestcity.ca/downloads/building-attributes/BUILDING_ATTRIBUTES.json";
    private static final float  DEFAULT_ZOOM_LEVEL     = 12;
    private static final int    DEFAULT_HEATMAP_RADIUS = 32;

    private GoogleMap mMap;
    private Geocoder  mCoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Initialize map and heatmap components.
     * @param googleMap GoogleMap to initialize
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap   = googleMap;
        mCoder = new Geocoder(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(getLatLngFromAddress("New Westminster Canada")));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
        addHeatMap();
    }

    /**
     * Retrieves WeightedLatLng (weighted coordinates) and builds a heatmap based on the weights.
     */
    private void addHeatMap() {
        new GetWeightedCoordinatesTask(this, JSON_URL,
            new GetWeightedCoordinatesTask.OnComplete() {
                @Override
                public void doTask(List<WeightedLatLng> weightedCoordinates) {
                    if (weightedCoordinates != null) {
                        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                            .weightedData(weightedCoordinates)
                            .radius(DEFAULT_HEATMAP_RADIUS)
                            .build();

                        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    }
                }
            }
        ).execute();
    }

    /**
     * Returns a LatLng (latitude and longitude) given an address.
     * @param address to be geocoded into LatLng
     * @return LatLng
     */
    private LatLng getLatLngFromAddress(String address) {
        try {
            List<Address> addr = mCoder.getFromLocationName(address, 1);
            if (addr == null) {
                return null;
            } else {
                Address location = addr.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {
            return null;
        }
    }
}
