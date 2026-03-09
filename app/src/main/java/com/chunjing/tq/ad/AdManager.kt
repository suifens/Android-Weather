package com.chunjing.tq.ad

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.CSJAdError
import com.bytedance.sdk.openadsdk.CSJSplashAd
import android.os.Bundle
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.bytedance.sdk.openadsdk.TTRewardVideoAd
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.bytedance.sdk.openadsdk.mediation.ad.MediationExpressRenderListener
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
     * 加载并展示激励视频广告（用于去广告功能）
     * @param codeId 激励视频广告位ID
     * @param onReward 观看完成并获得奖励时回调，参数为是否有效
     * @param onError 加载或展示失败时回调
     */
    fun loadRewardVideoAd(
        activity: Activity,
        codeId: String,
        onReward: (isRewardValid: Boolean) -> Unit,
        onError: (code: Int, msg: String) -> Unit
    ) {
        val adSlot = AdSlot.Builder().setCodeId(codeId).build()
        TTAdSdk.getAdManager().createAdNative(activity).loadRewardVideoAd(adSlot, object : TTAdNative.RewardVideoAdListener {
            override fun onError(code: Int, message: String?) {
                Log.e(TAG, "激励视频加载失败: code=$code, msg=$message")
                onError(code, message ?: "加载失败")
            }

            override fun onRewardVideoAdLoad(ad: TTRewardVideoAd?) {
                ad ?: run { onError(-1, "广告为空"); return }
                ad.setRewardAdInteractionListener(object : TTRewardVideoAd.RewardAdInteractionListener {
                    override fun onAdShow() {}
                    override fun onAdVideoBarClick() {}
                    override fun onAdClose() {}
                    override fun onVideoComplete() {}
                    override fun onVideoError() { onError(-1, "播放出错") }
                    override fun onRewardVerify(rewardVerify: Boolean, rewardAmount: Int, rewardName: String, errorCode: Int, errorMsg: String) {}
                    override fun onRewardArrived(isRewardValid: Boolean, rewardType: Int, extraInfo: Bundle) {
                        onReward(isRewardValid)
                    }
                    override fun onSkippedVideo() {}
                })
                ad.showRewardVideoAd(activity)
            }

            override fun onRewardVideoCached() {}
            override fun onRewardVideoCached(ad: TTRewardVideoAd?) {}
        })
    }

    /**
     * 加载 TTFeedAd 类型信息流广告（使用 loadFeedAd 接口）
     * 支持模板(Express)和原生(Native)两种形式，当前仅展示模板广告
     */
    fun loadTTFeedAd(
        activity: Activity,
        codeId: String = BuildConfig.PGE_FEED_POS_ID,
        width: Int,
        callback: AdCallback<TTFeedAd>
    ) {
        val adSlot = AdSlot.Builder()
            .setCodeId(codeId)
            .setExpressViewAcceptedSize(width.toFloat(), 0F)
            .setAdCount(1)
            .build()

        TTAdSdk.getAdManager().createAdNative(activity).loadFeedAd(adSlot, object : TTAdNative.FeedAdListener {
            override fun onError(code: Int, msg: String?) {
                Log.e(TAG, "TTFeedAd 加载失败: code=$code, msg=$msg")
                callback.onFail(code, msg ?: "加载失败")
            }

            override fun onFeedAdLoad(ads: MutableList<TTFeedAd>?) {
                if (ads.isNullOrEmpty()) {
                    callback.onFail(-1, "广告列表为空")
                    return
                }
                Log.d(TAG, "TTFeedAd 加载成功")
                callback.onSuccess(ads[0])
            }
        })
    }

    /**
     * 展示 TTFeedAd 信息流广告
     * 支持模板(Express)类型；原生(Native)类型需要 FeedAdUtils，当前不展示
     */
    fun showTTFeedAd(activity: Activity, container: ViewGroup, feedAd: TTFeedAd) {
        feedAd.setDislikeCallback(activity, object : TTAdDislike.DislikeInteractionCallback {
            override fun onShow() {}
            override fun onSelected(position: Int, value: String?, enforce: Boolean) {
                container.removeAllViews()
            }
            override fun onCancel() {}
        })

        val manager = feedAd.mediationManager
        if (manager != null && manager.isExpress) {
            feedAd.setExpressRenderListener(object : MediationExpressRenderListener {
                override fun onRenderSuccess(view: View?, w: Float, h: Float, b: Boolean) {
                    Log.d(TAG, "TTFeedAd Express 渲染成功")
                    val expressView = feedAd.adView
                    if (expressView != null) {
                        (expressView.parent as? ViewGroup)?.removeView(expressView)
                        container.removeAllViews()
                        container.addView(expressView)
                    }
                }
                override fun onRenderFail(view: View?, msg: String?, code: Int) {
                    Log.e(TAG, "TTFeedAd Express 渲染失败: code=$code, msg=$msg")
                }
                override fun onAdClick() { Log.d(TAG, "TTFeedAd 点击") }
                override fun onAdShow() { Log.d(TAG, "TTFeedAd 展示") }
            })
            feedAd.render()
        } else {
            Log.w(TAG, "TTFeedAd 为非模板类型，需要 FeedAdUtils 支持，当前不展示")
        }
    }

    /**
     * 展示信息流广告（TTNativeExpressAd 模板广告）
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
