package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.keyboardape.newwestminsteranalyticsapp.datautilities.DataManager;
import com.keyboardape.newwestminsteranalyticsapp.datasets.Data;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Data.OnDownloadCompleteCallback {

    private List<DataType> mDataSets;
    private Map<DataType, LinearLayout> mDataLabels;
    private Map<DataType, ProgressBar> mProgressBars;
    private Button mBtnViewmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataManager.Initialize(this);

        mDataSets = new ArrayList<>();
        mDataSets.add(DataType.POPULATION_DENSITY);
        mDataSets.add(DataType.SKYTRAIN_STATIONS);
        mDataSets.add(DataType.BUS_STOPS);
        mDataSets.add(DataType.BUSINESS_LICENSES);
        mDataSets.add(DataType.MAJOR_SHOPPINGS);

        mProgressBars = new HashMap<>();
        mProgressBars.put(DataType.POPULATION_DENSITY, (ProgressBar) findViewById(R.id.progressPopulationDensity));
        mProgressBars.put(DataType.SKYTRAIN_STATIONS, (ProgressBar) findViewById(R.id.progressSkytrainStations));
        mProgressBars.put(DataType.BUS_STOPS, (ProgressBar) findViewById(R.id.progressBusStops));
        mProgressBars.put(DataType.BUSINESS_LICENSES, (ProgressBar) findViewById(R.id.progressBusinessLicenses));
        mProgressBars.put(DataType.MAJOR_SHOPPINGS, (ProgressBar) findViewById(R.id.progressMajorShoppings));

        mDataLabels = new HashMap<>();
        mDataLabels.put(DataType.POPULATION_DENSITY, (LinearLayout) findViewById(R.id.labelPopulationDensity));
        mDataLabels.put(DataType.SKYTRAIN_STATIONS, (LinearLayout) findViewById(R.id.labelSkytrainStations));
        mDataLabels.put(DataType.BUS_STOPS, (LinearLayout) findViewById(R.id.labelBusStops));
        mDataLabels.put(DataType.BUSINESS_LICENSES, (LinearLayout) findViewById(R.id.labelBusinessLicenses));
        mDataLabels.put(DataType.MAJOR_SHOPPINGS, (LinearLayout) findViewById(R.id.labelMajorShoppings));

        mBtnViewmaps = (Button) findViewById(R.id.btnViewMaps);
        mBtnViewmaps.setEnabled(false);

        downloadDataSetOrEnableButtons();
    }

    public void onClickViewMaps(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    private void downloadDataSetOrEnableButtons() {
        if (mDataSets.size() > 0) {
            DataType type = mDataSets.get(0);
            Data data = DataManager.GetDataSetOrNull(type);
            if (data.isRequireUpdate()) {
                mProgressBars.get(type).setVisibility(View.VISIBLE);
                data.updateDataAsync(this);
            } else {
                mProgressBars.get(type).setVisibility(View.INVISIBLE);
                mDataLabels.get(type).setVisibility(View.INVISIBLE);
                mDataSets.remove(type);
                downloadDataSetOrEnableButtons();
            }
        } else {
            mBtnViewmaps.setEnabled(true);
        }
    }

    /**
     * Called when data is downloaded.
     * @param dataType downloaded
     * @param isSuccessful true if successful
     */
    @Override
    public void onDownloadComplete(DataType dataType, boolean isSuccessful) {
        if (!isSuccessful) {
            Toast.makeText(this, "Download data failed...", Toast.LENGTH_LONG).show();
        }

        mProgressBars.get(dataType).setVisibility(View.INVISIBLE);
        mDataLabels.get(dataType).setVisibility(View.INVISIBLE);
        mDataSets.remove(dataType);
        downloadDataSetOrEnableButtons();
    }
}
