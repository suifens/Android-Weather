package com.goodtech.tq.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import cn.jiguang.api.utils.JCollectionAuth
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.PermissionUtils
import com.goodtech.tq.BuildConfig
import com.goodtech.tq.activity.MainActivity
import com.goodtech.tq.activity.MyActivityManager
import com.goodtech.tq.activity.SettingActivity
import com.goodtech.tq.activity.SplashActivity
import com.goodtech.tq.helpers.DatabaseHelper
import com.goodtech.tq.jpush.JPushHelper
import com.goodtech.tq.location.helper.LocationHelper
import com.goodtech.tq.modules.signing.SigningActivity
import com.goodtech.tq.modules.video.DPHolder
import com.goodtech.tq.modules.video.djx.DJXHolder
import com.goodtech.tq.utils.AdSdkInitializer
import com.goodtech.tq.utils.Constants
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.widget.DoubleWidgetService
import com.goodtech.tq.widget.WidgetService
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class App : Application() {

    companion object {
        private const val TAG = "BaseApp"
        private const val SITE_ID = "5168917"
        const val VIDEO_SITE_ID = SITE_ID
        const val SDK_SETTINGS_CONFIG = "SDK_Setting_5168917.json"
        private const val LEVEL_TIME = "LEVEL_TIME"
        private const val BACKGROUND_RETURN_DELAY = 300L
        private const val AD_SHOW_INTERVAL = 1000L * 60 * 30
        private const val LOCATION_UPDATE_INTERVAL = 1000L * 60

        @Volatile
        @JvmStatic
        lateinit var instance: App

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var weakTopActivity: WeakReference<Activity?> = WeakReference(null)

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var weakTopRunningActivity: WeakReference<Activity>

        val topActivity: Activity?
            get() = weakTopActivity.get()

        val topRunningActivity: Activity?
            get() = weakTopActivity.get()

        @JvmStatic
        fun ensureAdSdkInitialized(onReady: Runnable? = null) {
            AdSdkInitializer.ensureInitialized(onReady)
        }

        /** 是否有 Activity 处于前台，用于延迟广告 SDK 初始化，避免后台注册安装/卸载监听 */
        @JvmStatic
        fun isInForeground(): Boolean = topActivity != null
    }

    var mainActivity: MainActivity? = null

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val mHandler = Handler(Looper.getMainLooper())
    private val mVibrator: Vibrator by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }
    
    var needStatePerm: Boolean = true
    var needLocationPerm: Boolean = true
    var mJPushRegId: String? = null
    private var isServiceStarted = false
    private var appCount = 0
    private var isRunInBackground = false
    //  加载插屏广告了
    var hadInitAd = false
    
    // 标记核心 SDK（推送/统计/崩溃）是否已初始化
    private var isCoreSDKInitialized = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupGlobalExceptionHandler()
        initializeApp()
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "未捕获的异常: ${thread.name}", throwable)
                
                // 如果是视频引擎相关的异常，记录详细信息
                if (throwable.stackTraceToString().contains("TTVideoEngine") || 
                    throwable.stackTraceToString().contains("StrategyGearABR")) {
                    Log.e(TAG, "视频引擎异常详情", throwable)
                }
                
                // 可以在这里添加崩溃上报逻辑
                // CrashReport.postCatchedException(throwable)
            } catch (e: Exception) {
                Log.e(TAG, "处理未捕获异常时出错", e)
            } finally {
                // 调用默认处理器
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun initializeApp() {
        try {
            // 只初始化基础组件，不初始化可能请求权限的SDK
            initializeMMKV()
            registerLifecycle()
            initializeDatabase()
        } catch (e: Exception) {
            Log.e(TAG, "初始化失败", e)
        }
    }

    private fun initializeMMKV() {
        MMKV.initialize(this)
    }

    private fun initializeDatabase() {
        DatabaseHelper.getInstance(applicationContext).openDatabase()
    }

    /**
     * 用户同意隐私政策后调用：仅初始化推送/统计/崩溃等核心 SDK，不初始化广告 SDK。
     */
    @SuppressLint("CheckResult")
    fun startUsingApp(activity: Activity?) {
        startCoreSdk()
    }

    @SuppressLint("CheckResult")
    fun startCoreSdk() {
        if (isCoreSDKInitialized) {
            Log.d(TAG, "核心SDK已经初始化，跳过重复初始化")
            return
        }
        if (!SpUtils.getInstance().isAgreePermission) {
            Log.d(TAG, "未同意隐私政策，跳过核心SDK初始化")
            return
        }

        mainScope.launch {
            try {
                Log.d(TAG, "开始初始化核心SDK...")
                configUM()
                initializeJPush()
                isCoreSDKInitialized = true
                Log.d(TAG, "核心SDK初始化完成")
            } catch (e: Exception) {
                Log.e(TAG, "核心SDK初始化失败", e)
            }
        }
    }

    private fun initializeJPush() {
        try {
            Log.d(TAG, "初始化极光推送...")
            JPushInterface.setDebugMode(false)
            JPushInterface.init(this)
            JCollectionAuth.setAuth(this, true)

            val registerId = JPushInterface.getRegistrationID(instance)
            if (!TextUtils.isEmpty(registerId)) {
                mJPushRegId = registerId
            }
        } catch (e: Exception) {
            Log.e(TAG, "初始化极光推送失败", e)
        }
    }

    fun initVideoSDKsAfterAd() {
        initVideoSDKs()
    }

    private fun initVideoSDKs() {
        try {
            Log.d(TAG, "初始化视频SDK...")
            initDJX()
            initDP()
        } catch (e: Exception) {
            Log.e(TAG, "初始化视频SDK失败", e)
        }
    }

    fun initDJX() {
        try {
            Log.d(TAG, "初始化DJX SDK...")
            DJXHolder.init(instance)
        } catch (e: Exception) {
            Log.e(TAG, "初始化DJX SDK失败", e)
        }
    }

    fun initDP() {
        try {
            Log.d(TAG, "初始化DP SDK...")
            DPHolder.init(instance)
        } catch (e: Exception) {
            Log.e(TAG, "初始化DP SDK失败", e)
        }
    }

    fun startIntent(activity: Activity) {
        if (!isServiceStarted) {
            startService(activity)
            isServiceStarted = true
        }
    }

    fun startService(context: Context) {
        try {
            context.startService(Intent(context, WidgetService::class.java))
            context.startService(Intent(context, DoubleWidgetService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "启动服务失败", e)
        }
    }

    private fun configUM() {
        try {
            Log.d(TAG, "初始化友盟统计...")
            UMConfigure.init(
                this,
                Constants.UM_APP_ID,
                BuildConfig.FLAVOR,
                UMConfigure.DEVICE_TYPE_PHONE,
                ""
            )
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)
            initializeBugly()
        } catch (e: Exception) {
            Log.e(TAG, "初始化友盟统计失败", e)
        }
    }

    private fun initializeBugly() {
        try {
            Log.d(TAG, "初始化Bugly...")
            CrashReport.initCrashReport(
                applicationContext,
                Constants.BUGLY_APP_ID,
                BuildConfig.DEBUG_MODE
            )
        } catch (e: Exception) {
            Log.e(TAG, "初始化Bugly失败", e)
        }
    }

    private fun registerLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            
            override fun onActivityStarted(activity: Activity) {
                appCount++
                weakTopActivity = WeakReference(activity)
                MyActivityManager.getInstance().setCurrentActivity(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                weakTopActivity = WeakReference(activity)
                if (isRunInBackground) {
                    mHandler.postDelayed({ back2App(activity) }, BACKGROUND_RETURN_DELAY)
                }
                AdSdkInitializer.onAppForeground()
            }

            override fun onActivityPaused(activity: Activity) {}
            
            override fun onActivityStopped(activity: Activity) {
                appCount--
                if (appCount == 0) {
                    weakTopActivity = WeakReference(null)
                    if (!isSpecialActivity(activity)) {
                        leaveApp(activity)
                    }
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun isSpecialActivity(activity: Activity): Boolean {
        return activity is SplashActivity || 
               activity is SettingActivity || 
               activity is SigningActivity
    }

    private fun back2App(activity: Activity) {
        isRunInBackground = false
        val interval = System.currentTimeMillis() - SpUtils.getInstance().getLong(LEVEL_TIME, 0L)
        
        if (TextUtils.isEmpty(SpUtils.getInstance().getString(SpUtils.VERSION_APP, ""))) {
            return
        }

        when {
            Math.abs(interval) > AD_SHOW_INTERVAL -> {
                activity.startActivity(Intent(activity, SplashActivity::class.java))
                SpUtils.getInstance().putBoolean("hadShowInterstitialAD", false)
            }
            Math.abs(interval) > LOCATION_UPDATE_INTERVAL && 
            SpUtils.getInstance().isAgreePermission &&
            PermissionUtils.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                LocationHelper.getInstance().startWithDelay(instance, true)
            }
        }
    }

    private fun leaveApp(activity: Activity) {
        SpUtils.getInstance().putLong(LEVEL_TIME, System.currentTimeMillis())
        isRunInBackground = true
    }

    fun setJPushRegId(jPushRegId: String) {
        this.mJPushRegId = jPushRegId
        Log.e(TAG, "setJPushRegId: $jPushRegId")
        JPushHelper.resumePush()
    }
}