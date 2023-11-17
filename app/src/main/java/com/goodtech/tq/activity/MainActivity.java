package com.goodtech.tq.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
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

import com.blankj.utilcode.util.AppUtils;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot;
import com.goodtech.tq.R;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.cityList.CityListActivity;
import com.goodtech.tq.db.SignDbHelper;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.fragment.WeatherFragment;
import com.goodtech.tq.fragment.adapter.ViewPagerAdapter;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.listener.CompletionListener;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.signing.SigningActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.IntentReceiver;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.popup.UpdatePopup;
import com.lxj.xpopup.XPopup;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private final BroadcastReceiver receiver = new IntentReceiver();

    protected TextView mAddressTv;
    protected ImageView mLocationTip;

    private ViewPager2 mViewPager;
    private ViewPagerAdapter mAdapter;
    private ImageView mBgImgView;
    private RadioGroup mRgIndicator;
    private View mSignTipV;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private ArrayList<CityMode> mCityModes = new ArrayList<>();
    private int mCurrIndex;
    private long mBackTime;
    private boolean mLoadLast;
    private boolean mSigned;    //  是否已签到
    private boolean isFirstLoad = true;
    private boolean isCurrent = false;  //  是否当前页面

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
        mSignTipV = findViewById(R.id.view_sign_tip);

        EventBus.getDefault().register(this);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        //  静止底部图片滑动
