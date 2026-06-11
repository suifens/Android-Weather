package com.chunjing.tq

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModelStore
import cn.jiguang.api.utils.JCollectionAuth
import cn.jpush.android.api.JPushInterface
import coil.ImageLoader
import coil.request.CachePolicy
import com.chunjing.tq.ad.TTAdManagerHolder
import com.chunjing.tq.ext.UM_APP_ID
import com.chunjing.tq.jpush.JPushHelper
import com.chunjing.tq.ui.activity.vm.CalendarViewModel
import com.chunjing.tq.ui.activity.vm.MainViewModel
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.BaseApp
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import kotlin.properties.Delegates

/**
 * com.chunjing.tq
 */
val calendarVM: CalendarViewModel by lazy { MyApp.calendarVmInstance}
val mainViewModel: MainViewModel by lazy { MyApp.mainViewModelInstance }
val imageLoader: ImageLoader by lazy { MyApp.imageLoader }

open class MyApp : BaseApp() {

    private var mJPushRegId: String? = null

    companion object {
        lateinit var calendarVmInstance: CalendarViewModel
        lateinit var mainViewModelInstance: MainViewModel

        val imageLoader: ImageLoader by lazy {
            ImageLoader.Builder(context)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .crossfade(1000)
                .build()
        }

        private var instance: MyApp by Delegates.notNull()
        fun instance(): MyApp = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        calendarVmInstance = getAppViewModelProvider()[CalendarViewModel::class.java]
        mainViewModelInstance = getAppViewModelProvider()[MainViewModel::class.java]
        MMKV.initialize(this)

        try {
            UMConfigure.preInit(this, UM_APP_ID, BuildConfig.UMENG_CHANNEL)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (ContentUtil.permissionGranted) {
            configUM()
        }
    }

    override val viewModelStore: ViewModelStore
        get() = ViewModelStore()

    /**
     * 关闭第三方 SDK 对「全量应用安装列表」的采集（须在友盟 init 之前调用）。
     */
    private fun disableThirdPartyAppListCollection() {
        try {
            Class.forName("com.uyumao.sdk.UYMManager")
                .getMethod("enableYm6", android.content.Context::class.java, java.lang.Boolean.TYPE)
                .invoke(null, this, false)
        } catch (_: Exception) {
            // 低版本 asms 无此 API 时忽略
        }
    }

    /**
     * 友盟配置
     */
    open fun configUM() {
        disableThirdPartyAppListCollection()
        //  配置 UM_APP_ID , 标识
        UMConfigure.init(
            this,
            UM_APP_ID,
            BuildConfig.UMENG_CHANNEL,
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
//        //手动采集选择
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)
        //
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)

        //  极光推送 register id（关闭智能推送，避免读取已安装应用列表做分群）
        JPushInterface.setDebugMode(false)
        JPushInterface.setSmartPushEnable(this, false)
        JPushInterface.init(this)
        JCollectionAuth.setAuth(context, true);

        val registerId = JPushInterface.getRegistrationID(this)
        Log.e("BaseApp", "startUsingApp: register id = $registerId")
        if (!TextUtils.isEmpty(registerId)) {
            this.setJPushRegId(registerId)
        }

        /// bug收集
        CrashReport.initCrashReport(applicationContext, "8c7abb6cd2", false)
        // 穿山甲广告 SDK 延迟到首次加载广告时再初始化（见 AdManager.ensureSdkInit）
    }

    open fun getJPushRegId(): String? {
        return mJPushRegId
    }

    open fun setJPushRegId(jPushRegId: String) {
        this.mJPushRegId = jPushRegId
        Log.e("BaseApp", "setJPushRegId: $jPushRegId")
        //  恢复极光推送
        JPushHelper.resumePush()
    }

}