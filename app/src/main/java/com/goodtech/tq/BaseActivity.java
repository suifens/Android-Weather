package com.goodtech.tq;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.TipHelper;

/**
 * com.goodtech.tq
 */
public class BaseActivity extends AppCompatActivity {

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    private static final String TAG = "BaseActivity";

    protected View mStationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

     /**
     * 配置station bar
     */
    public void configStationBar(View stationBar) {
        mStationBar = stationBar;

        ConstraintLayout.LayoutParams bars = new ConstraintLayout.LayoutParams(stationBar.getLayoutParams());
        bars.height = bars.height + DeviceUtils.getStatusBarHeight();
        stationBar.setLayoutParams(bars);
        Log.d(TAG, "onCreate: bar height = " + bars.height);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (resultCode != Activity.RESULT_OK) {
                TipHelper.showProgressDialog(BaseActivity.this);
                WeatherApp.getInstance().requestLocation();
            }
        }
    }

    protected static final int PERMISSION_REQUEST_COARSE_LOCATION = 10100;
    protected boolean checkLocationPermission() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    protected void requestLocationPermissions() {
        this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS},
                PERMISSION_REQUEST_COARSE_LOCATION);
    }

}
