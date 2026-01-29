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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.bytedance.sdk.openadsdk.CSJSplashAd;
import com.goodtech.tq.R;
import com.goodtech.tq.app.App;
import com.goodtech.tq.base.AppExtKt;
import com.goodtech.tq.helpers.BtnLinkHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.modules.citySearch.CitySearchActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.PermissionManager;
import com.goodtech.tq.utils.StatusBarUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";
    private static final String EXTRA_BACK = "EXTRA_BACK";
    private static final int SPLASH_DELAY = 500;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private FrameLayout mSplashContainer;
    private SplashViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        initViews();
        if (SpUtils.getInstance().isAgreePermission()) {
            viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
            setupObservers();
        }
        checkAppVersion();
    }

    private void initViews() {
        mSplashContainer = findViewById(R.id.splash_container);
        StatusBarUtil.setImmerseStatusBarSystemUiVisibility(this);
    }

    private void setupObservers() {
        viewModel.getIsAdLoaded().observe(this, isLoaded -> {
            if (isLoaded) {
                Log.d(TAG, "广告加载成功");
            }
        });

        viewModel.getIsAdShown().observe(this, isShown -> {
            if (isShown) {
                Log.d(TAG, "广告展示成功");
            } else {
                goToMainActivity();
            }
        });

        viewModel.getIsWeatherDataReady().observe(this, isReady -> {
            if (isReady) {
                Log.d(TAG, "天气数据准备完成");
            }
        });

        viewModel.getIsPreloading().observe(this, isPreloading -> {
            if (isPreloading) {
                Log.d(TAG, "正在预加载广告");
            }
        });

        viewModel.getQueueSize().observe(this, size -> {

        });

        viewModel.getSplashAd().observe(this, splashAd -> {
            if (splashAd != null) {
                View splashView = splashAd.getSplashView();
                AppExtKt.removeFromParent(splashView);
                mSplashContainer.removeAllViews();
                mSplashContainer.addView(splashView);
            }
        });
    }

    private void checkAppVersion() {
        String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
        if (TextUtils.isEmpty(saveVersion)) {
            handler.postDelayed(() -> {
                PermissionActivity.redirectTo(SplashActivity.this);
                finish();
            }, SPLASH_DELAY);
            return;
        }

        initializeApp();
    }

    private void initializeApp() {
        if (SpUtils.getInstance().isAgreePermission()) {
            // 使用权限管理工具安全初始化SDK
            PermissionManager.INSTANCE.safeInitializeSDK(this);
            viewModel.prepareWeatherData();
            BtnLinkHelper.fetchBtnLinks();
            loadSplashAd();
        } else {
            goToMainActivity();
        }
    }

    private void loadSplashAd() {
        if (!PermissionUtils.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            goToMainActivity();
            return;
        }

        // 检查是否在去广告有效期内
        if (com.goodtech.tq.utils.AdRemovalManager.INSTANCE.isAdRemovalActive()) {
            // 在去广告有效期内，直接跳转到主页面，不加载广告
            goToMainActivity();
            return;
        }

        viewModel.loadSplashAd();
        
        // 设置广告加载超时
        handler.postDelayed(() -> {
            Boolean isAdLoaded = viewModel.getIsAdLoaded().getValue();
            Boolean isAdShown = viewModel.getIsAdShown().getValue();
            if (isAdLoaded == null || !isAdLoaded || isAdShown == null || !isAdShown) {
                goToMainActivity();
            }
        }, Constants.AD_TIME_OUT);
    }

    private void goToMainActivity() {
        if (getIntent().getBooleanExtra(EXTRA_BACK, false)) {
            finish();
            return;
        }

        if (LocationSpHelper.getCityListAndLocation().isEmpty()) {
            CitySearchActivity.redirectTo(this, true);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        SpUtils.getInstance().putString(SpUtils.VERSION_APP, AppUtils.getAppVersionName());
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.destroyAd();
        }
    }
}
