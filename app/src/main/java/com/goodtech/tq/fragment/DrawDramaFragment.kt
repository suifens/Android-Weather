package com.goodtech.tq.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.bytedance.sdk.djx.DJXRewardAdResult
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.djx.IDJXWidget
import com.bytedance.sdk.djx.interfaces.listener.IDJXDramaUnlockListener
import com.bytedance.sdk.djx.interfaces.listener.IDJXDrawListener
import com.bytedance.sdk.djx.model.DJXDrama
import com.bytedance.sdk.djx.model.DJXDramaDetailConfig
import com.bytedance.sdk.djx.model.DJXDramaUnlockAdMode
import com.bytedance.sdk.djx.model.DJXDramaUnlockInfo
import com.bytedance.sdk.djx.model.DJXDramaUnlockMethod
import com.bytedance.sdk.djx.params.DJXWidgetDramaDetailParams
import com.bytedance.sdk.djx.params.DJXWidgetDrawParams
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdLoadType
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTRewardVideoAd
import com.goodtech.tq.R
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.IBusListener
import com.goodtech.tq.common.bus.event.DJXStartEvent
import com.goodtech.tq.modules.video.djx.DefaultAdListener
import com.goodtech.tq.modules.video.djx.DefaultDramaListener
import com.goodtech.tq.modules.video.djx.DefaultDrawListener
import com.goodtech.tq.utils.TipHelper
import androidx.core.graphics.toColorInt

class DrawDramaFragment : BaseFragment() {

    companion object {
        private const val TAG = "DrawDramaFragment"
        private const val LOCK_SET = 2
    }

    private var isRewardArrived = false
    private var dpWidget: IDJXWidget? = null
    private var isInited = false
    
    private var channelType = DJXWidgetDrawParams.DRAW_CHANNEL_TYPE_RECOMMEND
    private var contentType = DJXWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_DRAMA
    private var isCustomDetail = false
    private var isHideLike = false
    private var isHideFavor = false
    private var isHideInfo = false
    private var isHideEnter = false
    private var isInsertCustomView = false
    private var dramaFree = 3
    private var dramaDetailFree = 3
    private var dramaTopId = -1

    private val function = IBusListener { event ->
        if (event is DJXStartEvent) {
            if (event.isSuccess) {
                init()
            }
        }
    }

    override fun getViewLayoutRes(): Int {
        return R.layout.fragment_draw_drama
    }

    override fun setupCacheViews() {
        super.setupCacheViews()
        Bus.getInstance().addListener(function)
        if (DJXSdk.isStartSuccess()) {
            init()
        }
    }

