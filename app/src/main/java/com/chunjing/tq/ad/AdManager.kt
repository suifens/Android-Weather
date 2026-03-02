package com.chunjing.tq.ad

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.bytedance.sdk.openadsdk.TTSplashAd
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot
import com.chunjing.tq.BuildConfig

object AdManager {

    private const val TAG = "AdManager"
    private const val AD_TIME_OUT = 3500

    interface AdCallback<T> {
        fun onSuccess(ad: T)
        fun onFail(code: Int, msg: String)
    }

    /**
     * 加载开屏广告
     */
    fun loadSplashAd(
        activity: Activity,
        container: ViewGroup,
        callback: AdCallback<TTSplashAd>
    ) {
        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)
        val adSlot = AdSlot.Builder()
            .setCodeId(BuildConfig.PGE_SPLASH_POS_ID)
            .setExpressViewAcceptedSize(
                activity.resources.displayMetrics.widthPixels.toFloat(),
                activity.resources.displayMetrics.heightPixels.toFloat()
            )
            .build()

        adNativeLoader.loadSplashAd(adSlot, object : TTAdNative.CSJSplashAdListener {
            override fun onSplashLoadSuccess(ad: TTSplashAd?) {
                Log.d(TAG, "开屏广告加载成功")
            }

            override fun onSplashLoadFail(ad: TTSplashAd?, code: Int, msg: String?) {
                Log.e(TAG, "开屏广告加载失败: $code, $msg")
                callback.onFail(code, msg ?: "加载失败")
            }

            override fun onSplashRenderSuccess(ad: TTSplashAd?) {
                Log.d(TAG, "开屏广告渲染成功")
                ad?.let {
                    callback.onSuccess(it)
                    showSplashAd(activity, container, it)
                }
            }

            override fun onSplashRenderFail(ad: TTSplashAd?, code: Int, msg: String?) {
                Log.e(TAG, "开屏广告渲染失败: $code, $msg")
                callback.onFail(code, msg ?: "渲染失败")
            }
        }, AD_TIME_OUT)
    }

    private fun showSplashAd(activity: Activity, container: ViewGroup, ad: TTSplashAd) {
        ad.setSplashAdListener(object : TTSplashAd.SplashAdListener {
            override fun onSplashAdShow(ad: TTSplashAd?) {
                Log.d(TAG, "开屏广告展示")
            }

            override fun onSplashAdClick(ad: TTSplashAd?) {
                Log.d(TAG, "开屏广告点击")
            }

            override fun onSplashAdClose(ad: TTSplashAd?, closeType: Int) {
                Log.d(TAG, "开屏广告关闭: $closeType")
                container.removeAllViews()
            }
        })

        val splashView = ad.splashView
        if (splashView != null) {
            container.removeAllViews()
            container.addView(splashView)
        }
    }

    /**
     * 加载 Banner 广告
     */
    fun loadBannerAd(
        activity: Activity,
        width: Int,
        height: Int = 0,
        callback: AdCallback<TTNativeExpressAd>
    ) {
        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)
        val adSlot = AdSlot.Builder()
            .setCodeId(BuildConfig.PGE_BANNER_POS_ID)
            .setAdCount(1)
            .setExpressViewAcceptedSize(width.toFloat(), height.toFloat())
            .build()

        adNativeLoader.loadBannerExpressAd(adSlot, object : TTAdNative.NativeExpressAdListener {
            override fun onNativeExpressAdLoad(ads: MutableList<TTNativeExpressAd>?) {
                if (ads.isNullOrEmpty()) {
                    callback.onFail(-1, "广告列表为空")
                    return
                }
                val ad = ads[0]
                ad.setSlideIntervalTime(30 * 1000)
                callback.onSuccess(ad)
            }

            override fun onError(code: Int, msg: String?) {
                Log.e(TAG, "Banner广告加载失败: $code, $msg")
                callback.onFail(code, msg ?: "加载失败")
            }
        })
    }

    /**
     * 展示 Banner 广告
     */
    fun showBannerAd(container: ViewGroup, ad: TTNativeExpressAd) {
        ad.setExpressInteractionListener(object : TTNativeExpressAd.ExpressAdInteractionListener {
            override fun onAdClicked(view: android.view.View?, type: Int) {
                Log.d(TAG, "Banner广告点击")
            }

            override fun onAdShow(view: android.view.View?, type: Int) {
                Log.d(TAG, "Banner广告展示")
            }

            override fun onRenderFail(view: android.view.View?, msg: String?, code: Int) {
                Log.e(TAG, "Banner广告渲染失败: $code, $msg")
            }

            override fun onRenderSuccess(view: android.view.View?, width: Float, height: Float) {
                Log.d(TAG, "Banner广告渲染成功: ${width}x$height")
                container.removeAllViews()
                container.addView(view)
            }
        })
        ad.render()
    }

    /**
     * 加载信息流广告
     */
    fun loadFeedAd(
        activity: Activity,
        width: Int,
        callback: AdCallback<TTNativeExpressAd>
    ) {
        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)
        val adSlot = AdSlot.Builder()
            .setCodeId(BuildConfig.PGE_FEED_POS_ID)
            .setAdCount(1)
            .setExpressViewAcceptedSize(width.toFloat(), 0F)
            .build()

        adNativeLoader.loadNativeExpressAd(adSlot, object : TTAdNative.NativeExpressAdListener {
            override fun onNativeExpressAdLoad(ads: MutableList<TTNativeExpressAd>?) {
                if (ads.isNullOrEmpty()) {
                    callback.onFail(-1, "广告列表为空")
                    return
                }
                callback.onSuccess(ads[0])
            }

            override fun onError(code: Int, msg: String?) {
                Log.e(TAG, "信息流广告加载失败: $code, $msg")
                callback.onFail(code, msg ?: "加载失败")
            }
        })
    }

    /**
     * 展示信息流广告
     */
    fun showFeedAd(container: ViewGroup, ad: TTNativeExpressAd) {
        ad.setExpressInteractionListener(object : TTNativeExpressAd.ExpressAdInteractionListener {
            override fun onAdClicked(view: android.view.View?, type: Int) {
                Log.d(TAG, "信息流广告点击")
            }

            override fun onAdShow(view: android.view.View?, type: Int) {
                Log.d(TAG, "信息流广告展示")
            }

            override fun onRenderFail(view: android.view.View?, msg: String?, code: Int) {
                Log.e(TAG, "信息流广告渲染失败: $code, $msg")
            }

            override fun onRenderSuccess(view: android.view.View?, width: Float, height: Float) {
                Log.d(TAG, "信息流广告渲染成功: ${width}x$height")
                container.removeAllViews()
                container.addView(view)
            }
        })
        ad.render()
    }
}
