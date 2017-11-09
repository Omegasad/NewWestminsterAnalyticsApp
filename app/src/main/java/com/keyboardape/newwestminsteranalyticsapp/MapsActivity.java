package com.keyboardape.newwestminsteranalyticsapp;

import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.data.GetPopulationDensityAsync;
import com.keyboardape.newwestminsteranalyticsapp.db.DBHelper;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GetPopulationDensityAsync.Callbacks {

    private static final LatLngBounds BOUNDARY = new LatLngBounds(
            new LatLng(49.162589, -122.957891), new LatLng(49.239221, -122.887576));
    private static final float DEFAULT_ZOOM_LEVEL = 13f;
    private static final int DEFAULT_HEATMAP_RADIUS = 32;

    private SQLiteDatabase mDB;
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
        // Set Readable SQLiteDatabase
        mDB = new DBHelper(this).getReadableDatabase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDB.close();
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
        // Load Population Density data
        new GetPopulationDensityAsync(mDB, this).execute();
    }

    /**
     * Load data onto map once it's downloaded.
     */
    @Override
    public void onReadPopulationDensityComplete(List<WeightedLatLng> dataOrNull) {
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
