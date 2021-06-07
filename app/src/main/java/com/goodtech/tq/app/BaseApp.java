package com.goodtech.tq.app;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goodtech.tq.BuildConfig;
import com.goodtech.tq.MyActivityManager;
import com.goodtech.tq.SplashADActivity;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.location.services.LocationService;
import com.goodtech.tq.utils.Constants;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.managers.setting.GlobalSetting;
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
        registerLifecycle();
        UMConfigure.preInit(this, Constants.UM_APP_ID, BuildConfig.FLAVOR);
    }

    public void startUsingApp() {

        DatabaseHelper.getInstance(getApplicationContext()).openDatabase();

        //  初始化定位sdk，建议在Application中创建
        locationService = new LocationService(getApplicationContext());

        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
//        SDKInitializer.initialize(getApplicationContext());
//        SDKInitializer.setCoordType(CoordType.BD09LL);

        UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE, "");
        //  配置 UM_APP_ID , 标识
        UMConfigure.init(this, Constants.UM_APP_ID, BuildConfig.FLAVOR, UMConfigure.DEVICE_TYPE_PHONE, "");

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
                if (isRunInBackground) {
                    //应用从后台回到前台 需要做的操作
                    mHandler.postDelayed(() -> back2App(activity), 300);
//                    back2App(activity);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
                if (appCount == 0) {
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


//        //  监听生命周期状态
//        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
//            @Override
//            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
//
//            }
//
//            @Override
//            public void onActivityStarted(@NonNull Activity activity) {
//                mCount++;
//                Log.e("TAG", "onActivityStarted: " + mCount);
//            }
//
//            @Override
//            public void onActivityResumed(@NonNull Activity activity) {
//                Log.e("TAG", "onActivityResumed: " + mCount);
//                if (mCount == 1 && onBackground) {
//                    onBackground = false;
//                    Log.e("TAG", "onActivityStarted: 进入到前台");
//                    SplashADActivity.redirectToFront(activity);
//                }
//                MyActivityManager.getInstance().setCurrentActivity(activity);
//            }
//
//            @Override
//            public void onActivityPaused(@NonNull Activity activity) {
//
//            }
//
//            @Override
//            public void onActivityStopped(@NonNull Activity activity) {
//            }
//
//            @Override
//            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
//
//            }
//
//            @Override
//            public void onActivityDestroyed(@NonNull Activity activity) {
//                Log.e("TAG", "onActivityDestroyed: ");
//                mCount = Math.max(mCount - 1, 0);
//                MyActivityManager.getInstance().setCurrentActivity(activity);
//                if (mCount == 0) {
////                    onBackground = true;
//                    Log.e("TAG", "onActivityStopped: 退出到后台");
//                }
//                Log.e("TAG", "onActivityResumed: " + mCount);
//            }
//        });
}
