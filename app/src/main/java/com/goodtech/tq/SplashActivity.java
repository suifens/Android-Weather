package com.goodtech.tq;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.StatusBarUtil;

import cn.jpush.android.api.JPushInterface;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends Activity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        StatusBarUtil.setImmerseStatusBarSystemUiVisibility(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
        if (!TextUtils.isEmpty(saveVersion)) {
            //  注册
            BaseApp.getInstance().startUsingApp(this, true, false);

            SpUtils.getInstance().remove(Constants.TIME_LOCATION);
            SpUtils.getInstance().remove(Constants.TIME_WEATHER);
            if (!saveVersion.equals("0")) {
                LocationSpHelper.saveWithLocation(null);
                LocationHelper.getInstance().start(this);

                WeatherHttpHelper httpHelper = new WeatherHttpHelper(getApplicationContext());
                httpHelper.getBaseUrl(httpHelper::fetchCitiesWeather);
            }

            SpUtils.getInstance().putBoolean("hadShowInterstitialAD", false);

            SplashADActivity.redirectTo(this);
            overridePendingTransition(0, 0);
            this.finish();

        } else {
            handler.postDelayed(() -> {
                PermissionActivity.redirectTo(SplashActivity.this);
                this.finish();
            }, 500);
        }
    }
}
