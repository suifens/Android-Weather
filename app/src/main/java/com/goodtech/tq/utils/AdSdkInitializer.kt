package com.goodtech.tq.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.goodtech.tq.ad.GdtPrivacyConfig
import com.goodtech.tq.ad.TTAdManagerHolder
import com.goodtech.tq.app.App
import com.goodtech.tq.modules.video.CsjAdHolder

/**
 * 广告 SDK 延迟初始化：用户同意隐私后，仅在应用处于前台且即将展示/加载广告时初始化，
 * 避免后台静默注册安装/卸载广播监听（优量汇 com.qq.e.comm 等）。
 */
object AdSdkInitializer {

    private const val TAG = "AdSdkInitializer"

    @Volatile
    private var isAdSdkInitialized = false

    @Volatile
    private var isInitializing = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingCallbacks = mutableListOf<Runnable>()
    private var waitingForeground = false

    @JvmStatic
    fun isInitialized(): Boolean = isAdSdkInitialized

    @JvmStatic
    @JvmOverloads
    fun ensureInitialized(onReady: Runnable? = null) {
        if (!SpUtils.getInstance().isAgreePermission()) {
            Log.d(TAG, "未同意隐私，跳过广告SDK初始化")
            return
        }
        if (isAdSdkInitialized) {
            onReady?.run()
            return
        }
        synchronized(this) {
            onReady?.let { pendingCallbacks.add(it) }
            if (isInitializing) {
                return
            }
            if (!App.isInForeground()) {
                waitingForeground = true
                Log.d(TAG, "应用不在前台，延迟广告SDK初始化至回前台")
                return
            }
            isInitializing = true
        }
        startInit()
    }

    /** Activity 回到前台时，继续被延迟的广告 SDK 初始化 */
    @JvmStatic
    fun onAppForeground() {
        if (!waitingForeground || isAdSdkInitialized || isInitializing) {
            return
        }
        if (!SpUtils.getInstance().isAgreePermission()) {
            return
        }
        synchronized(this) {
            if (isInitializing || isAdSdkInitialized) {
                return
            }
            waitingForeground = false
            isInitializing = true
        }
        Log.d(TAG, "应用已回前台，继续延迟的广告SDK初始化")
        startInit()
    }

    private fun startInit() {
        Log.d(TAG, "开始延迟初始化广告SDK（前台）")
        try {
            GdtPrivacyConfig.applyBeforeInit()
            TTAdManagerHolder.init(App.instance)
            CsjAdHolder.init(App.VIDEO_SITE_ID, App.instance, object : TTAdSdk.Callback {
                override fun success() {
                    onAdSdkReady()
                }

                override fun fail(code: Int, msg: String?) {
                    Log.e(TAG, "CsjAdHolder init fail: $code, $msg")
                    onAdSdkReady()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "广告SDK初始化异常", e)
            onAdSdkReady()
        }
    }

    private fun onAdSdkReady() {
        App.instance.initVideoSDKsAfterAd()
        synchronized(this) {
            isAdSdkInitialized = true
            isInitializing = false
            waitingForeground = false
            val callbacks = pendingCallbacks.toList()
            pendingCallbacks.clear()
            mainHandler.post {
                callbacks.forEach { it.run() }
            }
        }
        Log.d(TAG, "广告SDK初始化完成")
    }
}
