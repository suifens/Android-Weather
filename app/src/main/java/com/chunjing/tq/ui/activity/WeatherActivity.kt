package com.chunjing.tq.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.load
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.adapter.StatePagerAdapter
import com.chunjing.tq.bean.MessageEvent
import com.chunjing.tq.databinding.ActivityWeatherBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.vm.MainViewModel
import com.chunjing.tq.ui.base.BaseVmActivity
import com.chunjing.tq.ui.fragment.WeatherFragment
import com.goodtech.weatherlib.extension.startActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WeatherActivity : BaseVmActivity<ActivityWeatherBinding, MainViewModel>() {

    private val fragments: MutableList<Fragment> by lazy { ArrayList() }
    private val mCityList = ArrayList<CityEntity>()
    private var mCityChanged: Boolean = false
    private var mCurIndex = 0
    private var isNewCity = false
    //  是否在 Start和Pause之间
    private var isShowing: Boolean = true

    companion object {
        fun startActivity(context: Activity, position: Int) {
            val intent = Intent(context, WeatherActivity::class.java)
            intent.putExtra("position", position)
            context.startActivity(intent)
        }

        fun startActivity(context: Activity, showNewCity: Boolean) {
            val intent = Intent(context, WeatherActivity::class.java)
            intent.putExtra("showNew", showNewCity)
            context.startActivity(intent)
        }
    }

    override fun bindView() = ActivityWeatherBinding.inflate(layoutInflater)
    
    override fun prepareData(intent: Intent?) {
        intent?.let {
            mCurIndex = it.getIntExtra("position", 0)
            isNewCity = it.getBooleanExtra("showNew", false)
        }
    }

    override fun initView() {
        configStationBar(mBinding.privateStationBar)

//        configVideoView()

        mBinding.cityNameTv.isSelected = true
        mBinding.backButton.setOnClickListener {
            startActivity<MainActivity>()
        }
        mBinding.btnAdd.setOnClickListener { startActivity<CityListActivity>() }
        //  分享
        mBinding.btnShare.setOnClickListener {
            showLoading()
            startActivity<WeatherShareActivity>()
        }

        mBinding.viewPager.adapter = StatePagerAdapter(supportFragmentManager, fragments)
        mBinding.viewPager.offscreenPageLimit = 5

        /// 滑动
        mBinding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(i: Int) {
                val city = mCityList[i]
                if (city.isLocal()) {
                    mBinding.ivLoc.visibility = View.VISIBLE
                    mBinding.cityNameTv.text = city.mergerName
                } else {
                    mBinding.ivLoc.visibility = View.INVISIBLE
                    mBinding.cityNameTv.text = city.cityName
                }

                mBinding.llRound.getChildAt(mCurIndex).isEnabled = false
                mBinding.llRound.getChildAt(i).isEnabled = true
                mCurIndex = i

                if (mCityList.size > i) {
                    mainViewModel.setCity(mCityList[i])
                }
            }
        })

        //注册订阅者
        //避免重复注册，重复注册会导致崩溃
        if (!EventBus.getDefault().isRegistered(this)) { //这里的取反别忘记了
            EventBus.getDefault().register(this)
        }
    }

    override fun initEvent() {
        mainViewModel.cities.observe(this) {
            if (it.isEmpty()) {
                startActivity<AddCityActivity>()
            } else {
                showCity(it)
            }
            dismissLoading(1500)
        }

        mainViewModel.curBgEntity.observe(this) {
            if (isShowing) {
                showBg(it)
            }
        }
    }

    override fun initData() {
        mainViewModel.getCitiesCache()
        mainViewModel.curBgEntity.value?.let {
            showBg(it)
        }
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
        if (mCityChanged) {
            showLoading()
            mainViewModel.getCitiesCache()
            mCityChanged = false
        } else {
            if (mCityList.isNotEmpty()) {
                for (i in mainViewModel.cities.value!!.indices) {
                    mBinding.llRound.getChildAt(i).isEnabled = mCurIndex == i
                }
                mBinding.viewPager.setCurrentItem(mCurIndex, true)
            }
        }
    }

    override fun onPause() {
        isShowing = false
        isNewCity = false
        super.onPause()
//        if (mBinding.videoView.isPlaying) {
//            mBinding.videoView.pause()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //注销订阅者
        EventBus.getDefault().unregister(this)
    }

    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.cityChanged) {
            mCityChanged = true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity<MainActivity>()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private var mBgEntity: WeatherBgEntity? = null
    private fun showBg(bgEntity: WeatherBgEntity) {
        mBgEntity = bgEntity
//        if (bgEntity.videoPath.endsWith("mp4")) {
//            Log.e("path", "videoPath = ${bgEntity.videoPath}" )
//            mBinding.videoView.setVideoPath(bgEntity.videoPath)
//
//            if (!bgEntity.videoPath.startsWith("http")) {
//                return
//            }
//
//            downVideo(bgEntity)
//        } else if (mBinding.videoView.isPlaying) {
//            mBinding.videoView.pause()
//        }

        showWeatherImg(bgEntity)
    }

    private fun showWeatherImg(bgEntity: WeatherBgEntity) {
        mBinding.weatherImgV.visibility = View.VISIBLE
        val originDrawable = mBinding.weatherImgV.drawable
        mBinding.weatherImgV.load(bgEntity.imgPath, imageLoader) {
            if (originDrawable == null) {
                placeholder(R.drawable.gradient_weather_main)
            } else {
                placeholder(originDrawable)
            }
        }
    }

//    private fun downVideo(bgEntity: WeatherBgEntity) {
//        PermissionUtils.permission(
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ).callback(object : PermissionUtils.SimpleCallback {
//            override fun onGranted() {
//                mainViewModel.downAloneVideo(bgEntity, false)
//            }
//
//            override fun onDenied() {
//
//            }
//        }).request()
//    }
//
//    private fun configVideoView() {
//        mBinding.videoView.setOnPreparedListener {
//            it.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
//            mBinding.videoView.start()
//        }
//        mBinding.videoView.setOnInfoListener { _, what, _ ->
//            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
//                mBinding.weatherImgV.visibility = View.GONE
//            }
//            true
//        }
//        mBinding.videoView.setOnErrorListener { mp, what, extra ->
//            mBinding.videoView.stopPlayback()
//            mBgEntity?.let {
//                showWeatherImg(it)
//            }
//            true
//        }
//        mBinding.videoView.setOnCompletionListener {
//            it.start()
//            it.isLooping = true
//        }
//    }

    /**
     * 显示城市
     */
    private fun showCity(cityList: List<CityEntity>) {

        mCityList.clear()
        mCityList.addAll(cityList)
        if (mCurIndex >= cityList.size || isNewCity) {
            mCurIndex = cityList.size - 1
        }

        cityList[mCurIndex].let {
            if (it.isLocal()) {
                mBinding.ivLoc.visibility = View.VISIBLE
                mBinding.cityNameTv.text = it.mergerName
            } else {
                mBinding.ivLoc.visibility = View.INVISIBLE
                mBinding.cityNameTv.text = it.cityName
            }
        }
        mBinding.ivLoc.visibility =
            if (cityList[mCurIndex].isLocal()) View.VISIBLE else View.INVISIBLE
        mBinding.cityNameTv.text = if (cityList[mCurIndex].isLocal()) cityList[mCurIndex].mergerName else cityList[mCurIndex].cityName

        mBinding.llRound.removeAllViews()

        // 宽高参数
        val size = SizeUtils.dp2px(4f)
        val layoutParams = LinearLayout.LayoutParams(size, size)
        // 设置间隔
        layoutParams.rightMargin = 10

        for (i in cityList.indices) {
            // 创建底部指示器(小圆点)
            val view = View(this@WeatherActivity)
            view.setBackgroundResource(R.drawable.item_round)
            view.isEnabled = false

            // 添加到LinearLayout
            mBinding.llRound.addView(view, layoutParams)
        }
        // 小白点
        mBinding.llRound.getChildAt(mCurIndex).isEnabled = true
        mBinding.llRound.visibility = View.VISIBLE

        fragments.clear()
        for (city in cityList) {
            val cityId = city.cityId
            val weatherFragment = WeatherFragment.newInstance(cityId)
            fragments.add(weatherFragment)
        }

        mBinding.viewPager.adapter = StatePagerAdapter(supportFragmentManager, fragments)
        mBinding.viewPager.currentItem = mCurIndex
    }

}