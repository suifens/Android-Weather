package com.goodtech.tq.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import com.goodtech.tq.BuildConfig;
import com.goodtech.tq.activity.MyActivityManager;
import com.goodtech.tq.activity.SettingActivity;
import com.goodtech.tq.activity.SplashActivity;
import com.goodtech.tq.ad.TTAdManagerHolder;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.jpush.JPushHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.signing.SigningActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.PermissionUtil;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.widget.DoubleWidgetService;
import com.goodtech.tq.widget.WidgetService;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import cn.jpush.android.api.JPushInterface;


public class BaseApp extends Application {
    private static final String TAG = "BaseApp";
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    public Vibrator mVibrator;
    //  需要获取phone state权限
    public boolean needStatePerm = true;
    //  定位权限，
    public boolean needLocationPerm = true;
    private static BaseApp mApplication;
    private String mJPushRegId;

    public static BaseApp getInstance() {
        return mApplication;
    }

    //  首次请求权限
    public static final String FIRST_CHECK = "FIRST_CHECK";

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        try {
            UMConfigure.preInit(this, Constants.UM_APP_ID, BuildConfig.FLAVOR);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        //  MMKV 存储配置
        MMKV.initialize(this);
        //  Activity生命周期监听
        registerLifecycle();

        DatabaseHelper.getInstance(getApplicationContext()).openDatabase();
    }

    @SuppressLint("CheckResult")
    public void startUsingApp(Activity activity) {

        TTAdManagerHolder.init(this);

        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

        configUM();

        JPushInterface.setDebugMode(true);

        //  极光推送 register id
        String registerId = JPushInterface.getRegistrationID(BaseApp.getInstance());
        Log.e(TAG, "startUsingApp: register id = " + registerId);
        if (!TextUtils.isEmpty(registerId)) {
            BaseApp.getInstance().setJPushRegId(registerId);
        }
    }

    private boolean isServiceStarted = false;

    public void startIntent(Activity activity) {
        if (!isServiceStarted) {
            startService(activity);
            isServiceStarted = true;
        }
    }

    public void startService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && PermissionUtil.canDrawOverlays(context)) {
            startForegroundService(new Intent(context, WidgetService.class));
            startForegroundService(new Intent(context, DoubleWidgetService.class));
        } else {
            startService(new Intent(context, WidgetService.class));
            startService(new Intent(context, DoubleWidgetService.class));
        }
    }


    /**
     * 友盟配置
     */
    private void configUM() {
        //  配置 UM_APP_ID , 标识
        UMConfigure.init(this, Constants.UM_APP_ID, BuildConfig.FLAVOR, UMConfigure.DEVICE_TYPE_PHONE, "");
        //手动采集选择
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);

        /// bug收集
        CrashReport.initCrashReport(getApplicationContext(), Constants.BUGLY_APP_ID, BuildConfig.DEBUG_MODE);
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
                if (isRunInBackground) {
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
                if (appCount == 0 && !(activity instanceof SplashActivity
                        || activity instanceof SettingActivity
                        || activity instanceof SigningActivity)) {
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
        long interval = System.currentTimeMillis() - SpUtils.getInstance().getLong(LEVEL_TIME, 0L);
        if (!TextUtils.isEmpty(SpUtils.getInstance().getString(SpUtils.VERSION_APP, ""))) {
            if (Math.abs(interval) > 1000 * 60 * 30) {
                //  离开前台1分钟后返回，则显示启动页广告
                activity.startActivity(new Intent(activity, SplashActivity.class));
                SpUtils.getInstance().putBoolean("hadShowInterstitialAD", false);
            } else if (Math.abs(interval) > 1000 * 60 && SpUtils.getInstance().isAgreePermission()) {
                //加载开屏广告
                LocationHelper.getInstance().startWithDelay(BaseApp.getInstance(), true);
            }
        }
    }

    /**
     * 离开应用 压入后台或者退出应用
     */
    private static final String LEVEL_TIME = "LEVEL_TIME";

    private void leaveApp(Activity activity) {
        SpUtils.getInstance().putLong(LEVEL_TIME, System.currentTimeMillis());
        isRunInBackground = true;
    }

    public String getJPushRegId() {
        return mJPushRegId;
    }

    public void setJPushRegId(String jPushRegId) {
        this.mJPushRegId = jPushRegId;
        Log.e(TAG, "setJPushRegId: " + jPushRegId);
        //  恢复极光推送
        JPushHelper.resumePush();
    }
}
