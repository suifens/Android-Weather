package com.goodtech.tq.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.PermissionUtils
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.goodtech.tq.BuildConfig
import com.goodtech.tq.activity.MyActivityManager
import com.goodtech.tq.activity.SettingActivity
import com.goodtech.tq.activity.SplashActivity
import com.goodtech.tq.ad.TTAdManagerHolder
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.event.DJXStartEvent
import com.goodtech.tq.helpers.DatabaseHelper
import com.goodtech.tq.jpush.JPushHelper
import com.goodtech.tq.location.helper.LocationHelper
import com.goodtech.tq.modules.signing.SigningActivity
import com.goodtech.tq.modules.video.CsjAdHolder
import com.goodtech.tq.modules.video.djx.DJXHolder
import com.goodtech.tq.modules.video.DPHolder
import com.goodtech.tq.utils.Constants
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.widget.DoubleWidgetService
import com.goodtech.tq.widget.WidgetService
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import java.lang.ref.WeakReference


class App : Application() {

    companion object {
        const val SITE_ID = "5168917"
        const val SDK_SETTINGS_CONFIG = "SDK_Setting_5168917.json"
        private const val TAG = "BaseApp"

        @Volatile
        lateinit var instance: App

        @SuppressLint("StaticFieldLeak")
        @Volatile
        lateinit var weakTopActivity: WeakReference<Activity>

        @SuppressLint("StaticFieldLeak")
        @Volatile
        lateinit var weakTopRunningActivity: WeakReference<Activity>

        val topActivity: Activity?
            get() = weakTopActivity.get()

        val topRunningActivity: Activity?
            get() = weakTopActivity.get()
    }

    private val mHandler = Handler(Looper.getMainLooper())
    val mVibrator: Vibrator by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }
    var needStatePerm: Boolean = true
    var needLocationPerm: Boolean = true
    var mJPushRegId: String? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            UMConfigure.preInit(this, Constants.UM_APP_ID, BuildConfig.FLAVOR)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        //  MMKV 存储配置
        MMKV.initialize(this)
        //  Activity生命周期监听
        registerLifecycle()

        DatabaseHelper.getInstance(applicationContext).openDatabase()
    }

    @SuppressLint("CheckResult")
    fun startUsingApp(activity: Activity?) {
        TTAdManagerHolder.init(this)
        configUM()
        JPushInterface.setDebugMode(true)

        //  极光推送 register id
        val registerId = JPushInterface.getRegistrationID(instance)
        Log.e(TAG, "startUsingApp: register id = $registerId")
        if (!TextUtils.isEmpty(registerId)) {
            mJPushRegId = registerId
        }

        // 业务 SDK 初始化前请确保 CSJ SDK 正常初始化，初始化逻辑建议都放在 application.onCreate()
        CsjAdHolder.init(SITE_ID, instance, object : TTAdSdk.Callback {
            override fun success() {
                Log.e(TAG, "CsjAdHolder init success")

                // 初始化短剧 sdk
                DJXHolder.init(instance) {
                    Bus.getInstance().sendEvent(DJXStartEvent(it))
                }

                // 初始化小视频 sdk
//                DPHolder.init(instance)
            }

            override fun fail(code: Int, msg: String?) {
                Log.e(TAG, "CsjAdHolder init fail: $code, $msg")
            }
        })
    }

    private var isServiceStarted = false

    fun startIntent(activity: Activity) {
        if (!isServiceStarted) {
            startService(activity)
            isServiceStarted = true
        }
    }

    fun startService(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, WidgetService::class.java))
            context.startForegroundService(Intent(context, DoubleWidgetService::class.java))
        } else {
            context.startService(Intent(context, WidgetService::class.java))
            context.startService(Intent(context, DoubleWidgetService::class.java))
        }
    }


    /**
     * 友盟配置
     */
    private fun configUM() {
        //  配置 UM_APP_ID , 标识
        UMConfigure.init(
            this,
            Constants.UM_APP_ID,
            BuildConfig.FLAVOR,
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        //手动采集选择
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)

        /// bug收集
        CrashReport.initCrashReport(
            applicationContext,
            Constants.BUGLY_APP_ID,
            BuildConfig.DEBUG_MODE
        )
    }

    var appCount = 0
    var isRunInBackground = false

    fun registerLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                appCount++
                MyActivityManager.getInstance().currentActivity = activity
            }

            override fun onActivityResumed(activity: Activity) {
                if (isRunInBackground) {
                    //应用从后台回到前台 需要做的操作
                    mHandler.postDelayed({ back2App(activity) }, 300)
                }
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                appCount--
                if (appCount == 0 && !(activity is SplashActivity
                            || activity is SettingActivity
                            || activity is SigningActivity)
                ) {
                    //应用进入后台 需要做的操作
                    leaveApp(activity)
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    /**
     * 从后台回到前台需要执行的逻辑
     */
    private fun back2App(activity: Activity) {
        isRunInBackground = false
        val interval = System.currentTimeMillis() - SpUtils.getInstance().getLong(LEVEL_TIME, 0L)
        if (!TextUtils.isEmpty(SpUtils.getInstance().getString(SpUtils.VERSION_APP, ""))) {
            if (Math.abs(interval) > 1000 * 60 * 30) {
                //  离开前台1分钟后返回，则显示启动页广告
                activity.startActivity(Intent(activity, SplashActivity::class.java))
                SpUtils.getInstance().putBoolean("hadShowInterstitialAD", false)
            } else if (Math.abs(interval) > 1000 * 60 && SpUtils.getInstance().isAgreePermission
                && PermissionUtils.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                //加载开屏广告
                LocationHelper.getInstance().startWithDelay(instance, true)
            }
        }
    }

    /**
     * 离开应用 压入后台或者退出应用
     */
    private val LEVEL_TIME = "LEVEL_TIME"

    private fun leaveApp(activity: Activity) {
        SpUtils.getInstance().putLong(LEVEL_TIME, System.currentTimeMillis())
        isRunInBackground = true
    }

    fun setJPushRegId(jPushRegId: String) {
        this.mJPushRegId = jPushRegId
        Log.e(TAG, "setJPushRegId: $jPushRegId")
        //  恢复极光推送
        JPushHelper.resumePush()
    }

}