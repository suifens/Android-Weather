package com.goodtech.tq.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
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
import com.goodtech.tq.activity.SplashADActivity;
import com.goodtech.tq.activity.SplashActivity;
import com.goodtech.tq.app.config.GMAdManagerHolder;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.jpush.JPushHelper;
import com.goodtech.tq.signing.SigningActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.widget.DoubleWidgetService;
import com.goodtech.tq.widget.WidgetService;
import com.tbruyelle.rxpermissions2.RxPermissions;
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

        //播放器配置，注意：此为全局配置，按需开启
//         VideoViewManager.setConfig(VideoViewConfig.newBuilder()
//                 .setLogEnabled(BuildConfig.DEBUG) //调试的时候请打开日志，方便排错
//                 /** 软解，支持格式较多，可通过自编译so扩展格式，结合 {@link xyz.doikki.dkplayer.widget.videoview.IjkVideoView} 使用更佳 */
// //                .setPlayerFactory(IjkPlayerFactory.create())
// //                .setPlayerFactory(AndroidMediaPlayerFactory.create()) //不推荐使用，兼容性较差
//                 /** 硬解，支持格式看手机，请使用CpuInfoActivity检查手机支持的格式，结合 {@link xyz.doikki.dkplayer.widget.videoview.ExoVideoView} 使用更佳 */
//                 .setPlayerFactory(ExoMediaPlayerFactory.create())
//                 // 设置自己的渲染view，内部默认TextureView实现
// //                .setRenderViewFactory(SurfaceRenderViewFactory.create())
//                 // 根据手机重力感应自动切换横竖屏，默认false
// //                .setEnableOrientation(true)
//                 // 监听系统中其他播放器是否获取音频焦点，实现不与其他播放器同时播放的效果，默认true
// //                .setEnableAudioFocus(false)
//                 // 视频画面缩放模式，默认按视频宽高比居中显示在VideoView中
// //                .setScreenScaleType(VideoView.SCREEN_SCALE_MATCH_PARENT)
//                 // 适配刘海屏，默认true
// //                .setAdaptCutout(false)
//                 // 移动网络下提示用户会产生流量费用，默认不提示，
//                 // 如果要提示则设置成false并在控制器中监听STATE_START_ABORT状态，实现相关界面，具体可以参考PrepareView的实现
// //                .setPlayOnMobileNetwork(false)
//                 // 进度管理器，继承ProgressManager，实现自己的管理逻辑
// //                .setProgressManager(new ProgressManagerImpl())
//                 .build());

    }

//    public void configDB() {
//        DatabaseHelper.getInstance(getApplicationContext()).openDatabase();
//    }

    @SuppressLint("CheckResult")
    public void startUsingApp(Activity activity) {

        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

        RxPermissions rxPermissions = new RxPermissions(activity);
        if (!SpUtils.getInstance().getBoolean(FIRST_CHECK, true)) {
            startIntent(activity);
        }
        configUM();

        GMAdManagerHolder.init(this);

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(activity, WidgetService.class));
                startForegroundService(new Intent(activity, DoubleWidgetService.class));
            } else {
                startService(new Intent(activity, WidgetService.class));
                startService(new Intent(activity, DoubleWidgetService.class));
            }
            isServiceStarted = true;
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
        CrashReport.initCrashReport(getApplicationContext());
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
                        || activity instanceof SplashADActivity
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
        long interval = SpUtils.getInstance().getLong(LEVEL_TIME, 0L) - System.currentTimeMillis();
        if (!TextUtils.isEmpty( SpUtils.getInstance().getString(SpUtils.VERSION_APP, ""))
                && interval > 1000 * 60) {
            //  离开前台1分钟后返回，则显示启动页广告
            activity.startActivity(new Intent(activity, SplashActivity.class));
            SpUtils.getInstance().putBoolean("hadShowInterstitialAD", false);
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
