package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * PopulationDensity's MapLayerInfoFragment.
 */
public class PopulationDensityFragment extends MapLayerInfoFragment {

    private AgeDemographics    mAgeDemographics;

    private HorizontalBarChart mAgeDemographicsChart;
    private PieChart           mAgeGroupsChart;

    public PopulationDensityFragment() {
        mAgeDemographics = new AgeDemographics();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_population_dentisy_map_layer_info, container, false);

        mAgeDemographicsChart = (HorizontalBarChart) v.findViewById(R.id.chart_age_demographics);
        mAgeGroupsChart = (PieChart) v.findViewById(R.id.chart_age_groups);

        setupAgeDemographicsChart();
        setupAgeGroupsChart();

        loadAgeDemographicsFromDB();

        return v;
    }

    private void loadAgeDemographicsFromDB() {
        String sqlQuery = "SELECT YEAR, MINAGE, MALE_POPULATION, FEMALE_POPULATION " +
                "FROM '" + DataSetType.AGE_DEMOGRAPHICS.getDataSet().getTableName() + "' " +
                "WHERE YEAR = 2016 " +
                "ORDER BY MINAGE DESC";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            @Override
            public void onDBCursorReady(Cursor cursor) {
                if (cursor.moveToFirst()) {
                    do {
                        mAgeDemographics.addPopulation(
                                cursor.getInt(0),
                                cursor.getInt(1),
                                cursor.getInt(2),
                                cursor.getInt(3)
                        );
                    } while (cursor.moveToNext());
                }
            }
            @Override
            public void onDBReadComplete() {
                loadAgeDemographicsChart();
                loadAgeGroupsChart();
            }
        }, sqlQuery).execute();
    }

    private void setupAgeDemographicsChart() {
        mAgeDemographicsChart.setDrawGridBackground(false);
        mAgeDemographicsChart.getDescription().setEnabled(false);
        mAgeDemographicsChart.setScaleEnabled(false);
        mAgeDemographicsChart.setPinchZoom(false);
        mAgeDemographicsChart.setDrawBarShadow(false);
        mAgeDemographicsChart.setDrawValueAboveBar(true);
        mAgeDemographicsChart.setHighlightFullBarEnabled(false);
        mAgeDemographicsChart.getAxisLeft().setEnabled(false);
        mAgeDemographicsChart.getAxisRight().setDrawGridLines(false);
        mAgeDemographicsChart.getAxisRight().setDrawZeroLine(true);
        mAgeDemographicsChart.getAxisRight().setLabelCount(7, false);
        mAgeDemographicsChart.getAxisRight().setValueFormatter(new CustomFormatter());
        mAgeDemographicsChart.getAxisRight().setTextSize(9f);
        XAxis xAxis = mAgeDemographicsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextSize(9f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(105f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setLabelCount(23);
        xAxis.setGranularity(5f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private DecimalFormat format = new DecimalFormat("###");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return format.format(value) + "-" + format.format(value + 5);
            }
        });
        Legend l = mAgeDemographicsChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);
    }

    private void loadAgeDemographicsChart() {
        // IMPORTANT: When using negative values in stacked bars, always make sure the negative values are in the array first
        ArrayList<BarEntry> yValues = new ArrayList<BarEntry>();
        for (Map.Entry<Integer, Pair<Integer, Integer>> entry : mAgeDemographics.getDemographics(2016).entrySet()) {
            Integer ageGroup = entry.getKey();
            Pair<Integer, Integer> population = entry.getValue();
            yValues.add(new BarEntry(ageGroup + 2.5f, new float[]{ population.first * -1, population.second }));
        }
        BarDataSet set = new BarDataSet(yValues, "[AGE DISTRIBUTION]");
        set.setDrawValues(true);
        set.setDrawIcons(false);
        set.setValueFormatter(new CustomFormatter());
        set.setValueTextSize(7f);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setColors(new int[] {Color.rgb(67,67,72), Color.rgb(124,181,236)});
        set.setStackLabels(new String[]{
                "Men", "Women"
        });
        BarData data = new BarData(set);
        data.setBarWidth(4.5f);
        mAgeDemographicsChart.setData(data);
        mAgeDemographicsChart.invalidate();
        mAgeDemographicsChart.animateY(1500);
    }

    private void setupAgeGroupsChart() {
        mAgeGroupsChart.setUsePercentValues(true);
        mAgeGroupsChart.getDescription().setEnabled(false);
        mAgeGroupsChart.setExtraOffsets(5, 10, 5, 5);
        mAgeGroupsChart.setDragDecelerationFrictionCoef(0.95f);
//        mAgeGroupsChart.setCenterTextTypeface(mTfLight);
        mAgeGroupsChart.setCenterText("AGE GROUPS");
        mAgeGroupsChart.setDrawHoleEnabled(true);
        mAgeGroupsChart.setHoleColor(Color.WHITE);
        mAgeGroupsChart.setTransparentCircleColor(Color.WHITE);
        mAgeGroupsChart.setTransparentCircleAlpha(110);
        mAgeGroupsChart.setHoleRadius(45f);
        mAgeGroupsChart.setTransparentCircleRadius(61f);
        mAgeGroupsChart.setDrawCenterText(true);
        mAgeGroupsChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mAgeGroupsChart.setRotationEnabled(false);
        mAgeGroupsChart.setHighlightPerTapEnabled(false);
    }

    private void loadAgeGroupsChart() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (Map.Entry<Integer, Long> entry : mAgeDemographics.getAgeGroups(2016).entrySet()) {
            String label;
            if (entry.getKey() == 0) {
                label = "Children (0-15)"; // Children
            } else if (entry.getKey() == 1) {
                label = "Youth (15-25)"; // Youth
            } else if (entry.getKey() == 2) {
                label = "Adults (25-65)"; // Adults
            } else {
                label = "Seniors (65+)"; // Seniors
            }
            entries.add(new PieEntry(entry.getValue(), label));
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        mAgeGroupsChart.setData(data);
        // undo all highlights
        mAgeGroupsChart.highlightValues(null);
        mAgeGroupsChart.invalidate();
        this.mAgeGroupsChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);
        Legend l = this.mAgeGroupsChart.getLegend();
        l.setEnabled(false);
//        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
//        l.setOrientation(Legend.LegendOrientation.VERTICAL);
//        l.setDrawInside(false);
//        l.setXEntrySpace(7f);
//        l.setYEntrySpace(0f);
//        l.setYOffset(0f);
        // entry label styling
        this.mAgeGroupsChart.setEntryLabelColor(Color.BLACK);
        this.mAgeGroupsChart.setEntryLabelTextSize(12f);
    }

    private class CustomFormatter implements IValueFormatter, IAxisValueFormatter
    {
        private DecimalFormat mFormat;
        public CustomFormatter() {
            mFormat = new DecimalFormat("###");
        }
        // data
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(Math.abs(value)) + "";
        }
        // YAxis
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mFormat.format(Math.abs(value)) + "";
        }
    }
}
