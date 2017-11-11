// TODO: 9/29/2017 find out what barChart.invalidate(); does

package com.keyboardape.newwestminsteranalyticsapp;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.keyboardape.newwestminsteranalyticsapp.db.DBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChartActivity extends AppCompatActivity {

    BarChart barChart;
    float barWidth;
    float barSpace;
    float groupSpace;
    private SQLiteDatabase db;
    private Cursor cursor;
    Map<String,Float> graphCount = new HashMap<String,Float>();
    private String a[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Setup toolbar and title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_charts));


        readDb();
        printChart();
    }

    private void readDb()
    {
        // Get the SQL Statement
        SQLiteOpenHelper helper = new DBHelper(this);
        try {
            db = helper.getReadableDatabase();
            cursor= db.rawQuery("select * from business_licenses", null);
            int count = cursor.getCount();
            a = new String[count];
            if (cursor.moveToFirst()) {
                int ndx = 0;
                do {
                    a[ndx] = cursor.getString(8);
                    Float freq = graphCount.get(a[ndx]);
                    graphCount.put(a[ndx], (freq == null) ? 1 : freq +1);
                    ndx++;
                } while (cursor.moveToNext());

                //Using toast for debugging purposes
                Toast t = Toast.makeText(this, "Cursor count " + ndx , Toast.LENGTH_LONG);
                t.show();
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
        barWidth = 0.4f;
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

        // Really ugly sort code
        Set<Map.Entry<String, Float>> set = graphCount.entrySet();
        List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(set);
        Collections.sort( list, new Comparator<Map.Entry<String, Float>>()
        {
            public int compare( Map.Entry<String, Float> o1, Map.Entry<String, Float> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

        int i = 1;
        int topTen = 0;

        for (Map.Entry<String, Float> entry:list) {
            if (entry.getValue() > 1 && topTen < 10) {
                dummyEntries.add(new BarEntry(i++, entry.getValue())); //Entries must be floats
                barChart.notifyDataSetChanged();
                barChart.invalidate();
                System.out.println(i + " " + entry.getValue());
                topTen++;
            }
        }

        ArrayList<BarEntry> dummyEntries2 = new ArrayList<>();
        dummyEntries2.add(new BarEntry(1,26f));
        dummyEntries2.add(new BarEntry(2,58f));
        dummyEntries2.add(new BarEntry(3,13f));
        dummyEntries2.add(new BarEntry(4,48f));
        dummyEntries2.add(new BarEntry(5,48f));

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
        // Sets X-Axis settings
        barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * i);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.groupBars(0, groupSpace, barSpace); // Helps splits the bars
        barChart.invalidate(); // don't know what this does


        barChart.getData().setHighlightEnabled(false); // Prevents tapping on the bars
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setFitBars(true);
        barChart.setDoubleTapToZoomEnabled(true);

        // Y-Axis settings
        barChart.getAxisLeft().setAxisMinimum(0); // Starts at 0
        barChart.getAxisRight().setAxisMinimum(0); // Starts at 0

    }
}
