package com.goodtech.tq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodtech.tq.app.WeatherApp;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (!isLocationEnabled()) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            } else {
                TipHelper.showProgressDialog(this, false);
            }
            WeatherApp.getInstance().requestLocation();
        }
    }

    protected static final int PERMISSION_REQUEST_COARSE_LOCATION = 10100;
    protected boolean checkLocationPermission() {
        if (checkPermission()) {
            return false;
        }
        return isLocationEnabled();
    }

    private boolean checkPermission() {
        return this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 判断定位服务是否开启
     * @return true 表示开启
     */
    public boolean isLocationEnabled() {
        int locationMode = 0;
//        String locationProviders;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
//        } else {
//            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//            return !TextUtils.isEmpty(locationProviders);
//        }
    }

    protected void requestLocationPermissions() {
        if (checkPermission()) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS},
                    PERMISSION_REQUEST_COARSE_LOCATION);
        } else {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            WeatherApp.getInstance().requestLocation();
        }
    }

    protected void removeTicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mHandler.hasCallbacks(mCheckTicker)) {
                mHandler.removeCallbacks(mCheckTicker);
            }
        } else {
            mHandler.removeCallbacks(mCheckTicker);
        }
    }

    protected int scanCount = 0;
    protected final Runnable mCheckTicker = new Runnable() {
        public void run() {
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            if (scanCount++ > 5) {
                removeTicker();
                TipHelper.dismissProgressDialog();
            } else {
                mHandler.postAtTime(mCheckTicker, next);
            }
        }
    };

}
