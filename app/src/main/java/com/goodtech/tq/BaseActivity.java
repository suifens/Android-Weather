package com.goodtech.tq;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.goodtech.tq.utils.DeviceUtils;

/**
 * com.goodtech.tq
 */
public class BaseActivity extends AppCompatActivity {

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

}
