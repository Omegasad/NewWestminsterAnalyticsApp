package com.keyboardape.newwestminsteranalyticsapp;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;

import java.util.ArrayList;

public class Demo_2001_activity extends DBActivity {

    BarChart barChart;
    float barWidth;
    float barSpace;
    float groupSpace;

    private Cursor cursor;
    private SQLiteDatabase db;
    int count;
    int[] male;
    int[] female;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_2001_activity);
        readDb();
        printChart();
    }

    private void readDb()
    {
        //Get the SQL statement
        SQLiteOpenHelper helper = DBHelper.GetInstance();
        try {
            db = helper.getReadableDatabase();
            cursor = db.rawQuery("select * from age_demographics where year = 2001",null);
            count = cursor.getCount();
            female = new int[count];
            male = new int[count];
            if (cursor.moveToFirst()) {
                int ndx = 0;
                do {
                    female[ndx] = cursor.getInt(4);
                    male[ndx++] = cursor.getInt(5);
                } while (cursor.moveToNext());
            }

            for (int i = 0; i < female.length;i++) {
                System.out.println(female[i]);
            }
        } catch (SQLiteException sqlex) {
            String msg = "[MainActivity / getContinents] DB unavailable";
            msg += "\n\n" + sqlex.toString();

            Toast t = Toast.makeText(this, msg, Toast.LENGTH_LONG);
            t.show();
        }
    }

    private void printChart()
    {
        // Declarations
        barChart = (BarChart) findViewById(R.id.bargraph);
        barWidth = 0.3f;
        barSpace = 0f;
        groupSpace = 0.4f;

        // Set animation to make it cool
        barChart.animateY(3000);

        // X-Axis settings
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawLabels(false);

        // Y-Axis settings
        barChart.getAxisLeft().setAxisMinimum(0); // Starts at 0
        barChart.getAxisRight().setAxisMinimum(0); // Starts at 0

        //Make the description empty
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        // Populate the male chart
        ArrayList<BarEntry> dummyEntries = new ArrayList<>();
        for (int i = 0; i < male.length; i++) {
            dummyEntries.add(new BarEntry(i+1, male[i]));
        }

        // Populate the female chart
        ArrayList<BarEntry> dummyEntries2 = new ArrayList<>();
        for (int i = 0; i < female.length; i++) {
            dummyEntries2.add(new BarEntry(i+1,female[i]));
        }

        // Is needed so the data can be outputted
        BarDataSet dummySet1 = new BarDataSet(dummyEntries,"Male");
        BarDataSet dummySet2 = new BarDataSet(dummyEntries2,"Female");

        // Changes the color of the bar
        dummySet1.setColor(Color.CYAN);
        dummySet2.setColor(Color.BLACK);

        BarData dummyData = new BarData(dummySet1,dummySet2);

        barChart.setData(dummyData); // Sets the data and list into the chart

        barChart.getBarData().setBarWidth(barWidth); // Sets the width of the chart
        barChart.getXAxis().setAxisMinimum(0); // Shows the mininum of the chart

        // Shows the maximum amount of the width and height, 4 because there are 4 entries
        barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * count);
        barChart.groupBars(0, groupSpace, barSpace); // Helps splits the bars
        barChart.invalidate(); // don't know what this does

        barChart.getData().setHighlightEnabled(false); // Prevents tapping on the bars
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setFitBars(true); // makes the x-axis fit exactly all bars
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.invalidate(); // refreshes (redraws the chart)

    }
}
