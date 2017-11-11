// TODO: 9/29/2017 find out what barChart.invalidate(); does

package com.keyboardape.newwestminsteranalyticsapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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
        barWidth = 0.9f;

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

        // To store the data in somehow
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        // Really ugly sort code to sort the hashmap to values
        Set<Map.Entry<String, Float>> set = graphCount.entrySet();
        List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(set);
        Collections.sort( list, new Comparator<Map.Entry<String, Float>>()
            {
                public int compare( Map.Entry<String, Float> o1, Map.Entry<String, Float> o2 )
                {
                    return (o2.getValue()).compareTo( o1.getValue() );
                }
            }
        );

        //Help gets the top 10 most popular businesses
        int i = 1;
        int topTen = 0;
        for (Map.Entry<String, Float> entry:list) {
            if (entry.getValue() > 1 && topTen < 10) {
                barEntries.add(new BarEntry(i++, entry.getValue())); //Entries must be floats
                barChart.notifyDataSetChanged();
                barChart.invalidate();
                System.out.println(i + " " + entry.getValue());
                topTen++;
            }
        }

        // Is needed so the data can be outputted
        BarDataSet barDataSet = new BarDataSet(barEntries,"Most Popular Businesses");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(barDataSet);

        barChart.setData(data);

        // Settings, Needs to be declared at the very bottom
        data.setBarWidth(barWidth);
        barChart.getData().setHighlightEnabled(false); // Prevents tapping on the bars
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setFitBars(true); // makes the x-axis fit exactly all bars
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.invalidate(); // refreshes (redraws the chart)

        testLegend(barChart);
    }

    private void testLegend(BarChart barChart) {
        Legend l = barChart.getLegend();
        //Hides the legend for now
        l.setEnabled(false);
    }
}
