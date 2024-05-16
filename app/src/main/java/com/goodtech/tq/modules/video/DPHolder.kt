package com.goodtech.tq.modules.video

import android.app.Application
import android.content.Context
import android.util.Log
import com.bytedance.sdk.dp.DPSdk
import com.bytedance.sdk.dp.DPSdkConfig
import com.bytedance.sdk.dp.DPWidgetBannerParams
import com.bytedance.sdk.dp.DPWidgetBubbleParams
import com.bytedance.sdk.dp.DPWidgetDrawParams
import com.bytedance.sdk.dp.DPWidgetGridParams
import com.bytedance.sdk.dp.DPWidgetInnerPushParams
import com.bytedance.sdk.dp.DPWidgetNewsParams
import com.bytedance.sdk.dp.DPWidgetTextChainParams
import com.bytedance.sdk.dp.DPWidgetVideoCardParams
import com.bytedance.sdk.dp.DPWidgetVideoSingleCardParams
import com.bytedance.sdk.dp.IDPNativeData.DPNativeDataListener
import com.bytedance.sdk.dp.IDPPrivacyController
import com.bytedance.sdk.dp.IDPWidget
import com.bytedance.sdk.dp.IDPWidgetFactory
import com.goodtech.tq.app.App
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.event.DPStartEvent
import org.json.JSONObject

/**
 * Create by hanweiwei on 2020-03-26.
 */
object DPHolder {
    private const val TAG = "DPHolder"

    var isDPStarted = false
    private var disableABTest = false
    private var newUser = false
    private var aliveSec = 0

    private val factory: IDPWidgetFactory
        get() = DPSdk.factory()

    var isTeenMode = false

    fun init(application: Application) {
        try {
            initDp(application)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initDp(application: Application) {
        val isXlFont = application.getSharedPreferences("sp_dpsdk", Context.MODE_PRIVATE)
            ?.getBoolean("sp_key_xl_font", false)
            ?: false
        //1. 初始化，最好放到application.onCreate()执行
        val configBuilder = DPSdkConfig.Builder()
            .debug(true)
            .disableABTest(disableABTest)
            .newUser(newUser)
            .aliveSeconds(aliveSec)
            .fontStyle(if (isXlFont) DPSdkConfig.ArticleDetailListTextStyle.FONT_XL else DPSdkConfig.ArticleDetailListTextStyle.FONT_NORMAL)
            .luckConfig(DPSdkConfig.LuckConfig().application(application).enableLuck(false)) // 积分配置

        val config = configBuilder.build().apply {
            // 配置青少年模式，可选
            privacyController = object : IDPPrivacyController() {
                override fun isTeenagerMode(): Boolean {
                    return isTeenMode
                }
            }
        }

        DPSdk.init(application, App.SDK_SETTINGS_CONFIG, config)
        DPSdk.start { isSuccess, message ->
            //请确保使用时Sdk已经成功启动
            //isSuccess=true表示启动成功
            //启动失败，可以再次调用启动接口（建议最多不要超过3次)
            isDPStarted = isSuccess
            Log.e(TAG, "start result=$isSuccess, msg=$message")
            Bus.getInstance().sendEvent(DPStartEvent(isSuccess))
        }
    }

    fun buildDrawWidget(params: DPWidgetDrawParams?): IDPWidget {
        //创建draw视频流组件
        return factory.createDraw(params)
    }

    fun buildGridWidget(params: DPWidgetGridParams?): IDPWidget {
        //创建宫格组件
        return factory.createGrid(params)
    }

    fun buildDoubleFeedWidget(params: DPWidgetGridParams?): IDPWidget {
        //创建双Feed组件
        return factory.createDoubleFeed(params)
    }

    fun buildNewsTabsWidget(params: DPWidgetNewsParams?): IDPWidget {
        //创建多频道新闻组件
        return factory.createNewsTabs(params)
    }

    fun buildNewsOneTabWidget(params: DPWidgetNewsParams?): IDPWidget {
        //创建单列表新闻组件
        return factory.createNewsOneTab(params)
    }

    fun loadVideoCard(params: DPWidgetVideoCardParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadVideoCard(params, callback)
    }

    fun loadSmallVideoCard(params: DPWidgetVideoCardParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadSmallVideoCard(params, callback)
    }

    fun loadVideoSingleCard(params: DPWidgetVideoSingleCardParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadVideoSingleCard(params, callback)
    }

    fun loadTextChain(params: DPWidgetTextChainParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadTextChain(params, callback)
    }

    fun loadCustomVideoCard(params: DPWidgetVideoCardParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadCustomVideoCard(params, callback)
    }

    fun loadBubble(params: DPWidgetBubbleParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadBubble(params, callback)
    }

    fun loadBanner(params: DPWidgetBannerParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadBanner(params, callback)
    }

    fun loadInnerPush(params: DPWidgetInnerPushParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadInnerPush(params, callback)
    }

    fun loadVideoSingleCard4News(params: DPWidgetVideoSingleCardParams?, callback: IDPWidgetFactory.Callback?) {
        factory.loadVideoSingleCard4News(params, callback)
    }

    fun loadNativeNews(params: DPWidgetNewsParams?, listener: DPNativeDataListener?) {
        factory.loadNativeNews(params, listener)
    }

    fun enterNewsDetail(params: DPWidgetNewsParams?, groupId: Long, data: String?) {
        factory.enterNewsDetail(params, groupId, data)
    }

    fun loadPush(params: DPWidgetNewsParams?) {
        factory.pushNews(params)
    }

    fun uploadLog(category: String?, event: String?, json: JSONObject?) {
        factory.uploadLog(category, event, json)
    }

    fun notifyUserInfo() {
        //接入红包功能的开发者，在用户登录成功后需要刷新用户信息
//        long uid = LoginActivity.getUserId();
//        if (uid == 0) {
//            return;
//        }
//        getFactory().notifyUserInfo(new DPUser()
//                .setUserId(uid)//必须透传用户uid
//                .setName("test_name")//用户昵称，可选
//                .setAvatarUrl("xxx")//用户图像，可选
//        );
    }

}
