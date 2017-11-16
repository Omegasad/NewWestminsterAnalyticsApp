package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DataSet.OnDataSetUpdatedCallback {

    private List<DataSetType> mDataSets;
    private Map<DataSetType, LinearLayout> mDataLabels;
    private Map<DataSetType, ProgressBar> mProgressBars;
    private Button mBtnViewmaps;
    private Button mBtnViewCharts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataManager.Initialize(this);

        mDataSets = new ArrayList<>();
        mDataSets.add(DataSetType.BUILDING_ATTRIBUTES);
        mDataSets.add(DataSetType.SKYTRAIN_STATIONS);
        mDataSets.add(DataSetType.BUS_STOPS);
        mDataSets.add(DataSetType.BUSINESS_LICENSES);
        mDataSets.add(DataSetType.MAJOR_SHOPPING);
        mDataSets.add(DataSetType.BUILDING_AGE);
        mDataSets.add(DataSetType.HIGH_RISES);

        mProgressBars = new HashMap<>();
        mProgressBars.put(DataSetType.BUILDING_ATTRIBUTES, (ProgressBar) findViewById(R.id.progressPopulationDensity));
        mProgressBars.put(DataSetType.SKYTRAIN_STATIONS, (ProgressBar) findViewById(R.id.progressSkytrainStations));
        mProgressBars.put(DataSetType.BUS_STOPS, (ProgressBar) findViewById(R.id.progressBusStops));
        mProgressBars.put(DataSetType.BUSINESS_LICENSES, (ProgressBar) findViewById(R.id.progressBusinessLicenses));
        mProgressBars.put(DataSetType.MAJOR_SHOPPING, (ProgressBar) findViewById(R.id.progressMajorShoppings));
        mProgressBars.put(DataSetType.BUILDING_AGE, (ProgressBar) findViewById(R.id.progressBuildingAge));
        mProgressBars.put(DataSetType.HIGH_RISES, (ProgressBar) findViewById(R.id.progressHighRises));

        mDataLabels = new HashMap<>();
        mDataLabels.put(DataSetType.BUILDING_ATTRIBUTES, (LinearLayout) findViewById(R.id.labelPopulationDensity));
        mDataLabels.put(DataSetType.SKYTRAIN_STATIONS, (LinearLayout) findViewById(R.id.labelSkytrainStations));
        mDataLabels.put(DataSetType.BUS_STOPS, (LinearLayout) findViewById(R.id.labelBusStops));
        mDataLabels.put(DataSetType.BUSINESS_LICENSES, (LinearLayout) findViewById(R.id.labelBusinessLicenses));
        mDataLabels.put(DataSetType.MAJOR_SHOPPING, (LinearLayout) findViewById(R.id.labelMajorShoppings));
        mDataLabels.put(DataSetType.BUILDING_AGE, (LinearLayout) findViewById(R.id.labelBuildingAge));
        mDataLabels.put(DataSetType.HIGH_RISES, (LinearLayout) findViewById(R.id.labelHighRises));

        mBtnViewmaps = (Button) findViewById(R.id.btnViewMaps);
        mBtnViewmaps.setEnabled(false);

        mBtnViewCharts = (Button) findViewById(R.id.btnViewCharts);
        mBtnViewCharts.setEnabled(false);

        downloadDataSetOrEnableButtons();
    }

    public void onClickViewMaps(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void onClickViewCharts(View v) {
        Intent i = new Intent(this, ChartActivity.class);
        startActivity(i);
    }

    private void downloadDataSetOrEnableButtons() {
        if (mDataSets.size() > 0) {
            DataSetType type = mDataSets.get(0);
            DataSet data = DataManager.GetDataSet(type);
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
            mBtnViewCharts.setEnabled(true);
        }
    }

    @Override
    public void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful) {
        if (!isUpdateSuccessful) {
            Toast.makeText(this, "Download data failed...", Toast.LENGTH_LONG).show();
        }

        mProgressBars.get(dataSetType).setVisibility(View.INVISIBLE);
        mDataLabels.get(dataSetType).setVisibility(View.INVISIBLE);
        mDataSets.remove(dataSetType);
        downloadDataSetOrEnableButtons();
    }
}
