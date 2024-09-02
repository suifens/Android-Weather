package com.goodtech.tq.modules.video.djx

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.sdk.djx.DJXRewardAdResult
import com.goodtech.tq.R
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.IBusListener
import com.goodtech.tq.common.bus.event.DJXStartEvent
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
import com.goodtech.tq.utils.TipHelper

/**
 * Created by limingqi on 2023/5/10
 */
class DrawDramaActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DrawDramaActivity"
        private const val CHANNEL_TYPE = "channel_type"
        private const val CONTENT_TYPE = "content_type"
        private const val IS_CUSTOM_DETAIL = "is_custom_detail"
        private const val HIDE_LIKE = "HIDE_LIKE"
        private const val HIDE_FAVOR = "HIDE_FAVOR"
        private const val HIDE_INFO = "hide_info"
        private const val HIDE_ENTER = "hide_enter"
        private const val DRAMA_FREE = "drama_free"
        private const val DRAMA_DETAIL_FREE = "drama_detail_free"
        private const val DRAMA_TOP_ID = "drama_top_id"
        private const val INSERT_CUSTOM_VIEW = "insert_custom_view"

        private const val LOCK_SET = 2

        fun start(
            activity: Activity,
            channelType: Int,
            contentType: Int,
            isCustomDetail: Boolean,
            hideLike: Boolean,
            hideFavor: Boolean,
            hideInfo: Boolean,
            hideEnter: Boolean,
            insertCustomView: Boolean,
            dramaFree: Int,
            dramaDetailFree: Int,
            dramaTopId: Int
        ) {
            val intent = Intent(activity, DrawDramaActivity::class.java)
            intent.putExtra(HIDE_LIKE, hideLike)
            intent.putExtra(HIDE_FAVOR, hideFavor)
            intent.putExtra(CHANNEL_TYPE, channelType)
            intent.putExtra(CONTENT_TYPE, contentType)
            intent.putExtra(IS_CUSTOM_DETAIL, isCustomDetail)
            intent.putExtra(HIDE_INFO, hideInfo)
            intent.putExtra(HIDE_ENTER, hideEnter)
            intent.putExtra(INSERT_CUSTOM_VIEW, insertCustomView)
            intent.putExtra(DRAMA_FREE, dramaFree)
            intent.putExtra(DRAMA_DETAIL_FREE, dramaDetailFree)
            intent.putExtra(DRAMA_TOP_ID, dramaTopId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }

        @JvmStatic
        fun start(activity: Activity) {
            val intent = Intent(activity, DrawDramaActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }
    }

    private var isRewardArrived = false
    private var dpWidget: IDJXWidget? = null

    private var lastBackTime: Long = -1
    private var channelType = DJXWidgetDrawParams.DRAW_CHANNEL_TYPE_RECOMMEND
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

    private var isInited = false
    private val function = IBusListener { event ->
        if (event is DJXStartEvent) {
            if (event.isSuccess) {
                init()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.draw_drama_activity)
        intent?.let { intent ->
            channelType = intent.getIntExtra(CHANNEL_TYPE, DJXWidgetDrawParams.DRAW_CHANNEL_TYPE_THEATER)
            contentType = intent.getIntExtra(CONTENT_TYPE, DJXWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_DRAMA)
            isCustomDetail = intent.getBooleanExtra(IS_CUSTOM_DETAIL, false)
            isHideLike = intent.getBooleanExtra(HIDE_LIKE, false)
            isHideFavor = intent.getBooleanExtra(HIDE_FAVOR, false)
            isHideInfo = intent.getBooleanExtra(HIDE_INFO, false)
            isHideEnter = intent.getBooleanExtra(HIDE_ENTER, false)
            isInsertCustomView = intent.getBooleanExtra(INSERT_CUSTOM_VIEW, false)
            dramaFree = intent.getIntExtra(DRAMA_FREE, 3)
            dramaDetailFree = intent.getIntExtra(DRAMA_DETAIL_FREE, 3)
            dramaTopId = intent.getIntExtra(DRAMA_TOP_ID, -1)
        }
        Bus.getInstance().addListener(function)
        if (DJXSdk.isStartSuccess()) {
            init()
        }
    }

    private fun init() {
        if (isInited) {
            return
        }
        //初始化draw组件
        initDrawWidget()
        dpWidget?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.draw_drama_frame, it.fragment)
                .commitAllowingStateLoss()
            isInited = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("android:support:fragments", null)
    }

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
                    label.setTextColor(Color.parseColor("#f1f1f1"))
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
                    TipHelper.showProgressDialog(this@DrawDramaActivity)
                    val adSlot = AdSlot.Builder()
                        .setCodeId("102948965") // 广告代码位Id
                        .setAdLoadType(TTAdLoadType.LOAD) // 本次广告用途：TTAdLoadType.LOAD实时；TTAdLoadType.PRELOAD预请求
                        .build()

                    TTAdSdk.getAdManager().createAdNative(this@DrawDramaActivity).loadRewardVideoAd(adSlot, object : TTAdNative.RewardVideoAdListener {
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
                                showRewardVideoAd(this@DrawDramaActivity)
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
                hideClose(false, null)
                listener(DefaultDrawListener(drawListener))
                adListener(DefaultAdListener(null))
//                if (isCustomDetail) {
//                    setEnterDelegate { context, drama, current ->
//                        DramaDetailActivity.outerDrama = drama
//                        DramaDetailActivity.enterFrom = enterFrom
//                        val intent = Intent(context, DramaDetailActivity::class.java)
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        intent.putExtra(DramaDetailConfigActivity.KEY_DRAMA_PLAY_DURATION, current)
//                        context.startActivity(intent)
//                    }
//                } else {
                    detailConfig(dramaDetailConfig)
//                }
            }
        )
    }

    override fun onBackPressed() {
        if (dpWidget != null && !dpWidget!!.canBackPress()) {
            return
        }
        if (dpWidget == null) {
            return
        }
        val current = SystemClock.elapsedRealtime()
        if (current - lastBackTime > 3000) {
            lastBackTime = current
            dpWidget?.backRefresh()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        dpWidget?.destroy()
        Bus.getInstance().removeListener(function)
    }

}