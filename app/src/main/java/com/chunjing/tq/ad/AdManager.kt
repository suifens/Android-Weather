package com.chunjing.tq.ad

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.CSJAdError
import com.bytedance.sdk.openadsdk.CSJSplashAd
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
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
     * @param onAdClose 广告关闭时的回调，可为 null
     */
    fun loadSplashAd(
        activity: Activity,
        container: ViewGroup,
        callback: AdCallback<CSJSplashAd>,
        onAdClose: (() -> Unit)? = null
    ) {
        val width = activity.resources.displayMetrics.widthPixels
        val height = activity.resources.displayMetrics.heightPixels
        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)
        val adSlot = AdSlot.Builder()
            .setCodeId(BuildConfig.PGE_SPLASH_POS_ID)
            .setExpressViewAcceptedSize(width.toFloat(), height.toFloat())
            .build()

        adNativeLoader.loadSplashAd(adSlot, object : TTAdNative.CSJSplashAdListener {
            override fun onSplashLoadSuccess(ad: CSJSplashAd) {
                Log.d(TAG, "开屏广告加载成功")
            }

            override fun onSplashLoadFail(csjAdError: CSJAdError) {
                Log.e(TAG, "开屏广告加载失败: ${csjAdError.code}, ${csjAdError.msg}")
                callback.onFail(csjAdError.code, csjAdError.msg ?: "加载失败")
            }

            override fun onSplashRenderSuccess(ad: CSJSplashAd) {
                Log.d(TAG, "开屏广告渲染成功")
                ad.setSplashAdListener(object : CSJSplashAd.SplashAdListener {
                    override fun onSplashAdShow(csjSplashAd: CSJSplashAd) {
                        Log.d(TAG, "开屏广告展示")
                    }

                    override fun onSplashAdClick(csjSplashAd: CSJSplashAd) {
                        Log.d(TAG, "开屏广告点击")
                    }

                    override fun onSplashAdClose(csjSplashAd: CSJSplashAd, closeType: Int) {
                        Log.d(TAG, "开屏广告关闭: $closeType")
                        container.removeAllViews()
                        onAdClose?.invoke()
                    }
                })
                val splashView: View? = ad.splashView
                if (splashView != null) {
                    container.removeAllViews()
                    container.addView(splashView)
                }
                callback.onSuccess(ad)
            }

            override fun onSplashRenderFail(ad: CSJSplashAd, csjAdError: CSJAdError) {
                Log.e(TAG, "开屏广告渲染失败: ${csjAdError.code}, ${csjAdError.msg}")
                callback.onFail(csjAdError.code, csjAdError.msg ?: "渲染失败")
            }
        }, AD_TIME_OUT)
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
