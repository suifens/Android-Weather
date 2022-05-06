package com.goodtech.tq;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.goodtech.tq.alarm.AlarmManagerUtil;
import com.goodtech.tq.alarm.JAlarmReceiver;
import com.goodtech.tq.cityList.CityListActivity;
import com.goodtech.tq.db.SignDbHelper;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.fragment.WeatherFragment2;
import com.goodtech.tq.fragment.adapter.ViewPagerAdapter;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.AdUtil;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.DownloadConfirmHelper;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.IntentReceiver;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.TipHelper;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.comm.listeners.NegativeFeedbackListener;
import com.qq.e.comm.util.AdError;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements UnifiedInterstitialADListener, UnifiedInterstitialMediaListener {

    private static final String TAG = "MainActivity";
    private final BroadcastReceiver receiver = new IntentReceiver();

    protected TextView mAddressTv;
    protected ImageView mLocationTip;

    private ViewPager2 mViewPager;
    private ViewPagerAdapter mAdapter;
    private ImageView mBgImgView;
    private RadioGroup mRgIndicator;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private ArrayList<CityMode> mCityModes = new ArrayList<>();
    private int mCurrIndex;
    private long mBackTime;
    private boolean mLoadLast;
    private boolean mSigned;    //  是否已签到

    @Override
    @SuppressLint("ClickableViewAccessibility")
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
//        findViewById(R.id.scroll_background).setOnTouchListener((v, event) -> true);

        //  点击地址，跳转到城市列表
        findViewById(R.id.layout_address).setOnClickListener(v -> CityListActivity.redirectTo(MainActivity.this));

        //  跳转到设置页面
        findViewById(R.id.img_setting).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        configViewPager();
        mCurrIndex = 0;

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(receiver, filter);

//        AlarmManagerUtil.setAlarm(getApplicationContext(), 21);

        String curDay = TimeUtils.timeToDay(System.currentTimeMillis());
        if (!SpUtils.getInstance().getString("alarmDay", "").equals(curDay)) {
            JAlarmReceiver.fetchAlarm(this);
        }

        mHandler.postDelayed(() -> this.startService(new Intent(this, WidgetService.class)), 1000);
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
        WeatherHttpHelper.getInstance().getBaseUrl(() -> WeatherHttpHelper.getInstance().fetchCitiesWeather());
        Log.e(TAG, "onResume: ");
        //  加载广告
        if (!SpUtils.getInstance().getBoolean("hadShowInterstitialAD", false)) {
            mAdLoadSuccess = false;
            mHandler.postDelayed(this::loadAd, 4000);
        }
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

    boolean isNeedReload = true;
    @Override
    protected void onStart() {
        super.onStart();

        if (TimeUtils.needLocation()) {
            LocationHelper.getInstance().start(this);
        }

        ArrayList<CityMode> cityModes = LocationSpHelper.getCityListAndLocation();
        if (cityModes.size() != mCityModes.size() || isNeedReload) {
            mCityModes = new ArrayList<>(cityModes);
            isNeedReload = true;
        }

        reloadView();
        mViewPager.setCurrentItem(mCurrIndex);
        setIndicator(mCurrIndex);
    }

    private void reloadView() {

        if (!isNeedReload) {
            return;
        }

        isNeedReload = false;
        configRgIndicator(mCityModes.size());
        reloadFragment();

        if (mLoadLast) {
            mCurrIndex = mFragmentList.size() - 1;
            mLoadLast = false;
        } else {
            mCurrIndex = Math.min(mCurrIndex, mFragmentList.size() - 1);
        }
        //  更新天气
        reloadWeathers();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        Log.e(TAG, "onMessageEvent: ");
        TipHelper.dismissProgressDialog();
        removeTicker();

        if (event.isSuccessLocation()) {

            CityMode cityMode = LocationSpHelper.getLocation();
            mCityModes.set(0, cityMode);
            reloadWeather(0);
        }
        if (event.getFetchCId() != 0) {
            for (int i = 0; i < mCityModes.size(); i++) {
                CityMode cityMode = mCityModes.get(i);
                if (cityMode.getCid() == event.getFetchCId()) {
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
            isNeedReload = true;
        }
        if (event.isNeedReload()) {
            isNeedReload = true;
        }
    }

    private void reloadFragment() {
        if (mFragmentList.size() != mCityModes.size()) {

            while (mFragmentList.size() > mCityModes.size()) {
                mFragmentList.remove(mFragmentList.size() - 1);
            }
            while (mFragmentList.size() < mCityModes.size()) {
                addFragment();
            }
            mAdapter.replaceAll(mFragmentList);
        }
    }

    private void addFragment() {
        WeatherFragment2 fragment = new WeatherFragment2();
        fragment.setStateBar(findViewById(R.id.station_bg_view));
        mFragmentList.add(fragment);
    }

    private void reloadWeathers() {
        SignDbHelper signDbHelper = new SignDbHelper(this);
        mSigned = signDbHelper.hadSigning(TimeUtils.longToString(System.currentTimeMillis(), "yyyy-MM-dd"));

        TipHelper.showProgressDialog(this, R.string.loading_data, false);
        for (int i = 0; i < mCityModes.size(); i++) {
            reloadWeather(i);
        }
    }

    private void reloadWeather(final int index) {

        mHandler.post(() -> {
            CityMode cityMode = mCityModes.get(index);
            if (cityMode.getCid() != 0) {
                WeatherModel model = WeatherSpHelper.getWeatherModel(cityMode.getCid());
                if (mFragmentList.size() > index) {
                    WeatherFragment2 fragment = (WeatherFragment2) mFragmentList.get(index);
                    fragment.changeWeather(model, cityMode, mSigned);
                }
                if (index == mCurrIndex) {
                    changeBg(model);
                }
            }
        });
    }

    /**
     * 更改背景
     */
    private void changeBg(final WeatherModel model) {

        mHandler.post(() -> {
            if (model != null && model.dailies != null) {
                TipHelper.dismissProgressDialog(1000);

                Daily daily = model.dailies.get(0);
                boolean night = false;
                if (daily != null) {
                    long tSunrise = TimeUtils.switchTime(daily.sunRise);
                    long tSunset = TimeUtils.switchTime(daily.sunSet);
                    long current = System.currentTimeMillis();

                    night = current < tSunrise || current > tSunset;
                }
                //  天气图标
                mBgImgView.setImageResource(ImageUtils.bgImageRes(model.getIconCd(), night));
            } else {
                mBgImgView.setImageResource(R.drawable.bg_normal);
                mHandler.postDelayed(TipHelper::dismissProgressDialog, 2000);
            }
        });
    }

    private void configViewPager() {
        WeatherFragment2 fragment = new WeatherFragment2();
        fragment.setStateBar(findViewById(R.id.station_bg_view));
        mFragmentList.add(fragment);
        mAdapter = new ViewPagerAdapter(this, mFragmentList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (mCurrIndex != position) {
                    mCurrIndex = position;
                    setIndicator(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
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
                WeatherModel weatherModel = WeatherSpHelper.getWeatherModel(cityMode.getCid());
                changeBg(weatherModel);

                if (mFragmentList.size() > position) {
                    WeatherFragment2 fragment = (WeatherFragment2) mFragmentList.get(position);
                    fragment.changeWeather(weatherModel, cityMode, mSigned);
                    fragment.reloadNativeAD();
                }
            }
        }
    }

    private void setAddress(CityMode cityMode) {
        if (cityMode != null && mAddressTv != null) {
            mLocationTip.setVisibility(cityMode.getLocation() ? View.VISIBLE : View.GONE);

            if (!TextUtils.isEmpty(cityMode.getCity())) {
                if (cityMode.getLocation()) {
                    //  定位
                    mAddressTv.setText(cityMode.getMergerName());
                } else {
                    mAddressTv.setText(cityMode.getCity());
                }
            } else {
                mAddressTv.setText("");
            }
        }

    }


    /**
     * 以下为插屏广告
     */
    private UnifiedInterstitialAD iad;
    private boolean isRenderFail;
    private boolean mAdLoadSuccess;
    
    private void loadAd() {
        if (!mAdLoadSuccess || !(iad != null && iad.isValid())) {
            iad = getIAD();
            iad.loadAD();
        }
    }

    private void showAd() {
        Log.e(TAG, "showAd: ");
        mHandler.postDelayed(() -> {
            if (mAdLoadSuccess && iad != null && iad.isValid()) {
                Log.e(TAG, "showAd: true");
                iad.show();
                SpUtils.getInstance().putBoolean("hadShowInterstitialAD", true);
            }
        }, 1000);
    }
    
    private UnifiedInterstitialAD getIAD() {
        if (this.iad != null) {
            iad.close();
            iad.destroy();
        }
        isRenderFail = false;
        String posId = Constants.INT_POS_ID;
//        Log.d(TAG, "getIAD: BiddingToken " + s2sBiddingToken);
        if (iad == null) {
//            if (!TextUtils.isEmpty(s2sBiddingToken)) {
//                iad = new UnifiedInterstitialAD(this, posId, this, null, s2sBiddingToken);
//            } else {
                iad = new UnifiedInterstitialAD(this, posId, this);
//            }
            iad.setNegativeFeedbackListener(new NegativeFeedbackListener() {
                @Override
                public void onComplainSuccess() {
                    Log.i(TAG, "onComplainSuccess");
                }
            });
            iad.setMediaListener(this);
            iad.setLoadAdParams(AdUtil.getLoadAdParams("interstitial"));
//            currentPosId = posId;
        }
        return iad;
    }

    private void close() {
        if (iad != null) {
            iad.close();
        } else {
//            Toast.makeText(this, "广告尚未加载 ！ ", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onADReceive() {
//        mAdLoadSuccess = true;
//        showAd();
//        Toast.makeText(this, "广告加载成功 ！ ", Toast.LENGTH_LONG).show();
        // onADReceive之后才可调用getECPM()
        Log.d(TAG, "onADReceive eCPMLevel = " + iad.getECPMLevel()+ ", ECPM: " + iad.getECPM()
                + ", videoduration=" + iad.getVideoDuration()
                + ", testExtraInfo:" + iad.getExtraInfo().get("mp")
                + ", request_id:" + iad.getExtraInfo().get("request_id"));
        if (DownloadConfirmHelper.USE_CUSTOM_DIALOG) {
            iad.setDownloadConfirmListener(DownloadConfirmHelper.DOWNLOAD_CONFIRM_LISTENER);
        }
        reportBiddingResult(iad);
    }

    /**
     * 上报给优量汇服务端在开发者客户端竞价中优量汇的竞价结果，以便于优量汇服务端调整策略提供给开发者更合理的报价
     *
     * 优量汇竞价失败调用 sendLossNotification，并填入优量汇竞败原因（必填）、竞胜ADN ID（选填）、竞胜ADN报价（选填）
     * 优量汇竞价胜出调用 sendWinNotification，并填入开发者期望扣费价格（单位分）
     * 请开发者如实上报相关参数，以保证优量汇服务端能根据相关参数调整策略，使开发者收益最大化
     */
    private void reportBiddingResult(UnifiedInterstitialAD interstitialAD) {
//        DemoBiddingC2SUtils.reportBiddingWinLoss(interstitialAD);
//        if (DemoUtil.isNeedSetBidECPM()) {
//            interstitialAD.setBidECPM(300);
//        }
    }

    @Override
    public void onVideoCached() {
        // 视频素材加载完成，在此时调用iad.show()或iad.showAsPopupWindow()视频广告不会有进度条。
        Log.i(TAG, "onVideoCached");
    }

    @Override
    public void onNoAD(AdError error) {
        String msg = String.format(Locale.getDefault(), "onNoAD, error code: %d, error msg: %s",
                error.getErrorCode(), error.getErrorMsg());
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onADOpened() {
        Log.i(TAG, "onADOpened");
    }

    @Override
    public void onADExposure() {
        Log.i(TAG, "onADExposure");
    }

    @Override
    public void onADClicked() {
        Log.i(TAG, "onADClicked");
        if (iad != null && iad.isValid()) {
            iad.close();
        }
    }

    @Override
    public void onADLeftApplication() {
        Log.i(TAG, "onADLeftApplication");
    }

    @Override
    public void onADClosed() {
        Log.i(TAG, "onADClosed");
    }

    @Override
    public void onRenderSuccess() {
        Log.i(TAG, "onRenderSuccess，建议在此回调后再调用展示方法");
        mAdLoadSuccess = true;
        showAd();
    }

    @Override
    public void onRenderFail() {
        Log.i(TAG, "onRenderFail");
        isRenderFail = true;
    }

    @Override
    public void onVideoInit() {
        Log.i(TAG, "onVideoInit");
    }

    @Override
    public void onVideoLoading() {
        Log.i(TAG, "onVideoLoading");
    }

    @Override
    public void onVideoReady(long videoDuration) {
        Log.i(TAG, "onVideoReady, duration = " + videoDuration);
    }

    @Override
    public void onVideoStart() {
        Log.i(TAG, "onVideoStart");
    }

    @Override
    public void onVideoPause() {
        Log.i(TAG, "onVideoPause");
    }

    @Override
    public void onVideoComplete() {
        Log.i(TAG, "onVideoComplete");
    }

    @Override
    public void onVideoError(AdError error) {
        Log.i(TAG, "onVideoError, code = " + error.getErrorCode() + ", msg = " + error.getErrorMsg());
    }

    @Override
    public void onVideoPageOpen() {
        Log.i(TAG, "onVideoPageOpen");
    }

    @Override
    public void onVideoPageClose() {
        Log.i(TAG, "onVideoPageClose");
    }
}
