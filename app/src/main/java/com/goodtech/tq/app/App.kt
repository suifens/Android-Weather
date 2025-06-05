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
import com.goodtech.tq.modules.video.DPHolder
import com.goodtech.tq.modules.video.djx.DJXHolder
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
        const val SDK_SETTINGS_CONFIG = "SDK_Setting_5168917.json"
        private const val LEVEL_TIME = "LEVEL_TIME"
        private const val BACKGROUND_RETURN_DELAY = 300L
        private const val AD_SHOW_INTERVAL = 1000L * 60 * 30
        private const val LOCATION_UPDATE_INTERVAL = 1000L * 60

        @Volatile
        lateinit var instance: App

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var weakTopActivity: WeakReference<Activity>

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var weakTopRunningActivity: WeakReference<Activity>

        val topActivity: Activity?
            get() = weakTopActivity.get()

        val topRunningActivity: Activity?
            get() = weakTopActivity.get()
    }

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

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeApp()
    }

    private fun initializeApp() {
        try {
            initializeUM()
            initializeMMKV()
            registerLifecycle()
            initializeDatabase()
            JCollectionAuth.setAuth(this, false)
        } catch (e: Exception) {
            Log.e(TAG, "初始化失败", e)
        }
    }

    private fun initializeUM() {
        try {
            UMConfigure.preInit(this, Constants.UM_APP_ID, BuildConfig.FLAVOR)
        } catch (e: Exception) {
            Log.e(TAG, "UM初始化失败", e)
        }
    }

    private fun initializeMMKV() {
        MMKV.initialize(this)
    }

    private fun initializeDatabase() {
        DatabaseHelper.getInstance(applicationContext).openDatabase()
    }

    @SuppressLint("CheckResult")
    fun startUsingApp(activity: Activity?) {
        mainScope.launch {
            initializeAdSDK()
            configUM()
            initializeJPush()
        }
    }

    private fun initializeAdSDK() {
        TTAdManagerHolder.init(this)
    }

    private fun initializeJPush() {

        JPushInterface.setDebugMode(false)
        JPushInterface.init(this)
        JCollectionAuth.setAuth(this, true);

        val registerId = JPushInterface.getRegistrationID(instance)
        Log.i(TAG, "startUsingApp: register id = $registerId")
        if (!TextUtils.isEmpty(registerId)) {
            mJPushRegId = registerId
        }
    }

    fun loadCsjAdHolder() {
        CsjAdHolder.init(SITE_ID, instance, object : TTAdSdk.Callback {
            override fun success() {
                Log.e(TAG, "CsjAdHolder init success")
                initializeVideoSDKs()
            }

            override fun fail(code: Int, msg: String?) {
                Log.e(TAG, "CsjAdHolder init fail: $code, $msg")
            }
        })
    }

    private fun initializeVideoSDKs() {
        initDJX()
        initDP()
    }

    fun initDJX() {
        DJXHolder.init(instance) {
            Bus.getInstance().sendEvent(DJXStartEvent(it))
        }
    }

    fun initDP() {
        DPHolder.init(instance)
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
        UMConfigure.init(
            this,
            Constants.UM_APP_ID,
            BuildConfig.FLAVOR,
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)
        initializeBugly()
    }

    private fun initializeBugly() {
        CrashReport.initCrashReport(
            applicationContext,
            Constants.BUGLY_APP_ID,
            BuildConfig.DEBUG_MODE
        )
    }

    private fun registerLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            
            override fun onActivityStarted(activity: Activity) {
                appCount++
                MyActivityManager.getInstance().setCurrentActivity(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                if (isRunInBackground) {
                    mHandler.postDelayed({ back2App(activity) }, BACKGROUND_RETURN_DELAY)
                }
            }

            override fun onActivityPaused(activity: Activity) {}
            
            override fun onActivityStopped(activity: Activity) {
                appCount--
                if (appCount == 0 && !isSpecialActivity(activity)) {
                    leaveApp(activity)
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