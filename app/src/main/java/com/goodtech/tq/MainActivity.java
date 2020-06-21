package com.goodtech.tq;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.cityList.CityListActivity;
import com.goodtech.tq.fragement.WeatherFragment;
import com.goodtech.tq.fragement.WeatherFragment2;
import com.goodtech.tq.fragement.adapter.ViewPagerAdapter;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.ApiCallback;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    protected TextView mAddressTv;
    //  当前城市
    private CityMode mCurLocation;

    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private ImageView mBgImgView;
    private RadioGroup mRgIndicator;
    private List<CityMode> mLocationList;
    private List<Fragment> mFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAddressTv = findViewById(R.id.tv_address);
        mViewPager = findViewById(R.id.viewpager_main);
        mBgImgView = findViewById(R.id.img_background);

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

        mLocationList = LocationSpHelper.getLocationList();
        mCurLocation = LocationSpHelper.getLocation();
        mAddressTv.setText(mCurLocation.city);

        configViewPager();
    }

    private void configViewPager() {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setStateBar(findViewById(R.id.station_bg_view));
        mFragmentList.add(fragment);
        WeatherFragment2 nextFragment = new WeatherFragment2();
        mFragmentList.add(nextFragment);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        WeatherApp.getInstance().locationHelper.stop();
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        WeatherHttpHelper httpHelper = new WeatherHttpHelper(WeatherApp.getInstance());
        httpHelper.getWeather(mCurLocation.lat, mCurLocation.lon, new ApiCallback() {
            @Override
            public void onResponse(boolean success, final WeatherModel weather, ErrorCode errCode) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        WeatherFragment fragment = (WeatherFragment) mFragmentList.get(0);
                        fragment.changeWeather(weather, mCurLocation.city);
                        changeBg(weather);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void changeBg(final WeatherModel model) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (model != null && model.dailies != null) {
                    Daily daily = model.dailies.get(0);
                    if (daily != null) {
                        long tSunrise = TimeUtils.switchTime(daily.sunRise);
                        long tSunset = TimeUtils.switchTime(daily.sunSet);
                        long current = System.currentTimeMillis();

                        boolean night = current < tSunrise || current > tSunset;
                        mBgImgView.setImageResource(ImageUtils.bgImageRes(model.icon_cd, night));
                    }
                }
            }
        });
    }
}
