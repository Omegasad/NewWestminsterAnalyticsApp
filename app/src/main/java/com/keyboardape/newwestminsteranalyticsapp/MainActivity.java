package com.keyboardape.newwestminsteranalyticsapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickChart(View v) {
        Intent i = new Intent(this, ChartActivity.class);
        startActivity(i);
    }

    public void onClickMaps(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }
}
