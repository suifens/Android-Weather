package com.goodtech.tq.utils

import android.util.Log
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.goodtech.tq.ad.TTAdManagerHolder
import com.goodtech.tq.app.App
import com.goodtech.tq.modules.video.CsjAdHolder

/**
 * 广告 SDK 延迟初始化：用户同意隐私后，仅在即将展示/加载广告时初始化，避免启动即读安装列表等。
 */
object AdSdkInitializer {

    private const val TAG = "AdSdkInitializer"

    @Volatile
    private var isAdSdkInitialized = false

    @Volatile
    private var isInitializing = false

    private val pendingCallbacks = mutableListOf<Runnable>()

    @JvmStatic
    fun isInitialized(): Boolean = isAdSdkInitialized

    @JvmStatic
    @JvmOverloads
    fun ensureInitialized(onReady: Runnable? = null) {
        if (!SpUtils.getInstance().isAgreePermission()) {
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
            isInitializing = true
        }
        Log.d(TAG, "开始延迟初始化广告SDK")
        try {
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
            val callbacks = pendingCallbacks.toList()
            pendingCallbacks.clear()
            callbacks.forEach { it.run() }
        }
        Log.d(TAG, "广告SDK初始化完成")
    }
}
