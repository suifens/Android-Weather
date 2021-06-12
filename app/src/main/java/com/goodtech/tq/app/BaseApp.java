package com.goodtech.tq.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;

import com.goodtech.tq.BuildConfig;
import com.goodtech.tq.MainActivity;
import com.goodtech.tq.MyActivityManager;
import com.goodtech.tq.SplashADActivity;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.location.services.LocationService;
import com.goodtech.tq.utils.Constants;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.managers.setting.GlobalSetting;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tbruyelle.rxpermissions2.RxPermissionsFragment;
import com.umeng.commonsdk.UMConfigure;

public class BaseApp extends Application {
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    public LocationService locationService;
    public Vibrator mVibrator;
    private static BaseApp mApplication;

    public static BaseApp getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        try {
            UMConfigure.preInit(this, Constants.UM_APP_ID, BuildConfig.FLAVOR);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //  Activity生命周期监听
        registerLifecycle();
    }

    public void configLocation() {
        if (locationService == null) {
            //  初始化定位sdk，建议在Application中创建
            locationService = new LocationService(getApplicationContext());
        }
    }

    @SuppressLint("CheckResult")
    public void startUsingApp(Activity activity) {

        DatabaseHelper.getInstance(getApplicationContext()).openDatabase();

        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.requestEach(Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.READ_PHONE_STATE
                , Manifest.permission.ACCESS_WIFI_STATE).subscribe(permission ->
        {
            if (permission.granted) {
                switch (permission.name) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        configLocation();
                        break;
                    case Manifest.permission.READ_PHONE_STATE:
                    case Manifest.permission.ACCESS_WIFI_STATE:
                        //  配置 UM_APP_ID , 标识
                        UMConfigure.init(this, Constants.UM_APP_ID, BuildConfig.FLAVOR, UMConfigure.DEVICE_TYPE_PHONE, "");
                        break;
                }
            }
        });

        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

        // 通过调用此方法初始化 SDK。如果需要在多个进程拉取广告，每个进程都需要初始化 SDK。
        GDTADManager.getInstance().initWith(getApplicationContext(), Constants.APP_ID);
        GlobalSetting.setChannel(BuildConfig.BAIDU_CHANNEL);
    }

    public int appCount = 0;
    public boolean isRunInBackground = false;
    public void registerLifecycle() {

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                appCount++;
                MyActivityManager.getInstance().setCurrentActivity(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (isRunInBackground
                        && MainActivity.class.toString().contains(MyActivityManager.getInstance().getBaseActivityName())) {
                    //应用从后台回到前台 需要做的操作
                    mHandler.postDelayed(() -> back2App(activity), 300);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
                if (appCount == 0 && MainActivity.class.toString().contains(MyActivityManager.getInstance().getBaseActivityName())) {
                    //应用进入后台 需要做的操作
                    leaveApp(activity);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    /**
     * 从后台回到前台需要执行的逻辑
     */
    private void back2App(Activity activity) {
        isRunInBackground = false;
        SplashADActivity.redirectToFront(activity);
    }

    /**
     * 离开应用 压入后台或者退出应用
     */
    private void leaveApp(Activity activity) {
        isRunInBackground = true;
    }
}
