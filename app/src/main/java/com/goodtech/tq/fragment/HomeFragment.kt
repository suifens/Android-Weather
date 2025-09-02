package com.goodtech.tq.fragment

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot
import com.goodtech.tq.BuildConfig
import com.goodtech.tq.R
import com.goodtech.tq.activity.BaseActivity
import com.goodtech.tq.activity.SettingActivity
import com.goodtech.tq.app.App
import com.goodtech.tq.databinding.FragmentHomeBinding
import com.goodtech.tq.db.SignDbHelper
import com.goodtech.tq.eventbus.CityEvent
import com.goodtech.tq.eventbus.MessageEvent
import com.goodtech.tq.fragment.adapter.ViewPagerAdapter
import com.goodtech.tq.helpers.LocationSpHelper
import com.goodtech.tq.helpers.WeatherSpHelper
import com.goodtech.tq.listener.CompletionListener
import com.goodtech.tq.location.helper.LocationHelper
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.cityList.CityListActivity
import com.goodtech.tq.modules.signing.SigningActivity
import com.goodtech.tq.utils.IntentReceiver
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.utils.TimeUtils
import com.goodtech.tq.utils.TipHelper
import com.goodtech.tq.utils.WeatherUtils
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : BaseFragment() {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private val receiver: BroadcastReceiver = IntentReceiver()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    // 数据
    private val fragmentList = mutableListOf<Fragment>()
    private var cityModes = mutableListOf<CityMode>()
    private var currIndex = 0
    private var loadLast = false
    private var signed = false
    private var isCurrent = false
    private var isNeedReload = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupClickListeners()
        configViewPager()
        registerEventBus()
        
        isCurrent = true
        
        if (isFirstLoad) {
            if (SpUtils.getInstance().isAgreePermission()) {
                // 判断新版本
                fetchNewVersion { }
            }
            isFirstLoad = false
        }

        if (LocationSpHelper.getLocation() != null && SpUtils.getInstance().isAgreePermission()) {
            LocationHelper.getInstance().startWithDelay(requireActivity())
        }

        val cityModesFromSp = LocationSpHelper.getCityListAndLocation()
        if (cityModesFromSp.size != cityModes.size || isNeedReload) {
            cityModes = cityModesFromSp.toMutableList()
        }
        reloadView()
    }

    private fun setupViews() {
        // 配置状态栏
        configStationBar(binding.privateStationBar)
    }

    private fun switchViewPager(index: Int) {
        binding.viewPager.currentItem = index
        setIndicator(index)
    }

    private fun configStationBar(stationBar: View) {
        val bars = stationBar.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        bars.height += BarUtils.getStatusBarHeight()
        stationBar.layoutParams = bars
    }

    private fun setupClickListeners() {
        // 点击地址，跳转到城市列表
        binding.layoutAddress.setOnClickListener {
            if (!SpUtils.getInstance().isAgreePermission()) {
                (requireActivity() as? BaseActivity)?.showPermissionDialog(requireActivity()) {
                    CityListActivity.redirectTo(requireActivity())
                }
                return@setOnClickListener
            }
            CityListActivity.redirectTo(requireActivity())
        }

        // 跳转到设置页面
        binding.imgSetting.setOnClickListener {
            if (!SpUtils.getInstance().isAgreePermission()) {
                (requireActivity() as? BaseActivity)?.showPermissionDialog(requireActivity()) {
                    startActivity(Intent(requireActivity(), SettingActivity::class.java))
                }
                return@setOnClickListener
            }
            startActivity(Intent(requireActivity(), SettingActivity::class.java))
        }

        // 签到按钮
        binding.imgSign.setOnClickListener {
            if (!SpUtils.getInstance().isAgreePermission()) {
                (requireActivity() as? BaseActivity)?.showPermissionDialog(requireActivity()) {
                    onSignClick()
                }
                return@setOnClickListener
            }
            onSignClick()
        }
    }

    private fun configViewPager() {
        val fragment = WeatherFragment()
        fragment.setStateBar(binding.stationBgView)
        fragmentList.add(fragment)
        val adapter = ViewPagerAdapter(requireActivity(), fragmentList)
        binding.viewPager.adapter = adapter
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (currIndex != position) {
                    currIndex = position
                    setIndicator(position)
                }
            }
        })
    }

    private fun registerEventBus() {
        EventBus.getDefault().register(this)
        
        // 注册广播接收器
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireActivity().registerReceiver(receiver, filter)
    }

    override fun onResume() {
        super.onResume()
        isCurrent = true
        if (isNeedReload) {
            val cityModesFromSp = LocationSpHelper.getCityListAndLocation()
            if (cityModesFromSp.size != cityModes.size || isNeedReload) {
                cityModes = cityModesFromSp.toMutableList()
            }
            reloadView()
        }
    }

    override fun onPause() {
        super.onPause()
        isCurrent = false
    }

    override fun onStop() {
        if (SpUtils.getInstance().isAgreePermission()) {
            LocationHelper.getInstance().stop()
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        requireActivity().unregisterReceiver(receiver)
        _binding = null
    }

    private fun reloadView() {
        if (!isNeedReload) {
            return
        }

        isNeedReload = false
        configRgIndicator(cityModes.size)
        reloadFragment()

        if (loadLast) {
            currIndex = fragmentList.size - 1
            loadLast = false
        } else {
            currIndex = minOf(currIndex, fragmentList.size - 1)
        }
        
        // 更新天气
        reloadWeathers()
        switchViewPager(currIndex)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CityEvent) {
        if (event.showIndex() >= 0) {
            currIndex = event.showIndex()
            switchViewPager(currIndex)
        }
        if (event.isAddCity) {
            loadLast = true
            isNeedReload = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        Log.e(TAG, "onMessageEvent: ")
        TipHelper.dismissProgressDialog()

        if (event.loadIntAd) {
            // 广告相关逻辑
            if (System.currentTimeMillis() - SpUtils.getInstance().getLong(
                    BuildConfig.PGE_INT_POS_ID,
                    0L
                ) > 12 * 60 * 60 * 1000 && !App.instance.hadInitAd
            ) {
                App.instance.hadInitAd = true
                lifecycleScope.launch {
                    initAdLoader()
                }
            }
            return
        }

        if (event.isSuccessLocation) {
            // 定位成功
            val cityMode = LocationSpHelper.getLocation()
            if (cityMode != null && cityModes.isNotEmpty()) {
                cityModes[0] = cityMode
                reloadWeather(0)
            }
        }
        
        if (event.isNeedReload) {
            isNeedReload = true
        }
        
        if (event.getFetchCId().isNotEmpty()) {
            for (i in cityModes.indices) {
                val cityMode = cityModes[i]
                if (cityMode.poiId == event.getFetchCId()) {
                    reloadWeather(i)
                    break
                }
            }
        }
    }

    private fun reloadFragment() {
        if (fragmentList.size != cityModes.size) {
            while (fragmentList.size > cityModes.size) {
                fragmentList.removeAt(fragmentList.size - 1)
            }
            while (fragmentList.size < cityModes.size) {
                addFragment()
            }
            val adapter = binding.viewPager.adapter as ViewPagerAdapter
            adapter.replaceAll(fragmentList)
        }
    }

    private fun addFragment() {
        val fragment = WeatherFragment()
        fragment.setStateBar(binding.stationBgView)
        fragmentList.add(fragment)
    }

    private fun reloadWeathers() {
        val signDbHelper = SignDbHelper(requireActivity())
        signed = signDbHelper.hadSigning(TimeUtils.longToString(System.currentTimeMillis(), "yyyy-MM-dd"))

        TipHelper.showProgressDialog(requireActivity(), R.string.loading_data, false)
        for (i in cityModes.indices) {
            reloadWeather(i)
        }

        setSignedIn(signed)
    }

    private fun reloadWeather(index: Int) {
        mHandler.post {
            val cityMode = cityModes[index]
            if (cityMode.poiId.isNotEmpty()) {
                val model = WeatherSpHelper.getWeatherModel(cityMode.poiId)
                if (fragmentList.size > index) {
                    val fragment = fragmentList[index] as WeatherFragment
                    if (model != null) {
                        fragment.changeWeather(model, cityMode)
                    }
                    if (index == currIndex) {
                        changeBg(model)
                    }
                }
            }
        }
    }

    private fun changeBg(model: WeatherModel?) {
        mHandler.post {
            if (model != null && model.dailies != null) {
                TipHelper.dismissProgressDialog(1000)

                val daily = model.dailies[0]
                var night = false
                if (daily != null) {
                    val tSunrise = TimeUtils.switchTime(daily.sunRise)
                    val tSunset = TimeUtils.switchTime(daily.sunSet)
                    val current = System.currentTimeMillis()

                    night = current < tSunrise || current > tSunset
                }
                // 天气图标
                binding.imgBackground.setImageResource(WeatherUtils.bgImageRes(model.getIconCd(), night))
            } else {
                binding.imgBackground.setImageResource(R.drawable.bg_normal)
                mHandler.postDelayed({ TipHelper.dismissProgressDialog() }, 2000)
            }
        }
    }

    private fun configRgIndicator(pagerSize: Int) {
        binding.indicatorCity.removeAllViews()

        val width = SizeUtils.dp2px(5f)
        val height = SizeUtils.dp2px(5f)
        val margin = SizeUtils.dp2px(6f)
        val layoutParams = RadioGroup.LayoutParams(width, height)
        
        for (i in 0 until pagerSize) {
            val tempButton = RadioButton(requireActivity())
            tempButton.isEnabled = false
            tempButton.isChecked = false
            tempButton.setBackgroundResource(R.drawable.sl_indicator_white)
            tempButton.buttonDrawable = null
            if (i > 0) {
                layoutParams.leftMargin = margin
            }
            binding.indicatorCity.addView(tempButton, layoutParams)
        }
        setIndicator(currIndex)
    }

    private fun setIndicator(position: Int) {
        if (position >= 0 && position < binding.indicatorCity.childCount) {
            binding.indicatorCity.check(binding.indicatorCity.getChildAt(position).id)

            val cityMode = cityModes[position]
            setAddress(cityMode)
            val weatherModel = WeatherSpHelper.getWeatherModel(cityMode.poiId)
            changeBg(weatherModel)

            if (fragmentList.size > position) {
                val fragment = fragmentList[position] as WeatherFragment
                if (weatherModel != null) {
                    fragment.changeWeather(weatherModel, cityMode)
                }
            }
        }
    }

    private fun setAddress(cityMode: CityMode) {
        binding.imgLocation.visibility = if (cityMode.location) View.VISIBLE else View.GONE
        binding.tvAddress.text = cityMode.getMergerName()
    }

    private fun setSignedIn(signedIn: Boolean) {
        binding.viewSignTip.visibility = if (signedIn) View.GONE else View.VISIBLE
    }

    private fun onSignClick() {
        val cityMode = cityModes[currIndex]
        var weatherModel: WeatherModel? = null
        if (cityMode.poiId.isNotEmpty()) {
            weatherModel = WeatherSpHelper.getWeatherModel(cityMode.poiId)
        }
        if (weatherModel != null) {
            SigningActivity.redirectTo(
                requireActivity(),
                weatherModel.hourlies[0],
                cityMode,
                0
            )
        }
    }

    private fun fetchNewVersion(listener: CompletionListener) {
        // 这里应该调用版本检查的API
        // 暂时简化实现
        listener.onCompletion()
    }

    //<editor-fold desc="插屏广告">
    private var mTTFullScreenVideoAd: TTFullScreenVideoAd? = null
    private var mLoadSuccess = false //是否加载成功
    private var mIsLoadedAndShow = true //广告加载成功并展示

    private var adNativeLoader: TTAdNative? = null

    /**
     * 展示广告
     */
    private fun showAd() {
        Log.e(TAG, "showAd: ++++++++++")
        mLoadSuccess = false
//        if (mAdInterstitialFullManager != null) {
//            mAdInterstitialFullManager.loadAdWithCallback(BuildConfig.PGE_INT_POS_ID);
//        }
    }

    private fun initAdLoader() {
        mLoadSuccess = false
        adNativeLoader = TTAdSdk.getAdManager().createAdNative(requireActivity())
        val adSlot = AdSlot.Builder()
            .setCodeId(BuildConfig.PGE_INT_POS_ID)
            .setOrientation(TTAdConstant.ORIENTATION_VERTICAL) //设置横竖屏方向
            .setMediationAdSlot(
                MediationAdSlot.Builder()
                    .setMuted(true) //是否静音
                    .setVolume(0.7f) //设置音量
                    .setBidNotify(true) //竞价结果通知
                    .build()
            )
            .build()

        adNativeLoader!!.loadFullScreenVideoAd(
            adSlot,
            object : TTAdNative.FullScreenVideoAdListener {
                override fun onError(code: Int, message: String?) {
                    mLoadSuccess = false
                    Log.d("TAG", "InterstitialFull onError code = " + code + " msg = " + message)
                }

                override fun onFullScreenVideoAdLoad(ad: TTFullScreenVideoAd) {
                    Log.d("TAG", "InterstitialFull onFullScreenVideoLoaded")
                    mLoadSuccess = true
                    mTTFullScreenVideoAd = ad
                }

                override fun onFullScreenVideoCached() {
                    Log.d("TAG", "InterstitialFull onFullScreenVideoCached")
                }

                override fun onFullScreenVideoCached(ad: TTFullScreenVideoAd) {
                    Log.d("TAG", "InterstitialFull onFullScreenVideoCached")
                    mLoadSuccess = true
                    mTTFullScreenVideoAd = ad
                    if (mIsLoadedAndShow && isCurrent) {
                        lifecycleScope.launch { showInterFullAd() }
                        SpUtils.getInstance()
                            .putLong(BuildConfig.PGE_INT_POS_ID, System.currentTimeMillis())
                    }
                }
            })
    }

    /**
     * 展示广告
     */
    private fun showInterFullAd() {
        if (mIsLoadedAndShow && mLoadSuccess) {
            // 展示广告

            this.mTTFullScreenVideoAd!!.setFullScreenVideoAdInteractionListener(object :
                TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {
                override fun onAdShow() {
                    Log.d("TAG", "InterstitialFull onAdShow")
                }

                override fun onAdVideoBarClick() {
                    Log.d("TAG", "InterstitialFull onAdVideoBarClick")
                }

                override fun onAdClose() {
                    Log.d("TAG", "InterstitialFull onAdClose")
                }

                override fun onVideoComplete() {
                    Log.d("TAG", "InterstitialFull onVideoComplete")
                }

                override fun onSkippedVideo() {
                    Log.d("TAG", "InterstitialFull onSkippedVideo")
                }
            })
            this.mTTFullScreenVideoAd!!.showFullScreenVideoAd(requireActivity())
            mIsLoadedAndShow = false
        } else {
            // TToast.show(this, "请先加载广告");
        }
    }
    //</editor-fold>
} 