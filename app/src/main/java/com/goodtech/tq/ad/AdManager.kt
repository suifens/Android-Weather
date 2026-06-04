package com.goodtech.tq.ad

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTAppDownloadListener
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.bytedance.sdk.openadsdk.TTNativeAd
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.bytedance.sdk.openadsdk.mediation.ad.MediationExpressRenderListener
import com.goodtech.tq.base.callback.DataCallback
import com.goodtech.tq.base.removeFromParent

class AdManager {
    companion object {
        private const val TAG = "AdManager"
        
        @Volatile
        private var instance: AdManager? = null
        
        fun getInstance(): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager().also { instance = it }
            }
        }
    }

    fun loadFeedAd(activity: android.app.Activity, codeId: String, width: Int, callback: DataCallback<TTFeedAd>) {
        com.goodtech.tq.app.App.ensureAdSdkInitialized {
            loadFeedAdInternal(activity, codeId, width, callback)
        }
    }

    private fun loadFeedAdInternal(activity: android.app.Activity, codeId: String, width: Int, callback: DataCallback<TTFeedAd>) {
        val adSlot = AdSlot.Builder()
            .setCodeId(codeId)
            .setExpressViewAcceptedSize(width.toFloat(), 0F)
            .setAdCount(1)
            .build()

        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)
        val adListener = createFeedAdListener(callback)
        adNativeLoader.loadFeedAd(adSlot, adListener)
    }

    fun showFeedAd(activity: android.app.Activity, feedContainer: FrameLayout, feedAd: TTFeedAd) {
        if (feedAd == null) {
            Log.i(TAG, "请先加载广告或等待广告加载完毕后再调用show方法")
            return
        }

        feedAd.setDislikeCallback(activity, createDislikeCallback(feedContainer))
        
        val manager = feedAd.mediationManager
        if (manager != null) {
            if (manager.isExpress) {
                showExpressFeedAd(feedContainer, feedAd)
            } else {
                showNativeFeedAd(activity, feedContainer, feedAd)
            }
        }
    }

    private fun showExpressFeedAd(feedContainer: FrameLayout, feedAd: TTFeedAd) {
        feedAd.setExpressRenderListener(object : MediationExpressRenderListener {
            override fun onRenderFail(view: View?, s: String?, i: Int) {
                Log.d(TAG, "feed express render fail, errCode: $i, errMsg: $s")
            }

            override fun onAdClick() {
                Log.d(TAG, "feed express click")
            }

            override fun onAdShow() {
                Log.d(TAG, "feed express show")
            }

            override fun onRenderSuccess(view: View?, v: Float, v1: Float, b: Boolean) {
                Log.d(TAG, "feed express render success")
                val expressFeedView = feedAd.adView
                removeFromParent(expressFeedView)
                feedContainer.removeAllViews()
                feedContainer.addView(expressFeedView)
            }
        })
        feedAd.render()
    }

    private fun showNativeFeedAd(activity: android.app.Activity, feedContainer: FrameLayout, feedAd: TTFeedAd) {
        val feedView = FeedAdUtils.getFeedAdFromFeedInfo(feedAd, activity, null, object : TTNativeAd.AdInteractionListener {
            override fun onAdClicked(view: View?, ttNativeAd: TTNativeAd?) {
                Log.d(TAG, "feed click")
            }

            override fun onAdCreativeClick(view: View?, ttNativeAd: TTNativeAd?) {
                Log.d(TAG, "feed creative click")
            }

            override fun onAdShow(ttNativeAd: TTNativeAd?) {
                Log.d(TAG, "feed show")
            }
        })

        feedView?.let {
            removeFromParent(it)
            feedContainer.removeAllViews()
            feedContainer.addView(it)
        }
    }

    private fun createDislikeCallback(feedContainer: FrameLayout) = object : TTAdDislike.DislikeInteractionCallback {
        override fun onShow() {}
        
        override fun onSelected(i: Int, s: String?, b: Boolean) {
            feedContainer.removeAllViews()
        }
        
        override fun onCancel() {}
    }

    private fun createFeedAdListener(callback: DataCallback<TTFeedAd>) = object : TTAdNative.FeedAdListener {
        override fun onError(i: Int, s: String?) {
            Log.d(TAG, "feed load fail, errCode: $i, errMsg: $s")
        }

        override fun onFeedAdLoad(list: List<TTFeedAd>?) {
            if (!list.isNullOrEmpty()) {
                Log.d(TAG, "feed load success")
                callback.onComplete(list[0], "")
            } else {
                Log.d(TAG, "feed load success, but list is null")
            }
        }
    }

    fun loadExpressAd(activity: android.app.Activity, codeId: String, width: Int, height: Int, callback: DataCallback<TTNativeExpressAd>) {
        com.goodtech.tq.app.App.ensureAdSdkInitialized {
            loadExpressAdInternal(activity, codeId, width, height, callback)
        }
    }

    private fun loadExpressAdInternal(activity: android.app.Activity, codeId: String, width: Int, height: Int, callback: DataCallback<TTNativeExpressAd>) {
        val adSlot = AdSlot.Builder()
            .setCodeId(codeId)
            .setAdCount(1)
            .setExpressViewAcceptedSize(width.toFloat(), 0F)
            .build()

        val ttAdNative = TTAdManagerHolder.get().createAdNative(activity)
        ttAdNative.loadBannerExpressAd(adSlot, object : TTAdNative.NativeExpressAdListener {
            override fun onError(code: Int, message: String) {
                callback.onComplete(null, "load error : $code, $message")
            }

            override fun onNativeExpressAdLoad(ads: List<TTNativeExpressAd>) {
                if (ads.isNullOrEmpty()) {
                    callback.onComplete(null, "ads is empty")
                    return
                }

                Log.d(TAG, "BannerExpressAd load success")
                val ad = ads[0]
                ad.setSlideIntervalTime(30 * 1000)
//                bindAdListener(ad, activity)
                callback.onComplete(ad, null)
            }
        })
    }

    private fun bindAdListener(ad: TTNativeExpressAd, activity: android.app.Activity) {
        ad.setExpressInteractionListener(object : TTNativeExpressAd.ExpressAdInteractionListener {
            override fun onAdClicked(view: View, type: Int) {
                Log.d("AdManager", "广告被点击")
            }

            override fun onAdShow(view: View, type: Int) {
                Log.d("AdManager", "广告展示")
            }

            override fun onRenderFail(view: View, msg: String, code: Int) {
                Log.e("AdManager", "render fail: $msg code:$code")
            }

            override fun onRenderSuccess(view: View, width: Float, height: Float) {
                Log.d("AdManager", "渲染成功")
            }
        })

        // 设置dislike
        ad.setDislikeCallback(activity, object : TTAdDislike.DislikeInteractionCallback {
            override fun onShow() {
                Log.d("AdManager", "dislike show")
            }

            override fun onSelected(position: Int, value: String, enforce: Boolean) {
                Log.d("AdManager", "dislike selected: $value")
                if (enforce) {
                    Log.d("AdManager", "广告被强制关闭")
                }
            }

            override fun onCancel() {
                Log.d("AdManager", "dislike cancel")
            }
        })

        // 设置下载监听
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return
        }
        ad.setDownloadListener(object : TTAppDownloadListener {
            override fun onIdle() {
                Log.d("AdManager", "点击开始下载")
            }

            override fun onDownloadActive(totalBytes: Long, currBytes: Long, fileName: String, appName: String) {
                Log.d("AdManager", "下载中，点击暂停")
            }

            override fun onDownloadPaused(totalBytes: Long, currBytes: Long, fileName: String, appName: String) {
                Log.d("AdManager", "下载暂停，点击继续")
            }

            override fun onDownloadFailed(totalBytes: Long, currBytes: Long, fileName: String, appName: String) {
                Log.d("AdManager", "下载失败，点击重新下载")
            }

            override fun onInstalled(fileName: String, appName: String) {
                Log.d("AdManager", "安装完成，点击图片打开")
            }

            override fun onDownloadFinished(totalBytes: Long, fileName: String, appName: String) {
                Log.d("AdManager", "点击安装")
            }
        })
    }
} 