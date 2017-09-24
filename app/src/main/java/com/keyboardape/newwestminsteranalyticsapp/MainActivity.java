// TODO: 9/24/2017 get the X-Axis working

package com.keyboardape.newwestminsteranalyticsapp;

import android.app.Activity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class MainActivity extends Activity {

    BarChart barChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barChart = (BarChart) findViewById(R.id.bargraph);

        // To store y-Axis of the data
        ArrayList<BarEntry> dummyEntries = new ArrayList<>();
        dummyEntries.add(new BarEntry(1,40f)); //Entries must be floats
        dummyEntries.add(new BarEntry(2,44f)); //Entries must be floats
        dummyEntries.add(new BarEntry(3,30f)); //Entries must be floats
        dummyEntries.add(new BarEntry(4,36f)); //Entries must be floats

        BarDataSet dummySet1 = new BarDataSet(dummyEntries,"Dummy Entries 1");
        BarData dummyData = new BarData(dummySet1);

        barChart.setData(dummyData); // Sets the data and list into the chart

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
    }
}
