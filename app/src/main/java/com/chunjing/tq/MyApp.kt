package com.chunjing.tq

import android.app.Application
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
val calendarVM: CalendarViewModel by lazy { MyApp.calendarVM}
val mainViewModel: MainViewModel by lazy { MyApp.mainViewModel }
val imageLoader: ImageLoader by lazy { MyApp.imageLoader }

open class MyApp : BaseApp() {

    private var mJPushRegId: String? = null

    companion object {
        lateinit var calendarVM: CalendarViewModel
        lateinit var mainViewModel: MainViewModel

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
        calendarVM = getAppViewModelProvider()[CalendarViewModel::class.java]
        mainViewModel = getAppViewModelProvider()[MainViewModel::class.java]
        MMKV.initialize(this)

        try {
            UMConfigure.preInit(this, UM_APP_ID, BuildConfig.UMENG_CHANNEL)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (ContentUtil.permissionGranted) {
            configUM()
        } else {
            JCollectionAuth.setAuth(this, false)
        }
    }

    override val viewModelStore: ViewModelStore
        get() = ViewModelStore()

    /**
     * 友盟配置
     */
    open fun configUM() {
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

        //  极光推送 register id
        JPushInterface.setDebugMode(false)
        JPushInterface.init(this)
        JCollectionAuth.setAuth(context, true);

        val registerId = JPushInterface.getRegistrationID(this)
        Log.e("BaseApp", "startUsingApp: register id = $registerId")
        if (!TextUtils.isEmpty(registerId)) {
            this.setJPushRegId(registerId)
        }

        /// bug收集
        CrashReport.initCrashReport(applicationContext, "8c7abb6cd2", false)

//        TTAdManagerHolder.init(this)
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