// TODO: 9/29/2017 find out what barChart.invalidate(); does

package com.keyboardape.newwestminsteranalyticsapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class ChartActivity extends Activity {

    BarChart barChart;
    float barWidth;
    float barSpace;
    float groupSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Declarations
        barChart = (BarChart) findViewById(R.id.bargraph);
        barWidth = 0.3f;
        barSpace = 0f;
        groupSpace = 0.4f;

        //ArrayList of fields
        ArrayList labels = new ArrayList();
        labels.add("1986");
        labels.add("1991");
        labels.add("1996");
        labels.add("2001");

        // To store y-Axis of the data
        ArrayList<BarEntry> dummyEntries = new ArrayList<>();
        dummyEntries.add(new BarEntry(1,40f)); //Entries must be floats
        dummyEntries.add(new BarEntry(2,44f)); //Entries must be floats
        dummyEntries.add(new BarEntry(3,30f)); //Entries must be floats
        dummyEntries.add(new BarEntry(4,36f)); //Entries must be floats

        ArrayList<BarEntry> dummyEntries2 = new ArrayList<>();
        dummyEntries2.add(new BarEntry(1,26f));
        dummyEntries2.add(new BarEntry(2,58f));
        dummyEntries2.add(new BarEntry(3,13f));
        dummyEntries2.add(new BarEntry(4,48f));

        // Is needed so the data can be outputted
        BarDataSet dummySet1 = new BarDataSet(dummyEntries,"Dummy Entries 1");
        BarDataSet dummySet2 = new BarDataSet(dummyEntries2,"Dummy Entries 2");

        // Changes the color of the bar
        dummySet1.setColor(Color.CYAN);
        dummySet2.setColor(Color.BLACK);

        BarData dummyData = new BarData(dummySet1,dummySet2);

        barChart.setData(dummyData); // Sets the data and list into the chart

        barChart.getBarData().setBarWidth(barWidth); // Sets the width of the chart
        barChart.getXAxis().setAxisMinimum(0); // Shows the mininum of the chart

        // Shows the maximum amount of the width and height, 4 because there are 4 entries
        barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * 4);
        barChart.groupBars(0, groupSpace, barSpace); // Helps splits the bars
        barChart.invalidate(); // don't know what this does

        barChart.getData().setHighlightEnabled(false); // Prevents tapping on the bars
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
    }
}
