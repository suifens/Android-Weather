package com.goodtech.tq;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.goodtech.tq.cityList.CityListActivity;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.fragment.WeatherFragment;
import com.goodtech.tq.fragment.adapter.ViewPagerAdapter;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.IntentReceiver;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.MessageAlert;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    
    private final BroadcastReceiver receiver = new IntentReceiver();

    protected TextView mAddressTv;
    protected ImageView mLocationTip;

    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private ImageView mBgImgView;
    private RadioGroup mRgIndicator;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private ArrayList<CityMode> mCityModes = new ArrayList<>();
    private int mCurrIndex;
    private long mBackTime;
    private boolean mLoadLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAddressTv = findViewById(R.id.tv_address);
        mViewPager = findViewById(R.id.viewpager_main);
        mBgImgView = findViewById(R.id.img_background);
        mLocationTip = findViewById(R.id.img_location);
        mRgIndicator = findViewById(R.id.indicator_city);

        EventBus.getDefault().register(this);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        //  静止底部图片滑动
        findViewById(R.id.scroll_background).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
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

        //  当前城市
        CityMode mCurLocation = LocationSpHelper.getLocation();
        setAddress(mCurLocation);

        configViewPager();
        mCurrIndex = 0;

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(receiver, filter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void backAction(){
        long current = System.currentTimeMillis();
        if (current - mBackTime < 2 * 1000) {
            finish();
        } else {
            mBackTime = current;
            Toast.makeText(MainActivity.this.getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        WeatherHttpHelper.getInstance().fetchCitiesWeather();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        LocationHelper.getInstance().stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mViewPager.getCurrentItem() == 0) {
            setAddress(LocationSpHelper.getLocation());
        }

        if (TimeUtils.needLocation()) {
            LocationHelper.getInstance().start(this);
        }

        reloadView();

        if (mLoadLast) {
            mViewPager.setCurrentItem(mFragmentList.size() - 1);
            mLoadLast = false;

        } else if (mCityModes.size() > mCurrIndex) {
            mViewPager.setCurrentItem(mCurrIndex);
        }
    }

    private void reloadView() {
        mCityModes = LocationSpHelper.getCityListAndLocation();

        mCurrIndex = mViewPager.getCurrentItem();
        if (mCityModes.size() > 0 && mCurrIndex >= mCityModes.size()) {
            mCurrIndex = mCityModes.size() - 1;
        }

        reloadFragment();
        reloadWeathers();
        configRgIndicator(mCityModes.size());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        TipHelper.dismissProgressDialog();
        removeTicker();

        if (event.isSuccessLocation()) {

            reloadView();
        }
        if (event.getFetchCId() != 0) {
            for (int i = 0; i < mCityModes.size(); i++) {
                CityMode cityMode = mCityModes.get(i);
                if (cityMode.cid == event.getFetchCId()) {
                    reloadWeather(i);
                    break;
                }
            }
        }
        if (event.showIndex() >= 0) {
            mCurrIndex = event.showIndex();
        }
        if (event.isAddCity()) {
            mLoadLast = true;
        }
    }

    private void reloadFragment() {
        if (mFragmentList.size() != mCityModes.size()) {

//            ArrayList<Fragment> tempList = new ArrayList<>();
//            for (int i = 0; i < mCityModes.size(); i++) {
//                if (mFragmentList.size() > i) {
//                    tempList.add(mFragmentList.get(i));
//                } else {
//                    tempList.add(newFragment());
//                }
//            }
//            mAdapter.replaceAll(tempList);
//            mFragmentList = tempList;

            while (mFragmentList.size() > mCityModes.size()) {
                mFragmentList.remove(mFragmentList.size() - 1);
            }
            while (mFragmentList.size() < mCityModes.size()) {
                addFragment();
            }
            mAdapter.replaceAll(mFragmentList);
        }
    }

    private WeatherFragment newFragment() {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setStateBar(findViewById(R.id.station_bg_view));
        return fragment;
    }

    private void addFragment() {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setStateBar(findViewById(R.id.station_bg_view));
        mFragmentList.add(fragment);
    }

    private void reloadWeathers() {
        TipHelper.showProgressDialog(this, R.string.loading_data, false);
        for (int i = 0; i < mCityModes.size(); i++) {
            reloadWeather(i);
        }
    }

    private void reloadWeather(final int index) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CityMode cityMode = mCityModes.get(index);
                if (cityMode.cid != 0) {
                    WeatherModel model = WeatherSpHelper.getWeatherModel(cityMode.cid);
                    if (mFragmentList.size() > index) {
                        WeatherFragment fragment = (WeatherFragment) mFragmentList.get(index);
                        fragment.changeWeather(model, cityMode);
                    }
                    if (index == mCurrIndex) {
                        changeBg(model);
                    }
                }
            }
        });
    }

    /**
     * 更改背景
     */
    private void changeBg(final WeatherModel model) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (model != null && model.dailies != null) {
                    TipHelper.dismissProgressDialog();

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
                    if (model.observation != null) {
                        icon_cd = model.observation.wxIcon;
                        if (model.hourlies.size() > 0 && model.hourlies.get(0) != null) {
                            Hourly hourly = model.hourlies.get(0);
                            long time = hourly.fcst_valid;
                            if (System.currentTimeMillis() > time * 1000) {
                                icon_cd = hourly.icon_cd;
                            }
                        }
                    }

                    mBgImgView.setImageResource(ImageUtils.bgImageRes(icon_cd, night));
                } else {
                    mBgImgView.setImageResource(R.drawable.bg_normal);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TipHelper.dismissProgressDialog();
                        }
                    }, 2000);
                }
            }
        });
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

                if (mFragmentList.size() > position) {
                    WeatherFragment fragment = (WeatherFragment) mFragmentList.get(position);
                    fragment.changeWeather(weatherModel, cityMode);
                }
            }

            CityMode location = LocationSpHelper.getLocation();
            if (mCurrIndex == 0 && location.cid == 0) {
                if (!this.isFinishing()) {
                    MessageAlert alert = new MessageAlert(this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestLocationPermissions();
                            mHandler.post(mCheckTicker);
                        }
                    });
                    alert.show();
                }
            }
        }
    }

    private void setAddress(CityMode cityMode) {
        if (cityMode != null && mAddressTv != null) {
            mLocationTip.setVisibility(cityMode.location ? View.VISIBLE : View.GONE);

            if (cityMode.location && cityMode.cid == 0) {
                mAddressTv.setText("定位失败");
            } else {
                if (!TextUtils.isEmpty(cityMode.city)) {
                    mAddressTv.setText(cityMode.city);
                } else {
                    mAddressTv.setText("");
                }
            }
        }

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
