package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.keyboardape.newwestminsteranalyticsapp.data.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.db.DBConsts;
import com.keyboardape.newwestminsteranalyticsapp.downloaders.DownloadBusStopsAsync;
import com.keyboardape.newwestminsteranalyticsapp.downloaders.DownloadBusinessLicensesAsync;
import com.keyboardape.newwestminsteranalyticsapp.downloaders.DownloadMajorShoppingsAsync;
import com.keyboardape.newwestminsteranalyticsapp.downloaders.DownloadSkytrainStationsAsync;
import com.keyboardape.newwestminsteranalyticsapp.downloaders.JsonDownloaderAsync;
import com.keyboardape.newwestminsteranalyticsapp.downloaders.DownloadPopulationDensityAsync;
import com.keyboardape.newwestminsteranalyticsapp.db.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
    implements JsonDownloaderAsync.Callbacks {

    private SQLiteDatabase mDB;
    private List<DataSet> mDataSets;
    private Map<DataSet, JsonDownloaderAsync> mDownloadTasks;
    private Map<DataSet, LinearLayout> mDataLabels;
    private Map<DataSet, ProgressBar> mProgressBars;
    private Button mBtnViewmaps,mBtnCharts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDB = new DBHelper(this).getWritableDatabase();

        mDataSets = new ArrayList<>();
        mDataSets.add(DataSet.POPULATION_DENSITY);
        mDataSets.add(DataSet.SKYTRAIN_STATIONS);
        mDataSets.add(DataSet.BUS_STOPS);
        mDataSets.add(DataSet.BUSINESS_LICENSES);
        mDataSets.add(DataSet.MAJOR_SHOPPINGS);

        mDownloadTasks = new HashMap<>();
        mDownloadTasks.put(DataSet.POPULATION_DENSITY, new DownloadPopulationDensityAsync(mDB, this));
        mDownloadTasks.put(DataSet.SKYTRAIN_STATIONS, new DownloadSkytrainStationsAsync(mDB, this));
        mDownloadTasks.put(DataSet.BUS_STOPS, new DownloadBusStopsAsync(mDB, this));
        mDownloadTasks.put(DataSet.BUSINESS_LICENSES, new DownloadBusinessLicensesAsync(this, mDB, this));
        mDownloadTasks.put(DataSet.MAJOR_SHOPPINGS, new DownloadMajorShoppingsAsync(mDB, this));

        mProgressBars = new HashMap<>();
        mProgressBars.put(DataSet.POPULATION_DENSITY, (ProgressBar) findViewById(R.id.progressPopulationDensity));
        mProgressBars.put(DataSet.SKYTRAIN_STATIONS, (ProgressBar) findViewById(R.id.progressSkytrainStations));
        mProgressBars.put(DataSet.BUS_STOPS, (ProgressBar) findViewById(R.id.progressBusStops));
        mProgressBars.put(DataSet.BUSINESS_LICENSES, (ProgressBar) findViewById(R.id.progressBusinessLicenses));
        mProgressBars.put(DataSet.MAJOR_SHOPPINGS, (ProgressBar) findViewById(R.id.progressMajorShoppings));

        mDataLabels = new HashMap<>();
        mDataLabels.put(DataSet.POPULATION_DENSITY, (LinearLayout) findViewById(R.id.labelPopulationDensity));
        mDataLabels.put(DataSet.SKYTRAIN_STATIONS, (LinearLayout) findViewById(R.id.labelSkytrainStations));
        mDataLabels.put(DataSet.BUS_STOPS, (LinearLayout) findViewById(R.id.labelBusStops));
        mDataLabels.put(DataSet.BUSINESS_LICENSES, (LinearLayout) findViewById(R.id.labelBusinessLicenses));
        mDataLabels.put(DataSet.MAJOR_SHOPPINGS, (LinearLayout) findViewById(R.id.labelMajorShoppings));

        mBtnViewmaps = (Button) findViewById(R.id.btnViewMaps);
        mBtnViewmaps.setEnabled(false);

        mBtnCharts = (Button) findViewById(R.id.btnViewCharts);
        mBtnCharts.setEnabled(false);

        removeUneccessaryDownloadTasks(mDataSets);
        downloadDataSetOrEnableButtons();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDB.close();
    }

    public void onClickViewMaps(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void onClickViewCharts(View v) {
        Intent i = new Intent(this, ChartActivity.class);
        startActivity(i);
    }

    private void removeUneccessaryDownloadTasks(List<DataSet> list) {
        Iterator<DataSet> it = list.iterator();
        while (it.hasNext()) {
            DataSet dataSet = it.next();
            if (!isDataSetNeedUpdate(dataSet)) {
                mDataLabels.get(dataSet).setVisibility(View.INVISIBLE);
                it.remove();
            }
        }
    }

    private boolean isDataSetNeedUpdate(DataSet dataSet) {
        Cursor cursor = mDB.rawQuery(DataSet.TRACKER.SQL_GET_TABLE +
            "WHERE tableName = '" + dataSet.TABLE_NAME + "'", null);
        boolean isNeedUpdate = true;
        try {
            if (cursor.moveToFirst()) {
                isNeedUpdate = cursor.getInt(1) == DBConsts.TRUE;
            }
        } catch (Exception e) {}
        cursor.close();
        return isNeedUpdate;
    }

    private void downloadDataSetOrEnableButtons() {
        if (mDataSets.size() > 0) {
            DataSet ds = mDataSets.get(0);
            mDownloadTasks.get(ds).execute();
            mProgressBars.get(ds).setVisibility(View.VISIBLE);
        } else {
            mBtnViewmaps.setEnabled(true);
            mBtnCharts.setEnabled(true);
        }
    }

    @Override
    public void onDownloadSuccess(DataSet dataSet) {
        mProgressBars.get(dataSet).setVisibility(View.INVISIBLE);
        mDataLabels.get(dataSet).setVisibility(View.INVISIBLE);
        mDataSets.remove(dataSet);
        downloadDataSetOrEnableButtons();
    }

    @Override
    public void onDownloadFailed(DataSet dataSet) {
        Toast.makeText(this, "Download data failed...", Toast.LENGTH_LONG).show();
        mProgressBars.get(dataSet).setVisibility(View.INVISIBLE);
        mDataSets.remove(dataSet);
        downloadDataSetOrEnableButtons();
    }
}
