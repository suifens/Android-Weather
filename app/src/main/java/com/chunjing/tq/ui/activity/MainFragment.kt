package com.chunjing.tq.ui.activity

import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.adapter.FragmentPagerAdapter
import com.chunjing.tq.bean.MessageEvent
import com.chunjing.tq.databinding.FragmentMainBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.vm.MainViewModel
import com.chunjing.tq.ui.base.BaseVmFragment
import com.chunjing.tq.ui.fragment.WeatherFragment
import com.goodtech.weatherlib.extension.startActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainFragment : BaseVmFragment<FragmentMainBinding, MainViewModel>() {

    private val fragments: MutableList<Fragment> by lazy { ArrayList() }
    private val mCityList = ArrayList<CityEntity>()
    private var mCityChanged: Boolean = false
    private var mCurIndex = 0
    private var isNewCity = false
    //  是否在 Start和Pause之间
    private var isShowing: Boolean = true

    override fun bindView() = FragmentMainBinding.inflate(layoutInflater)
    
    override fun initView(view: View?) {

        val bars = ConstraintLayout.LayoutParams(mBinding.privateStationBar.layoutParams)
        bars.height = bars.height + BarUtils.getStatusBarHeight()
        mBinding.privateStationBar.layoutParams = bars
        
//        configVideoView()

        mBinding.cityNameTv.isSelected = true
        mBinding.listImgView.setOnClickListener {
            startActivity<CityListActivity>()
        }
        mBinding.btnAdd.setOnClickListener { startActivity<AddCityActivity>() }
        //  分享
//        mBinding.btnShare.setOnClickListener {
//            showLoading(true)
//            startActivity<WeatherShareActivity>()
//        }

        mBinding.viewPager.adapter = FragmentPagerAdapter(this, fragments)
        mBinding.viewPager.offscreenPageLimit = 5

        /// 滑动
        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
                configCities(it)
            }
            dismissLoading()
        }

        mainViewModel.curBgEntity.observe(this) {
            if (isShowing) {
                showBg(it)
            }
        }

        mainViewModel.showIndex.observe(this) {
            if (it == 1000) {
                isNewCity = true
            } else {
                mCurIndex = it
            }
        }
    }

    override fun loadData() {
        mainViewModel.getCitiesCache()
        mainViewModel.curBgEntity.value?.let {
            showBg(it)
        }
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
        if (mCityChanged) {
            showLoading(true)
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
    private fun configCities(cityList: List<CityEntity>) {

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
            val view = View(mContext)
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

        mBinding.viewPager.adapter = FragmentPagerAdapter(this, fragments)
        mBinding.viewPager.currentItem = mCurIndex
    }

}