    private fun init() {
        if (isInited) return
        
        initDrawWidget()
        dpWidget?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.draw_drama_frame, it.fragment)
                .commitAllowingStateLoss()
            isInited = true
        }
    }

    private fun initDrawWidget() {
        val drawListener = object : IDJXDrawListener() {
            override fun createCustomView(container: ViewGroup?, map: MutableMap<String, Any>?): View? {
                return if (isInsertCustomView) {
                    container ?: return null
                    map ?: return null

                    Log.d(TAG, "createCustomView: map=$map")
                    val label = TextView(container.context)
                    label.text = map["title"]?.toString() ?: ""
                    label.setTextColor("#f1f1f1".toColorInt())
                    label.textSize = 20f

                    val labelParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                    labelParams.topMargin = 300
                    labelParams.gravity = Gravity.CENTER_HORIZONTAL
                    label.layoutParams = labelParams

                    val customView = FrameLayout(container.context)
                    customView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    customView.addView(label)

                    return customView
                } else {
                    super.createCustomView(container, map)
                }
            }
        }

        val dramaDetailConfig =
            DJXDramaDetailConfig.obtain(DJXDramaUnlockAdMode.MODE_SPECIFIC, dramaDetailFree, object : IDJXDramaUnlockListener {
                override fun unlockFlowStart(drama: DJXDrama, callback: IDJXDramaUnlockListener.UnlockCallback, map: Map<String, Any>?) {
                    val info = DJXDramaUnlockInfo(drama.id, LOCK_SET, DJXDramaUnlockMethod.METHOD_AD, false)
                    callback.onConfirm(info)
                }

                override fun unlockFlowEnd(drama: DJXDrama, errCode: IDJXDramaUnlockListener.UnlockErrorStatus?, map: Map<String, Any>?) {
                }

                override fun showCustomAd(drama: DJXDrama, callback: IDJXDramaUnlockListener.CustomAdCallback) {
                    TipHelper.showProgressDialog(requireActivity())
                    val adSlot = AdSlot.Builder()
                        .setCodeId("102948965")
                        .setAdLoadType(TTAdLoadType.LOAD)
                        .build()

                    TTAdSdk.getAdManager().createAdNative(requireActivity()).loadRewardVideoAd(adSlot, object : TTAdNative.RewardVideoAdListener {
                        override fun onError(p0: Int, p1: String?) {
                            TipHelper.dismissProgressDialog()
                        }

                        override fun onRewardVideoAdLoad(ad: TTRewardVideoAd?) {
                            ad?.apply {
                                setRewardAdInteractionListener(object :
                                    TTRewardVideoAd.RewardAdInteractionListener {
                                    override fun onAdShow() {
                                        callback.onShow("")
                                        TipHelper.dismissProgressDialog()
                                    }

                                    override fun onAdVideoBarClick() {}

                                    override fun onAdClose() {}

                                    override fun onVideoComplete() {}

                                    override fun onVideoError() {
                                        callback.onRewardVerify(DJXRewardAdResult(false))
                                        TipHelper.dismissProgressDialog()
                                    }

                                    override fun onRewardVerify(
                                        rewardVerify: Boolean,
                                        rewardAmount: Int,
                                        rewardName: String,
                                        errorCode: Int,
                                        errorMsg: String
                                    ) {}

                                    override fun onRewardArrived(isRewardValid: Boolean, rewardType: Int, extraInfo: Bundle) {
                                        val result = DJXRewardAdResult(isRewardValid)
                                        isRewardArrived = isRewardValid
                                        callback.onRewardVerify(result)
                                        TipHelper.dismissProgressDialog()
                                    }

                                    override fun onSkippedVideo() {
                                        if (!isRewardArrived) {
                                            callback.onRewardVerify(DJXRewardAdResult(false))
                                        }
                                        TipHelper.dismissProgressDialog()
                                    }
                                })
                                showRewardVideoAd(requireActivity())
                            }
                        }

                        override fun onRewardVideoCached() {}
                        override fun onRewardVideoCached(p0: TTRewardVideoAd?) {}
                    })
                }
            }).apply {
                listener(DefaultDramaListener(null))
                adListener(DefaultAdListener(null))
            }

        dpWidget = DJXSdk.factory().createDraw(
            DJXWidgetDrawParams.obtain().apply {
                adOffset(0)
                drawContentType(contentType)
                drawChannelType(channelType)
                hideChannelName(channelType == DJXWidgetDrawParams.DRAW_CHANNEL_TYPE_RECOMMEND)
                hideLikeButton(isHideLike)
                hideFavorButton(isHideFavor)
                hideDramaInfo(isHideInfo)
                hideDramaEnter(isHideEnter)
                dramaFree(dramaFree)
                LOCK_SET
                topDramaId(dramaTopId.toLong())
                hideClose(false, null)
                listener(DefaultDrawListener(drawListener))
                adListener(DefaultAdListener(null))
                detailConfig(dramaDetailConfig)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        dpWidget?.fragment?.onResume()
    }

    override fun onPause() {
        super.onPause()
        dpWidget?.fragment?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        dpWidget?.destroy()
        Bus.getInstance().removeListener(function)
    }

    fun setConfig(
        channelType: Int = DJXWidgetDrawParams.DRAW_CHANNEL_TYPE_RECOMMEND,
        contentType: Int = DJXWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_DRAMA,
        isCustomDetail: Boolean = false,
        isHideLike: Boolean = false,
        isHideFavor: Boolean = false,
        isHideInfo: Boolean = false,
        isHideEnter: Boolean = false,
        isInsertCustomView: Boolean = false,
        dramaFree: Int = 3,
        dramaDetailFree: Int = 3,
        dramaTopId: Int = -1
    ) {
        this.channelType = channelType
        this.contentType = contentType
        this.isCustomDetail = isCustomDetail
        this.isHideLike = isHideLike
        this.isHideFavor = isHideFavor
        this.isHideInfo = isHideInfo
        this.isHideEnter = isHideEnter
        this.isInsertCustomView = isInsertCustomView
        this.dramaFree = dramaFree
        this.dramaDetailFree = dramaDetailFree
        this.dramaTopId = dramaTopId
    }
} 