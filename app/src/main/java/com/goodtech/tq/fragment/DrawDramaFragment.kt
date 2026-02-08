package com.goodtech.tq.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
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
import com.bytedance.sdk.djx.utils.StatusBarUtil
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdLoadType
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTRewardVideoAd
import com.goodtech.tq.R
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.BusEvent
import com.goodtech.tq.common.bus.event.DJXStartEvent
import com.goodtech.tq.databinding.FragmentDrawDramaBinding
import com.goodtech.tq.modules.video.djx.DefaultAdListener
import com.goodtech.tq.modules.video.djx.DefaultDramaListener
import com.goodtech.tq.modules.video.djx.DefaultDrawListener
import com.goodtech.tq.utils.AdRemovalManager
import com.goodtech.tq.utils.TipHelper

class DrawDramaFragment : BaseFragment() {

    private val TAG = "DrawDramaFragment"
    private var djxWidget: IDJXWidget? = null
    private var _binding: FragmentDrawDramaBinding? = null
    private val binding get() = _binding!!

    private val listener: (BusEvent) -> Unit = {
        if (it is DJXStartEvent) {
            if (it.isSuccess) {
                init()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawDramaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Bus.getInstance().addListener(listener)
        if (DJXSdk.isStartSuccess()) {
            init()
        }
    }

    private fun init() {
//        djxWidget = DJXHolder.loadDramaDraw(null, null, null)
        initDrawWidget()
        djxWidget?.fragment?.userVisibleHint = userVisibleHint

        djxWidget?.let {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.draw_drama_frame, it.fragment)
                this.commitNowAllowingStateLoss()
            }
        }
        val params = binding.drawDramaFrame.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = StatusBarUtil.getStatusBarHeight(requireActivity())
        binding.drawDramaFrame.layoutParams = params
    }

    private var lastBackTime: Long = -1
    private var channelType = DJXWidgetDrawParams.DRAW_CHANNEL_TYPE_THEATER
    private var contentType = DJXWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_DRAMA
    private var isCustomDetail = false
    private var isHideLike = false
    private var isHideFavor = false
    private var isHideInfo = false
    private var isHideEnter = false
    private var isInsertCustomView = false
    private var dramaFree = 3 // 短剧混排免费集数，默认1
    private var dramaDetailFree = 3 // 短剧详情免费集数，默认-1，会使用sdk的默认设置
    private var dramaTopId = -1 // 短剧置顶，默认-1，会使用sdk的默认设置

    private val LOCK_SET = 2
    private var isRewardArrived = false

    private fun initDrawWidget() {
        val enterFrom = DJXWidgetDramaDetailParams.DJXDramaEnterFrom.SKIT_MIXED

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
                    // 解锁支持多种方式：支付、广告。根据业务需求自行定义
                    // 如果在其他时机已经购买会员可以设置 unlockInfo 中的 hasMember 为 true
                    // val info = DJXDramaUnlockInfo(drama.id, lockSet, DJXDramaUnlockMethod.METHOD_PAY_MEMBER, true)
                    // callback.onConfirm(info)
                    val info = DJXDramaUnlockInfo(drama.id, LOCK_SET, DJXDramaUnlockMethod.METHOD_AD, false)
                    callback.onConfirm(info)
                }

                override fun unlockFlowEnd(drama: DJXDrama, errCode: IDJXDramaUnlockListener.UnlockErrorStatus?, map: Map<String, Any>?) {
                }

                override fun showCustomAd(drama: DJXDrama, callback: IDJXDramaUnlockListener.CustomAdCallback) {
                    TipHelper.showProgressDialog(requireActivity())
                    val adSlot = AdSlot.Builder()
                        .setCodeId("102948965") // 广告代码位Id
                        .setAdLoadType(TTAdLoadType.LOAD) // 本次广告用途：TTAdLoadType.LOAD实时；TTAdLoadType.PRELOAD预请求
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
//                                        Toast.makeText(this@DrawDramaActivity, "自定义广告展示", Toast.LENGTH_LONG).show()
                                        callback.onShow("") // CSJ cpm 不对外，可以参考 GroMore getShowEcpm 方法获取
                                        TipHelper.dismissProgressDialog()
                                    }

                                    override fun onAdVideoBarClick() {
                                        // 广告点击
                                    }

                                    override fun onAdClose() {
                                        // 广告关闭
                                    }

                                    override fun onVideoComplete() {
                                        // 广告素材播放完成，例如视频未跳过，完整的播放了
                                    }

                                    override fun onVideoError() {
                                        // 广告展示时出错
                                        callback.onRewardVerify(DJXRewardAdResult(false))
                                        TipHelper.dismissProgressDialog()
                                    }

                                    override fun onRewardVerify(
                                        rewardVerify: Boolean,
                                        rewardAmount: Int,
                                        rewardName: String,
                                        errorCode: Int,
                                        errorMsg: String
                                    ) {
                                        // 已废弃 请使用 onRewardArrived 替代
                                    }

                                    override fun onRewardArrived(isRewardValid: Boolean, rewardType: Int, extraInfo: Bundle) {
                                        val result = DJXRewardAdResult(isRewardValid)
                                        isRewardArrived = isRewardValid
                                        
                                        // 如果视频完整播放且有效，检查并领取视频任务奖励
                                        if (isRewardValid) {
                                            // 检查是否有可领取的视频任务
                                            val currentTask = AdRemovalManager.getCurrentAvailableVideoTask()
                                            if (currentTask != null) {
                                                // 自动领取视频任务奖励（内部会自动更新连续观看天数）
                                                val rewardResult = AdRemovalManager.claimVideoTaskReward()
                                                if (rewardResult != null && rewardResult.first) {
                                                    val (_, rewardHours) = rewardResult
                                                    Log.d(TAG, "视频任务奖励已领取: rewardHours=$rewardHours")
                                                }
                                            }
                                        }
                                        
                                        callback.onRewardVerify(result)
                                        TipHelper.dismissProgressDialog()
                                    }

                                    override fun onSkippedVideo() {
                                        // 用户在观看时点击了跳过
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

        djxWidget = DJXSdk.factory().createDraw(
            DJXWidgetDrawParams.obtain().apply {
                adOffset(0) //单位 dp，为 0 时可以不设置
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
                hideClose(true, null)
                listener(DefaultDrawListener(drawListener))
                adListener(DefaultAdListener(null))
                detailConfig(dramaDetailConfig)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        djxWidget?.fragment?.onResume()
    }

    override fun onPause() {
        super.onPause()
        djxWidget?.fragment?.onPause()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        djxWidget?.fragment?.onHiddenChanged(hidden)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        djxWidget?.fragment?.userVisibleHint = isVisibleToUser
    }

    override fun onDestroy() {
        super.onDestroy()
        Bus.getInstance().removeListener(listener)
    }
}