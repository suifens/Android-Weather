package com.goodtech.tq.ad

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.bytedance.sdk.openadsdk.TTNativeAd
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
} 