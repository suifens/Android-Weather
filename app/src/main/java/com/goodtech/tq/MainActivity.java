package com.goodtech.tq;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.location.Location;
import com.goodtech.tq.utils.DeviceUtils;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private TextView mAddressTv;
    //  当前城市
    private Location mCurLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        //  点击地址，跳转到城市列表
        findViewById(R.id.layout_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CityListActivity.redirectTo(MainActivity.this);
            }
        });

        //  跳转到设置页面
        findViewById(R.id.img_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
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
