package com.chunjing.tq.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NetworkUtils
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdLoadType
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdNative.FullScreenVideoAdListener
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd.FullScreenVideoAdInteractionListener
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot
import com.chunjing.tq.BuildConfig
import com.chunjing.tq.R
import com.chunjing.tq.ad.TTAdManagerHolder
import com.chunjing.tq.databinding.ActivityMainBinding
import com.chunjing.tq.dialog.RecommendPopup
import com.chunjing.tq.dialog.UpdatePopup
import com.chunjing.tq.ext.MILLIS_DAY
import com.chunjing.tq.ext.checkGPSOpen
import com.chunjing.tq.ext.checkGPSPermission
import com.chunjing.tq.ext.checkPermissionAgree
import com.chunjing.tq.ext.init
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseActivity
import com.chunjing.tq.ui.fragment.CalendarFragment
import com.chunjing.tq.ui.fragment.SettingsFragment
import com.chunjing.tq.utils.ContentUtil
import com.chunjing.tq.utils.ShareFileUtils
import java.io.File
import java.io.FileOutputStream
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.extension.toast
import com.goodtech.weatherlib.net.LoadState
import com.goodtech.weatherlib.utils.SpUtils
import com.lxj.xpopup.XPopup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {
        fun startActivity(context: Activity, position: Int) {
//            if (ActivityUtils.isActivityExistsInStack(MainActivity::class.java)) {
//                mainViewModel.showIndex.postValue(position)
//                context.finishAffinity()
//            } else {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("position", position)
                mainViewModel.showIndex.postValue(position)
                context.startActivity(intent)
//            }
        }

        fun startActivity(context: Activity, showNewCity: Boolean) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("showNew", showNewCity)
            mainViewModel.showIndex.postValue(1000)
            context.startActivity(intent)
        }
    }

    private val TAG = "MainActivity"

    private val fragments: ArrayList<Fragment> = arrayListOf()
    private var curIndex = 0
    private var isShowing: Boolean = false

    override fun bindView() = ActivityMainBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
        fragments.add(MainFragment())
        fragments.add(CalendarFragment())
        fragments.add(SettingsFragment())
    }

    override fun initView() {

        immersionStatusBar()
        //  开始定位计时
        mainViewModel.startTimer()

        mBinding.viewPager.init(this, fragments).offscreenPageLimit = fragments.size
        mBinding.viewPager.isUserInputEnabled = false

        mBinding.homeTab.setOnClickListener {
            this.onTabSelected(0)
        }
        mBinding.calendarTab.setOnClickListener {
            checkPermissionAgree { this.onTabSelected(1) }
        }
        mBinding.settingTab.setOnClickListener {
            checkPermissionAgree { this.onTabSelected(2) }
        }

        selectedTab(1)
    }

    private fun onTabSelected(tab: Int) {
        this.selectedTab(tab + 1)
        mBinding.viewPager.setCurrentItem(tab, false)
    }

    override fun initEvent() {

        mainViewModel.curLocation.observe(this) {
            mainViewModel.fetchWeather(it)
        }

        mainViewModel.cities.observe(this) {
            mainViewModel.getWeathers()
        }

        mainViewModel.needLocation.observe(this) {
            if (it) {
                //  开始定位
                checkLocation()
            }
        }

        //  加载状态
        mainViewModel.loadState.observe(this) {
            when (it) {
                is LoadState.Start -> {
                    showLoading(true, it.tip)
                }

                is LoadState.Error -> {
                    toast(it.msg)
                }

                is LoadState.Finish -> {
                    dismissLoading()
                }
            }
        }
    }

    override fun initData() {
        mainViewModel.getCitiesCache()
        mainViewModel.getCacheLocation()
        checkUpdate()

        CoroutineScope(Dispatchers.IO).launch {
            delay(4000L)
            if (mainViewModel.needShowRecommendAlert()) {
                CoroutineScope(Dispatchers.Main).launch {
                    showRecommendAlert()
                }
            }
        }
    }

    //<editor-fold desc="滑动返回">
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private var mBackTime: Long = 0

    private fun backAction() {
        val current = System.currentTimeMillis()
        if (current - mBackTime < 2 * 1000) {
            /// 退出app
            finish()
        } else {
            mBackTime = current
            Toast.makeText(this@MainActivity, "再按一次退出程序", Toast.LENGTH_SHORT).show()
        }
    }
    //</editor-fold>

    private fun checkUpdate() {
        mainViewModel.checkVersion { version ->
            version?.let {
                val curVersion = AppUtils.getAppVersionName()
                val curList = curVersion.split(".")
                val versionTemp = it.split(".")
                var needUpdate = false
                for (i in versionTemp.indices) {
                    if (versionTemp[i].toInt() > curList[i].toInt()) {
                        needUpdate = true
                        break
                    }
                }
                if (needUpdate
                    && System.currentTimeMillis() - SpUtils.instance.getLong(it, 0) > 2 * MILLIS_DAY
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        showUpdatePopup(it)
                    }
                }
            }
        }
    }

    private fun showUpdatePopup(version: String) {

        SpUtils.instance.putLong(version, System.currentTimeMillis())
        val popup = UpdatePopup(this)
        popup.setupVersion(version) {
            val uri = Uri.parse("market://details?id=$packageName")
            val intent =
                Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                //要调起的应用不存在时的处理
                Toast.makeText(this, "未能跳转到应用商店", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        XPopup.Builder(this)
            .isDestroyOnDismiss(false)
            .asCustom(popup)
            .show()
    }

    private var isFirstLoad = true
    override fun onResume() {
        super.onResume()
        dismissLoading()
        isShowing = true

        if (isFirstLoad && ContentUtil.permissionGranted) {
            isFirstLoad = false
            loadInterstitialFullAd()
        }
    }

    override fun onPause() {
        super.onPause()
        isShowing = false
    }

    private fun checkLocation() {
        //  检测是否需要定位
        if (NetworkUtils.isConnected() && checkGPSOpen() && checkGPSPermission()) {
            mainViewModel.getLocation()
        }
    }

    private fun selectedTab(index: Int) {
        if (curIndex == index) {
            return
        }
        if (index == 2) {
            mBinding.homeImgV.setImageResource(R.drawable.tab_1_w)
            mBinding.calendarImgV.setImageResource(R.drawable.tab_2_s)
            mBinding.settingImgV.setImageResource(R.drawable.tab_3_w)
            mBinding.calendarTv.setTextColor(Color.WHITE)
            mBinding.homeTv.setTextColor(Color.WHITE)
            mBinding.settingTv.setTextColor(Color.WHITE)
            mBinding.bottomLayout.setBackgroundColor(Color.TRANSPARENT)
        } else {
            mBinding.calendarTv.setTextColor(ContextCompat.getColor(this, R.color.black))
            mBinding.homeTv.setTextColor(ContextCompat.getColor(this, R.color.black))
            mBinding.settingTv.setTextColor(ContextCompat.getColor(this, R.color.black))

            mBinding.homeImgV.setImageResource(if (index == 1) R.drawable.tab_1_s else R.drawable.tab_1_n)
            mBinding.calendarImgV.setImageResource(R.drawable.tab_2_n)
            mBinding.settingImgV.setImageResource(if (index == 3) R.drawable.tab_3_s else R.drawable.tab_3_n)
            mBinding.bottomLayout.setBackgroundColor(Color.WHITE)
        }
        mBinding.homeTv.alpha = if (index == 1) 1f else 0.5f
        mBinding.calendarTv.alpha = if (index == 2) 1f else 0.5f
        mBinding.settingTv.alpha = if (index == 3) 1f else 0.5f

        curIndex = index

    }

    /**
     * 显示推荐弹窗
     */
    private fun showRecommendAlert() {
        val popup = RecommendPopup(context)
        popup.setListener {
            shareBtnPressed()
        }
        XPopup.Builder(context)
            .isDestroyOnDismiss(true)   //  只使用一次
            .dismissOnTouchOutside(false)
            .asCustom(popup)
            .show()
    }

    /**
     * 分享按钮点击：调用系统分享，分享 share_recommend 图片
     */
    @SuppressLint("ResourceType")
    private fun shareBtnPressed() {
        try {
            val cacheFile = File(cacheDir, "share_recommend.webp")
            resources.openRawResource(R.drawable.share_recommend).use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
            ShareFileUtils.shareImage(this, cacheFile.absolutePath)
        } catch (e: Exception) {
            toast("分享失败")
        }
    }

    private var hadShowAd = false
    private var mTTFullScreenVideoAd: TTFullScreenVideoAd? = null // 插全屏广告对象

    private var mFullScreenVideoListener: FullScreenVideoAdListener? = null // 广告加载监听器

    private var mFullScreenVideoAdInteractionListener: FullScreenVideoAdInteractionListener? = null // 广告展示监听器

    private fun loadInterstitialFullAd() {
        /** 1、创建AdSlot对象  */

        val adslot = AdSlot.Builder().setCodeId(BuildConfig.PGE_INT_POS_ID)
            .setOrientation(TTAdConstant.ORIENTATION_VERTICAL)
            .setAdCount(1)
            .setAdLoadType(TTAdLoadType.LOAD)
            .setMediationAdSlot(MediationAdSlot.Builder()
                .setMuted(true)//是否静音
                .setVolume(0.7f)//设置音量
                .setBidNotify(true)//竞价结果通知
                .build())
            .build()

        /** 2、创建TTAdNative对象  */
        Log.i("TAG", "success: " + "----- main start")
        val adNativeLoader: TTAdNative = TTAdManagerHolder.get().createAdNative(this)

        /** 3、创建加载、展示监听器  */
        initListeners()

        /** 4、加载广告  */
        adNativeLoader.loadFullScreenVideoAd(adslot, this.mFullScreenVideoListener)
    }

    // 在加载成功后展示广告
    private fun showInterstitialFullAd() {
        if (this.mTTFullScreenVideoAd == null) {
            Log.d(TAG, "请先加载广告或等待广告加载完毕后再调用show方法")
            return
        }

        /** 5、设置展示监听器，展示广告  */
        mTTFullScreenVideoAd!!.setFullScreenVideoAdInteractionListener(this.mFullScreenVideoAdInteractionListener)

        mTTFullScreenVideoAd!!.showFullScreenVideoAd(this@MainActivity)

        hadShowAd = true
    }

    private fun initListeners() {
        // 广告加载监听器

        this.mFullScreenVideoListener = object : FullScreenVideoAdListener {
            override fun onError(code: Int, message: String) {
                Log.d(TAG, "InterstitialFull onError code = $code msg = $message")
            }


            override fun onFullScreenVideoAdLoad(ad: TTFullScreenVideoAd) {
                Log.d(TAG, "InterstitialFull onFullScreenVideoLoaded")
                mTTFullScreenVideoAd = ad
            }


            override fun onFullScreenVideoCached() {
                Log.d(TAG, "InterstitialFull onFullScreenVideoCached")
            }


            override fun onFullScreenVideoCached(ad: TTFullScreenVideoAd) {
                Log.d(TAG, "InterstitialFull onFullScreenVideoCached")
                mTTFullScreenVideoAd = ad
                if (isShowing && !hadShowAd) {
                    MainScope().launch {
                        showInterstitialFullAd()
                    }
                }
            }
        }

        // 广告展示监听器
        this.mFullScreenVideoAdInteractionListener = object : FullScreenVideoAdInteractionListener {
            override fun onAdShow() {
                Log.d(TAG, "InterstitialFull onAdShow")
            }


            override fun onAdVideoBarClick() {
                Log.d(TAG, "InterstitialFull onAdVideoBarClick")
            }


            override fun onAdClose() {
                Log.d(TAG, "InterstitialFull onAdClose")
            }


            override fun onVideoComplete() {
                Log.d(TAG, "InterstitialFull onVideoComplete")
            }


            override fun onSkippedVideo() {
                Log.d(TAG, "InterstitialFull onSkippedVideo")
            }
        }
    }

}