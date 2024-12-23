package com.goodtech.tq.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.CSJAdError;
import com.bytedance.sdk.openadsdk.CSJSplashAd;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.goodtech.tq.BuildConfig;
import com.goodtech.tq.R;
import com.goodtech.tq.app.App;
import com.goodtech.tq.base.AppExtKt;
import com.goodtech.tq.helpers.BtnLinkHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.modules.citySearch.CitySearchActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.StatusBarUtil;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mSplashContainer = findViewById(R.id.splash_container);

        StatusBarUtil.setImmerseStatusBarSystemUiVisibility(this);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
        if (!TextUtils.isEmpty(saveVersion)) {
            if (SpUtils.getInstance().isAgreePermission()) {
                //  注册
                App.instance.startUsingApp(this);
            }
            if (!LocationSpHelper.getCityListAndLocation().isEmpty()) {
                SpUtils.getInstance().remove(Constants.TIME_LOCATION);
                SpUtils.getInstance().remove(Constants.TIME_WEATHER);
                if (!saveVersion.equals("0")) {
                    WeatherHttpHelper httpHelper = new WeatherHttpHelper(getApplicationContext());
                    httpHelper.getBaseUrl(httpHelper::fetchCitiesWeather);
                }

                SpUtils.getInstance().putBoolean("hadShowInterstitialAD", false);
            }

            BtnLinkHelper.fetchBtnLinks();

            if (SpUtils.getInstance().isAgreePermission()) {
                //加载开屏广告
                mSplashContainer.post(this::loadSplashAd);
                if (PermissionUtils.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    LocationHelper.getInstance().startWithDelay(App.instance, true);
                }
            } else {
                goToMainActivity();
            }

        } else {
            handler.postDelayed(() -> {
                PermissionActivity.redirectTo(SplashActivity.this);
                this.finish();
            }, 500);
        }
    }

    /**
     * 跳转到主页面
     */
    private static final String EXTRA_BACK = "EXTRA_BACK";

    private void goToMainActivity() {
        // Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        // startActivity(intent);
        // overridePendingTransition(0, 0);

        if (!getIntent().getBooleanExtra(EXTRA_BACK, false)) {
            if (LocationSpHelper.getCityListAndLocation().isEmpty()) {
                CitySearchActivity.redirectTo(this, true);
            } else {
                this.startActivity(new Intent(this, MainActivity.class));
            }
        }
//        App.instance.loadCsjAdHolder();
        SpUtils.getInstance().putString(SpUtils.VERSION_APP, AppUtils.getAppVersionName());
        this.finish();
    }

    // <editor-fold defaultstate="collapsed" desc="广告">
    private CSJSplashAd mCsjSplashAd;
    private TTAdNative.CSJSplashAdListener mCSJSplashAdListener;
    private CSJSplashAd.SplashAdListener mCSJSplashInteractionListener;
    private FrameLayout mSplashContainer;

    //开屏广告加载超时时间,建议大于1000,这里为了冷启动第一次加载到广告并且展示,示例设置了2000ms
    private static final int AD_TIME_OUT = 3000;

    /**
     * 加载开屏广告
     */
    private void loadSplashAd() {
        //设置不支持小窗模式
        String mAdUnitId = BuildConfig.PGE_SPLASH_POS_ID;
        /** 2、创建TTAdNative对象 */
        TTAdNative adNativeLoader = TTAdSdk.getAdManager().createAdNative(this);

        /** 1、创建AdSlot对象 */
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mAdUnitId)
                .setImageAcceptedSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight())
                .build();

        /** 3、创建加载、展示监听器 */
        initListeners();

        /** 4、加载广告 */
        adNativeLoader.loadSplashAd(adSlot, mCSJSplashAdListener, 1500);
    }

    private void initListeners() {
        // 广告加载监听器
        this.mCSJSplashAdListener = new TTAdNative.CSJSplashAdListener() {
            @Override
            public void onSplashRenderSuccess(CSJSplashAd csjSplashAd) {
                /** 5、渲染成功后，展示广告 */
                mCsjSplashAd = csjSplashAd;
                csjSplashAd.setSplashAdListener(mCSJSplashInteractionListener);
                View splashView = csjSplashAd.getSplashView();
                AppExtKt.removeFromParent(splashView);
                mSplashContainer.removeAllViews();
                mSplashContainer.addView(splashView);
            }

            @Override
            public void onSplashLoadSuccess(CSJSplashAd csjSplashAd) {
                Log.d("TAG", "splash load success");
            }

            @Override
            public void onSplashLoadFail(CSJAdError csjAdError) {
                Log.d("TAG", "splash load fail, errCode: " + csjAdError.getCode() + ", errMsg: " + csjAdError.getMsg());
                goToMainActivity();
            }

            @Override
            public void onSplashRenderFail(CSJSplashAd csjSplashAd, CSJAdError csjAdError) {
                Log.d("TAG", "splash render fail, errCode: " + csjAdError.getCode() + ", errMsg: " + csjAdError.getMsg());
            }
        };
        // 广告展示监听器
        this.mCSJSplashInteractionListener = new CSJSplashAd.SplashAdListener() {
            @Override
            public void onSplashAdShow(CSJSplashAd csjSplashAd) {
                Log.d("TAG", "splash show");
            }

            @Override
            public void onSplashAdClick(CSJSplashAd csjSplashAd) {
                Log.d("TAG", "splash click");
            }

            @Override
            public void onSplashAdClose(CSJSplashAd csjSplashAd, int i) {
                Log.d("TAG", "splash close");
                goToMainActivity();
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /** 6、在onDestroy中销毁广告 */
        if (mCsjSplashAd != null && mCsjSplashAd.getMediationManager() != null) {
            mCsjSplashAd.getMediationManager().destroy();
        }
    }

    // </editor-fold>
}
