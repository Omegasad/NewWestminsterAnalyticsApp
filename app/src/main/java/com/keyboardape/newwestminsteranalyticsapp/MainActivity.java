package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetAdapter;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;

public class MainActivity extends AppCompatActivity implements DataSet.OnDataSetUpdatedCallback {

    private DataSet[] mDataSets;
    private int       mCurrentDataSet;

    private ListView mDownloadList;
    private DataSetAdapter mDataSetAdapter;
    private Button   mBtnViewMaps;
    private Button   mBtnViewCharts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHelper.Initialize(this);
        DataSet.Initialize(this);

        DataSetType.BUS_STOPS.getDataSet().setRequireUpdate(true);
        DataSetType.HIGH_RISES.getDataSet().setRequireUpdate(true);

        mDataSets = DataSet.GetAllDataSets();
        mCurrentDataSet = 0;

        mDataSetAdapter = new DataSetAdapter(this, mDataSets);
        mDownloadList = (ListView) findViewById(R.id.downloadList);
        mDownloadList.setAdapter(mDataSetAdapter);

        mBtnViewMaps = (Button) findViewById(R.id.btnViewMaps);
        mBtnViewMaps.setEnabled(false);

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
        if (mCurrentDataSet++ < mDataSets.length) {
            DataSet data = mDataSets[mCurrentDataSet - 1];
            if (data.isRequireUpdate()) {
                data.updateDataAsync(this);
            } else {
                downloadDataSetOrEnableButtons();
            }
        } else {
            mBtnViewMaps.setEnabled(true);
            mBtnViewCharts.setEnabled(true);
        }
    }

    @Override
    public void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful) {
        if (!isUpdateSuccessful) {
            Toast.makeText(this, "Download data failed...", Toast.LENGTH_LONG).show();
        }
        mDataSetAdapter.notifyDataSetInvalidated();
        mDataSetAdapter.notifyDataSetChanged();

//        mDownloadList.setAdapter(mDataSetAdapter);
//        mProgressBars.get(dataSetType).setVisibility(View.INVISIBLE);
//        mDataLabels.get(dataSetType).setVisibility(View.INVISIBLE);
//        mDataSets.remove(dataSetType);
        downloadDataSetOrEnableButtons();
    }
}
