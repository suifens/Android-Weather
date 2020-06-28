package com.goodtech.tq;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.cityList.CityListActivity;
import com.goodtech.tq.fragement.WeatherFragment;
import com.goodtech.tq.fragement.adapter.ViewPagerAdapter;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    protected TextView mAddressTv;
    protected ImageView mLocationTip;
    //  当前城市
    private CityMode mCurLocation;

    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private ImageView mBgImgView;
    private RadioGroup mRgIndicator;
    private List<CityMode> mLocationList;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private ArrayList<CityMode> mCityModes = new ArrayList<>();
    private int mCurrIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAddressTv = findViewById(R.id.tv_address);
        mViewPager = findViewById(R.id.viewpager_main);
        mBgImgView = findViewById(R.id.img_background);
        mLocationTip = findViewById(R.id.img_location);
        mRgIndicator = findViewById(R.id.indicator_city);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        //  静止底部图片滑动
        findViewById(R.id.scroll_background).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

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

        mLocationList = LocationSpHelper.getCityListAndLocation();
        mCurLocation = LocationSpHelper.getLocation();
        setAddress(mCurLocation);

        configViewPager();
        mCurrIndex = 0;
    }

    private void setAddress(CityMode cityMode) {
        if (cityMode != null && cityMode.cid != 0) {
            mAddressTv.setText(cityMode.city);
        } else {
            mAddressTv.setText("定位失败");
        }
    }

    private void configViewPager() {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setStateBar(findViewById(R.id.station_bg_view));
        mFragmentList.add(fragment);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
    }

    private void configRgIndicator(int pagerSize) {
        if (mRgIndicator == null) {
            return;
        }
        mRgIndicator.removeAllViews();

        int width = DeviceUtils.dip2px(getBaseContext(), 5);
        int height = DeviceUtils.dip2px(getBaseContext(), 5);
        int margin = DeviceUtils.dip2px(getBaseContext(), 6);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(width, height);
        for (int i = 0; i < pagerSize; i++) {
            RadioButton tempButton = new RadioButton(MainActivity.this);
            tempButton.setEnabled(false);
            tempButton.setChecked(false);
            tempButton.setBackgroundResource(R.drawable.sl_indicator_white);   // 设置RadioButton的背景图片
            tempButton.setButtonDrawable(null);
            if (i > 0) {
                layoutParams.leftMargin = margin;
            }
            mRgIndicator.addView(tempButton, layoutParams);
        }
        setIndicator(mCurrIndex);
    }

    protected void setIndicator(int position) {
        if (position >= 0 && position < mRgIndicator.getChildCount()) {
            mRgIndicator.check(mRgIndicator.getChildAt(position).getId());

            CityMode cityMode = mCityModes.get(position);
            if (cityMode != null) {
                setAddress(cityMode);
                WeatherModel weatherModel = WeatherSpHelper.getWeatherModel(cityMode.cid);
                changeBg(weatherModel);
            }
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        WeatherApp.getInstance().stopLocation();
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mViewPager.getCurrentItem() == 0) {
            setAddress(LocationSpHelper.getLocation());
        }

        if (TimeUtils.needLocation()) {
            WeatherApp.getInstance().startLocation();
        }

        mCityModes = LocationSpHelper.getCityListAndLocation();

        reloadFragment();
        reloadWeathers();
        configRgIndicator(mCityModes.size());
    }

    private void reloadFragment() {
        if (mFragmentList.size() != mCityModes.size()) {

            while (mFragmentList.size() > mCityModes.size()) {
                mFragmentList.remove(mFragmentList.size() - 1);
            }
            while (mFragmentList.size() < mCityModes.size()) {
                WeatherFragment fragment = new WeatherFragment();
                fragment.setStateBar(findViewById(R.id.station_bg_view));
                mFragmentList.add(fragment);
            }
            mAdapter.replaceAll(mFragmentList);
        }
    }

    private void reloadWeathers() {
        for (int i = 0; i < mCityModes.size(); i++) {
            CityMode cityMode = mCityModes.get(i);
            if (cityMode.cid != 0) {
                WeatherModel model = WeatherSpHelper.getWeatherModel(cityMode.cid);
                if (mFragmentList.size() > i) {
                    WeatherFragment fragment = (WeatherFragment) mFragmentList.get(i);
                    fragment.changeWeather(model, cityMode.city);
                }
            }
        }
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

    /**
     * 更改背景
     */
    private void changeBg(final WeatherModel model) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (model != null && model.dailies != null) {
                    Daily daily = model.dailies.get(0);
                    boolean night = false;
                    if (daily != null) {
                        long tSunrise = TimeUtils.switchTime(daily.sunRise);
                        long tSunset = TimeUtils.switchTime(daily.sunSet);
                        long current = System.currentTimeMillis();

                        night = current < tSunrise || current > tSunset;
                    }
                    //  天气图标
                    int icon_cd = -1;
                    if (model.hourlies != null && model.hourlies.size() > 0) {
                        Hourly hourly = model.hourlies.get(0);
                        icon_cd = hourly.icon_cd;
                    }
                    mBgImgView.setImageResource(ImageUtils.bgImageRes(icon_cd, night));
                }
            }
        });
    }

    protected ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (mCurrIndex != position) {
                mCurrIndex = position;
                setIndicator(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };
}
