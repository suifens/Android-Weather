package com.goodtech.tq;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.utils.DeviceUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private View mStationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStationBar = findViewById(R.id.private_station_bar);

        ConstraintLayout.LayoutParams bars = new ConstraintLayout.LayoutParams(mStationBar.getLayoutParams());
        bars.height = bars.height + DeviceUtils.getStatusBarHeight();
        mStationBar.setLayoutParams(bars);

        Log.d(TAG, "onCreate: bar height = " + bars.height);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        WeatherApp.getInstance().locationHelper.stop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        WeatherApp.getInstance().locationHelper.start();
    }

    }
