package com.goodtech.tq;

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
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.StatusBarUtil;


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

        SpUtils.getInstance().remove(Constants.TIME_LOCATION);
        SpUtils.getInstance().remove(Constants.TIME_WEATHER);

        String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
        if (!TextUtils.isEmpty(saveVersion)) {
            //  注册
            BaseApp.getInstance().startUsingApp(this);

            String versionName = DeviceUtils.getVersionName(this);
            if (!saveVersion.equals("0")) {
                LocationSpHelper.saveWithLocation(null);
                LocationHelper.getInstance().start(this);

                WeatherHttpHelper httpHelper = new WeatherHttpHelper(getApplicationContext());
                httpHelper.fetchCitiesWeather();
            }

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
