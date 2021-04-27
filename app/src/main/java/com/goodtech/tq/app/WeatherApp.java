package com.goodtech.tq.app;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goodtech.tq.SplashADActivity;
import com.goodtech.tq.SplashActivity;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.location.services.LocationService;
import com.goodtech.tq.utils.Constants;
import com.qq.e.comm.managers.GDTADManager;
import com.tencent.mmkv.MMKV;
import com.umeng.commonsdk.UMConfigure;

public class WeatherApp extends Application {
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    public LocationService locationService;
    public Vibrator mVibrator;
    private static WeatherApp mApplication;

    public static WeatherApp getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        DatabaseHelper.getInstance(getApplicationContext()).openDatabase();

        registerLifecycle();

        //  MMKV 存储配置
        MMKV.initialize(this);
    }

    public void startUsingApp() {

        //  初始化定位sdk，建议在Application中创建
        locationService = new LocationService(getApplicationContext());

        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
//        SDKInitializer.initialize(getApplicationContext());
//        SDKInitializer.setCoordType(CoordType.BD09LL);

        UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE, "");

        // 通过调用此方法初始化 SDK。如果需要在多个进程拉取广告，每个进程都需要初始化 SDK。
        GDTADManager.getInstance().initWith(getApplicationContext(), Constants.APP_ID);
    }

    public int mCount = 0;
    public void registerLifecycle() {

        //  监听生命周期状态
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                mCount++;

//                if (mCount == 1 && !(activity instanceof SplashActivity)) {
//                    Log.e("TAG", "onActivityStarted: 进入到前台");
//                    SplashADActivity.redirectTo(activity);
//                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (mCount == 1
                        && !(activity instanceof SplashActivity)
                        && !(activity instanceof SplashADActivity))
                {
                    Log.e("TAG", "onActivityStarted: 进入到前台");
                    SplashADActivity.redirectToFront(activity);
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                mCount = Math.max(mCount - 1, 0);
                if (mCount == 0) {
                    Log.e("TAG", "onActivityStopped: 退出到后台");
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }
}