//        findViewById(R.id.scroll_background).setOnTouchListener((v, event) -> true);

        //  点击地址，跳转到城市列表
        findViewById(R.id.layout_address).setOnClickListener(v -> {
            if (!SpUtils.getInstance().isAgreePermission()) {
                showPermissionDialog(this, view ->
                        CityListActivity.redirectTo(MainActivity.this));
                return;
            }
            CityListActivity.redirectTo(MainActivity.this);
        });

        //  跳转到设置页面
        findViewById(R.id.img_setting).setOnClickListener(v -> {
            if (!SpUtils.getInstance().isAgreePermission()) {
                showPermissionDialog(this, view ->
                        startActivity(new Intent(MainActivity.this, SettingActivity.class)));
                return;
            }
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        });

        findViewById(R.id.img_sign).setOnClickListener(v -> {
            onSignClick();
        });

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

    protected void backAction() {
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
        // WeatherHttpHelper.getInstance().getBaseUrl(() -> WeatherHttpHelper.getInstance().fetchCitiesWeather());
        Log.e(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        this.isCurrent = false;
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

        this.isCurrent = true;
        // if (TimeUtils.needLocation()) {
        //     LocationHelper.getInstance().startWithDelay(this);
        // }
        if (isFirstLoad) {
            if (LocationSpHelper.getLocation() != null) {
                mHandler.postDelayed(() -> {
                    LocationHelper.getInstance().startWithDelay(this);
                }, 500);
            }
            /// 判断新版本
            fetchNewVersion(() -> {
                long interval = System.currentTimeMillis() - SpUtils.getInstance().getLong(Constants.PGE_INT_POS_ID, 0L);
                if (SpUtils.getInstance().isAgreePermission()) {
//                        && interval > 1000 * 60 * 30) {
                    mHandler.postDelayed(this::initAdLoader, 5000);
                }
            });

            isFirstLoad = false;
        } else {
            if (mIsLoadedAndShow) {
                showInterFullAd();
            }
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
            BaseApp.getInstance().startIntent(MainActivity.this);
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
        WeatherFragment fragment = new WeatherFragment();
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

        setSignedIn(mSigned);
    }

    private void reloadWeather(final int index) {

        mHandler.post(() -> {
            CityMode cityMode = mCityModes.get(index);
            if (cityMode.getCid() != 0) {
                WeatherModel model = WeatherSpHelper.getWeatherModel(cityMode.getCid());
                if (mFragmentList.size() > index) {
                    WeatherFragment fragment = (WeatherFragment) mFragmentList.get(index);
                    fragment.changeWeather(model, cityMode);
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
        WeatherFragment fragment = new WeatherFragment();
        fragment.setStateBar(findViewById(R.id.station_bg_view));
        mFragmentList.add(fragment);
        mAdapter = new ViewPagerAdapter(this, mFragmentList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(5);
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
                    WeatherFragment fragment = (WeatherFragment) mFragmentList.get(position);
                    fragment.changeWeather(weatherModel, cityMode);
                    // fragment.reloadNativeAD();
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
     * 设置签到状态
     *
     * @param signedIn 是否已签到
     */
    private void setSignedIn(boolean signedIn) {
        if (signedIn) {
            mSignTipV.setVisibility(View.GONE);
        } else {
            mSignTipV.setVisibility(View.VISIBLE);
        }
    }

    private void onSignClick() {
        if (!SpUtils.getInstance().isAgreePermission()) {
            showPermissionDialog(this, view -> onSignClick());
            return;
        }
        CityMode cityMode = mCityModes.get(mCurrIndex);
        WeatherModel weatherModel = null;
        if (cityMode.getCid() != 0) {
            weatherModel = WeatherSpHelper.getWeatherModel(cityMode.getCid());
        }
        if (weatherModel != null) {
            SigningActivity.redirectTo(MainActivity.this,
                    weatherModel.hourlies.get(0),
                    cityMode,
                    0);
        }
    }

    // <editor-folder desc="判断是否需要更新app">

    private void fetchNewVersion(CompletionListener listener) {
        JuHeHelper.getInstance().fetchNewVersion(new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                Log.e(TAG, "onResponse: " + jsonObject.toString());
                try {
                    if (success) {

                        if (!jsonObject.isNull("data")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            String version = data.getString("newVersion");
                            String[] versionTemp = version.split("\\.");
                            String curVersion = AppUtils.getAppVersionName();
                            String[] curTemp = curVersion.split("\\.");
                            boolean needUpdate = false;
                            for (int i = 0; i < versionTemp.length; i++) {
                                if (Integer.parseInt(versionTemp[i]) > Integer.parseInt(curTemp[i])) {
                                    needUpdate = true;
                                    break;
                                }
                            }
                            if (needUpdate
                                    && System.currentTimeMillis() - SpUtils.getInstance().getLong(version, 0L) > 2 * 24 * 60 * 60 * 1000) {
                                showUpdatePopup(version);
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (listener != null) listener.onCompletion();
            }
        });
    }

    private void showUpdatePopup(String version) {
        SpUtils.getInstance().putLong(version, System.currentTimeMillis());

        UpdatePopup popup = new UpdatePopup(this);
        popup.setupVersion(version, () -> {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                //要调起的应用不存在时的处理
                Toast.makeText(this, "未能跳转到应用商店", Toast.LENGTH_SHORT).show();
            }
        });
        new XPopup.Builder(this)
                .isDestroyOnDismiss(false)
                .asCustom(popup)
                .show();
    }

    // </editor-folder>


    /**
     * 以下为插屏广告
     */

    private TTFullScreenVideoAd mTTFullScreenVideoAd;
    private boolean mLoadSuccess; //是否加载成功
    private boolean mIsLoadedAndShow = true;//广告加载成功并展示

    private TTAdNative adNativeLoader;

    /**
     * 展示广告
     */
    private void showAd() {
        Log.e(TAG, "showAd: ++++++++++");
        mLoadSuccess = false;
//        if (mAdInterstitialFullManager != null) {
//            mAdInterstitialFullManager.loadAdWithCallback(Constants.PGE_INT_POS_ID);
//        }
    }

    private void initAdLoader() {
        mLoadSuccess = false;
        adNativeLoader = TTAdSdk.getAdManager().createAdNative(this);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(Constants.PGE_INT_POS_ID)
                .setOrientation(TTAdConstant.ORIENTATION_VERTICAL)//设置横竖屏方向
                .setMediationAdSlot(new MediationAdSlot.Builder()
                        .setMuted(true)//是否静音
                        .setVolume(0.7f)//设置音量
                        .setBidNotify(true)//竞价结果通知
                        .build())
                .build();

        adNativeLoader.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            public void onError(int code, String message) {
                mLoadSuccess = false;
                Log.d("TAG", "InterstitialFull onError code = " + code + " msg = " + message);
            }

            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                Log.d("TAG", "InterstitialFull onFullScreenVideoLoaded");
                mLoadSuccess = true;
                mTTFullScreenVideoAd = ad;
            }

            public void onFullScreenVideoCached() {
                Log.d("TAG", "InterstitialFull onFullScreenVideoCached");
            }

            public void onFullScreenVideoCached(TTFullScreenVideoAd ad) {
                Log.d("TAG", "InterstitialFull onFullScreenVideoCached");
                mLoadSuccess = true;
                mTTFullScreenVideoAd = ad;
                if (mIsLoadedAndShow && isCurrent) {
                    showInterFullAd();
                    SpUtils.getInstance().putLong(Constants.PGE_INT_POS_ID, System.currentTimeMillis());
                }
            }
        });
    }

    /**
     * 展示广告
     */
    private void showInterFullAd() {
        if (mIsLoadedAndShow && mLoadSuccess) {

            // 展示广告
            this.mTTFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                public void onAdShow() {
                    Log.d("TAG", "InterstitialFull onAdShow");
                }

                public void onAdVideoBarClick() {
                    Log.d("TAG", "InterstitialFull onAdVideoBarClick");
                }

                public void onAdClose() {
                    Log.d("TAG", "InterstitialFull onAdClose");
                }

                public void onVideoComplete() {
                    Log.d("TAG", "InterstitialFull onVideoComplete");
                }

                public void onSkippedVideo() {
                    Log.d("TAG", "InterstitialFull onSkippedVideo");
                }
            });
            this.mTTFullScreenVideoAd.showFullScreenVideoAd(this);
            mIsLoadedAndShow = false;
        } else {
            // TToast.show(this, "请先加载广告");
        }
    }
}
