package com.goodtech.tq;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private View mStationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStationBar = findViewById(R.id.private_station_bar);

        Toast.makeText(this, "starus bar height = " + getStatusBarHeight(), Toast.LENGTH_SHORT).show();
        Log.d("", "onCreate: status bar height = " + getStatusBarHeight());

        ConstraintLayout.LayoutParams bars = new ConstraintLayout.LayoutParams(mStationBar.getLayoutParams());
        bars.height = bars.height + getStatusBarHeight();
        mStationBar.setLayoutParams(bars);

        Log.d(TAG, "onCreate: bar height = " + bars.height);
    }

    /**
     * 利用反射获取状态栏高度
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
