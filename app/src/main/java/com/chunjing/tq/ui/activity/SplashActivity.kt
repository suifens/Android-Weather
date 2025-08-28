package com.chunjing.tq.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.*
import com.chunjing.tq.R
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

    private fun startIntent() {
        lifecycleScope.launch {
            var citySize: Int

            withContext(Dispatchers.IO) {
                val start = System.currentTimeMillis()
                val cities = AppRepo.getInstance().getCities()
                citySize = cities.size

                if (citySize > 0) {
                    startWidgetService()
                }

                getScreenInfo()

                if (AppRepo.getInstance().getCache<String>("Version") == null) {
                    /// 第一次安装
                    copyVideo()
                }

                if (AppRepo.getInstance()
                        .getCache<String>("Version") != AppUtils.getAppVersionName()
                ) {
                    //  新版本
                    AppRepo.getInstance().saveCache(
                        "Weather_Bg_Update", TimeUtils.millis2String(
                            0L,
                            "yyyy-MM-dd"
                        )
                    )
                    AppRepo.getInstance().saveCache("Version", AppUtils.getAppVersionName())
                }
                //  获取背景资料
                mainViewModel.fetchWeatherBg()
                mainViewModel.fetchCalendarBg()

                if (NetworkUtils.isConnected() && checkGPSOpen() && checkGPSPermission()) {
                    mainViewModel.getLocation()
                }

                delay(1200L)
            }
            if (citySize == 0) {
                if (ContentUtil.permissionGranted) {
                    AddCityActivity.startActivity(this@SplashActivity, true)
                } else {
                    startActivity<PermissionActivity>()
                }
            } else {
                startActivity<MainActivity>()
//                MainActivity.startActivity(this@SplashActivity, 0)
            }
            finish()
        }
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
    }
    // </editor-fold>
}