package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mCoder = new Geocoder(this);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(getLatLngFromAddress("New Westminster Canada")));
        addHeatMap();
    }

    private void addHeatMap() {
        List<LatLng> list = null;
        try {
            list = getList();
        } catch (Exception e) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
        }

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();

        TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private ArrayList<LatLng> getList() {
        ArrayList<LatLng> list = new ArrayList<LatLng>();

        list.add(getLatLngFromAddress("37 AGNES ST New Westminster Canada"));
        list.add(getLatLngFromAddress("251 PHILLIPS ST New Westminster Canada"));
        list.add(getLatLngFromAddress("219 PHILLIPS ST New Westminster Canada"));
        list.add(getLatLngFromAddress("200 PHILLIPS ST New Westminster Canada"));
        list.add(getLatLngFromAddress("110 PHILLIPS ST New Westminster Canada"));
        list.add(getLatLngFromAddress("333 PHILLIPS ST New Westminster Canada"));
        list.add(getLatLngFromAddress("820 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("800 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("811 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("821 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("833 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("777 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("650 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("776 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("774 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("773 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("772 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("771 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("770 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("779 THIRTEENTH ST New Westminster Canada"));
        list.add(getLatLngFromAddress("820 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("800 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("811 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("821 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("833 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("777 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("650 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("776 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("774 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("773 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("772 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("771 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("770 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("779 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("620 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("600 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("611 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("621 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("633 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("677 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("650 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("676 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("674 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("673 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("672 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("671 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("670 WELLS GRAY PL New Westminster Canada"));
        list.add(getLatLngFromAddress("679 WELLS GRAY PL New Westminster Canada"));

        return list;
    }

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
