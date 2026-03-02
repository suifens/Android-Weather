package com.chunjing.tq.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.*
import com.bytedance.sdk.openadsdk.TTSplashAd
import com.chunjing.tq.R
import com.chunjing.tq.ad.AdManager
import com.chunjing.tq.databinding.ActivitySplashBinding
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.ext.checkGPSOpen
import com.chunjing.tq.ext.checkGPSPermission
import com.chunjing.tq.ext.startWidgetService
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseActivity
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.extension.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val TAG = "SplashActivity"
    private var isAdShowing = false
    private var isDataReady = false
    private var citySize = 0

    private fun startIntent() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val cities = AppRepo.getInstance().getCities()
                citySize = cities.size

                if (citySize > 0) {
                    startWidgetService()
                }

                getScreenInfo()

                if (AppRepo.getInstance().getCache<String>("Version") == null) {
                    copyVideo()
                }

                if (AppRepo.getInstance()
                        .getCache<String>("Version") != AppUtils.getAppVersionName()
                ) {
                    AppRepo.getInstance().saveCache(
                        "Weather_Bg_Update", TimeUtils.millis2String(
                            0L,
                            "yyyy-MM-dd"
                        )
                    )
                    AppRepo.getInstance().saveCache("Version", AppUtils.getAppVersionName())
                }
                mainViewModel.fetchWeatherBg()
                mainViewModel.fetchCalendarBg()

                if (NetworkUtils.isConnected() && checkGPSOpen() && checkGPSPermission()) {
                    mainViewModel.getLocation()
                }
            }
            isDataReady = true
            tryGoNext()
        }
    }

    private fun loadSplashAd() {
        if (!ContentUtil.permissionGranted) {
            isAdShowing = false
            tryGoNext()
            return
        }

        AdManager.loadSplashAd(this, mBinding.splashContainer, object : AdManager.AdCallback<TTSplashAd> {
            override fun onSuccess(ad: TTSplashAd) {
                Log.d(TAG, "开屏广告加载成功")
                ad.setSplashAdListener(object : TTSplashAd.SplashAdListener {
                    override fun onSplashAdShow(ad: TTSplashAd?) {
                        Log.d(TAG, "开屏广告展示")
                    }

                    override fun onSplashAdClick(ad: TTSplashAd?) {
                        Log.d(TAG, "开屏广告点击")
                    }

                    override fun onSplashAdClose(ad: TTSplashAd?, closeType: Int) {
                        Log.d(TAG, "开屏广告关闭: $closeType")
                        mBinding.splashContainer.removeAllViews()
                        isAdShowing = false
                        tryGoNext()
                    }
                })
                val splashView = ad.splashView
                if (splashView != null) {
                    mBinding.splashContainer.removeAllViews()
                    mBinding.splashContainer.addView(splashView)
                    isAdShowing = true
                }
            }

            override fun onFail(code: Int, msg: String) {
                Log.e(TAG, "开屏广告加载失败: $code, $msg")
                isAdShowing = false
                tryGoNext()
            }
        })
    }

    private fun tryGoNext() {
        if (isAdShowing || !isDataReady) {
            return
        }
        goNext()
    }

    private fun goNext() {
        if (citySize == 0) {
            if (ContentUtil.permissionGranted) {
                AddCityActivity.startActivity(this@SplashActivity, true)
            } else {
                startActivity<PermissionActivity>()
            }
        } else {
            startActivity<MainActivity>()
        }
        finish()
    }

    private fun copyVideo() {
        val videoResIds = arrayOf(
            R.raw.details_day_heavyrains,
            R.raw.details_day_morecloud,
            R.raw.details_day_snows,
            R.raw.details_day_sunny,
            R.raw.details_day_sunrise,
            R.raw.details_night_heavyrains,
            R.raw.details_night_lightrain,
            R.raw.details_night_sunny
        )

        val videos = arrayOf(
            "details_day_heavyrains.mp4",
            "details_day_morecloud.mp4",
            "details_day_snows.mp4",
            "details_day_sunny.mp4",
            "details_day_sunrise.mp4",
            "details_night_heavyrains.mp4",
            "details_night_lightrain.mp4",
            "details_night_sunny.mp4"
        )

        for (i in videoResIds.indices) {
            val resId = videoResIds[i]
            val video = videos[i]
            ResourceUtils.copyFileFromRaw(
                resId,
                ContentUtil.getVideoDir() + "/" + video
            )
        }
    }

    private fun getScreenInfo() {
        val screenRealSize = ScreenUtils.getAppScreenHeight()

        val statusBarHeight = BarUtils.getStatusBarHeight()
        val dp45 = SizeUtils.dp2px(45f)
        ContentUtil.screenHeight = screenRealSize
        ContentUtil.visibleHeight = screenRealSize - statusBarHeight - dp45
    }


    // <editor-fold defaulted="collapsed" desc="Override">

    override fun bindView() = ActivitySplashBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {

    }

    override fun initView() {
        immersionStatusBar()
    }

    override fun initEvent() {

    }

    override fun initData() {

    }

    override fun onStart() {
        super.onStart()
        startIntent()
        loadSplashAd()
    }
    // </editor-fold>
}