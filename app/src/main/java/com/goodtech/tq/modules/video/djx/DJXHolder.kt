package com.goodtech.tq.modules.video.djx

import android.app.Application
import android.util.Log
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.djx.DJXSdkConfig
import com.bytedance.sdk.djx.IDJXPrivacyController
import com.bytedance.sdk.djx.IDJXWidget
import com.bytedance.sdk.djx.interfaces.listener.IDJXAdListener
import com.bytedance.sdk.djx.interfaces.listener.IDJXDramaHomeListener
import com.bytedance.sdk.djx.interfaces.listener.IDJXDramaListener
import com.bytedance.sdk.djx.interfaces.listener.IDJXDrawListener
import com.bytedance.sdk.djx.model.DJXDramaDetailConfig
import com.bytedance.sdk.djx.model.DJXDramaUnlockAdMode
import com.bytedance.sdk.djx.params.DJXWidgetDramaHomeParams
import com.bytedance.sdk.djx.params.DJXWidgetDrawParams
import com.goodtech.tq.app.App
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.event.DJXStartEvent

/**
 * create by hanweiwei on 8/30/23
 */
object DJXHolder {

    private const val TAG = "DJXHolder"

    const val FREE_SET = 5
    const val LOCK_SET = 2

    @Volatile
    private var initialized = false

    //初始化：仅在用户主动进入短剧功能时调用
    fun init(application: Application) {
        if (initialized) {
            if (!DJXSdk.isStartSuccess()) {
                start(null)
            }
            return
        }
        synchronized(this) {
            if (initialized) {
                return
            }
            doInitTask(application)
            initialized = true
        }
    }

    private fun doInitTask(application: Application) {
        val config = DJXSdkConfig.Builder()
            .debug(false)
            .newUser(true)
            .build()

        config.privacyController = object : IDJXPrivacyController() {
            // 配置青少年模式，true青少年模式，false正常模式。默认为false，青少年开启后不返回短剧内容
            override fun isTeenagerMode(): Boolean {
                return false
            }
            //  true仅有备案号、false全量内容(默认)
            override fun isOnlyICPNumber(): Boolean {
                return true
            }

            override fun isCanUsePhoneState(): Boolean {
                return false
            }

            override fun isCanUseAndroidId(): Boolean {
                return false
            }

            override fun isCanUseMac(): Boolean {
                return false
            }

            override fun isCanUseOAID(): Boolean {
                return false
            }

            override fun isCanUseICCID(): Boolean {
                return false
            }

            override fun isCanUseSerialNumber(): Boolean {
                return false
            }

            override fun isCanUseGAID(): Boolean {
                return false
            }

            override fun isCanUseOperatorInfo(): Boolean {
                return false
            }
        }

        DJXSdk.init(application, App.SDK_SETTINGS_CONFIG, config)
        start(null)
    }

    fun start(callback: ((success: Boolean) -> Unit)?) {
        DJXSdk.start(DJXSdk.StartListener { isSuccess, message, error ->
            Log.d(TAG, "doInitTask: $isSuccess, $message, $error")
            Bus.getInstance().sendEvent(DJXStartEvent(isSuccess))
            callback?.invoke(isSuccess)
        })
    }

    //加载短剧
    fun loadDramaDraw(dramaListener: IDJXDramaListener?, drawListener: IDJXDrawListener?, adListener: IDJXAdListener?): IDJXWidget {
        val dramaDetailConfig = DJXDramaDetailConfig.obtain(DJXDramaUnlockAdMode.MODE_COMMON, FREE_SET, DefaultDramaUnlockListener(LOCK_SET, null))
            .listener(DefaultDramaListener(dramaListener)) // 短剧详情页视频播放回调
            .adListener(adListener) // 短剧详情页激励视频回调

        return DJXSdk.factory().createDraw(
            DJXWidgetDrawParams.obtain()
                .adOffset(0) //单位 dp，为 0 时可以不设置
                .drawContentType(DJXWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_DRAMA)
                .drawChannelType(DJXWidgetDrawParams.DRAW_CHANNEL_TYPE_RECOMMEND)
                .hideClose(true, null)
                .hideChannelName(true)
                .detailConfig(dramaDetailConfig)
                .listener(DefaultDrawListener(drawListener)) // 混排流内视频监听
                .adListener(DefaultAdListener(adListener)) // 混排流内广告监听
        )
    }

    //加载剧场首页
    fun loadDramaHome(listener: IDJXDramaHomeListener?): IDJXWidget {
        val detailConfig = DJXDramaDetailConfig.obtain(DJXDramaUnlockAdMode.MODE_COMMON, FREE_SET, DefaultDramaUnlockListener(LOCK_SET, null))
        val params = DJXWidgetDramaHomeParams.obtain(detailConfig)
            .showBackBtn(false)
            .listener(DefaultDramaHomeListener(listener))

        return DJXSdk.factory().createDramaHome(params)
    }
}