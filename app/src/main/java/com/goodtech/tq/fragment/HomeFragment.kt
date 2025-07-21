package com.goodtech.tq.fragment

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.goodtech.tq.R
import com.goodtech.tq.activity.BaseActivity
import com.goodtech.tq.activity.SettingActivity
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
import com.goodtech.tq.utils.ImageUtils
import com.goodtech.tq.utils.IntentReceiver
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.utils.TimeUtils
import com.goodtech.tq.utils.TipHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlinx.coroutines.*

/**
 * 主页面 Fragment
 * 负责显示天气信息和城市管理
 */
class HomeFragment : BaseFragment() {

    companion object {
        private const val TAG = "HomeFragment"
        private const val RETRY_DELAY_MS = 100L
        private const val PROGRESS_DISMISS_DELAY_MS = 1000L
        private const val PROGRESS_DISMISS_FALLBACK_MS = 2000L
        private const val INDICATOR_WIDTH_DP = 5f
        private const val INDICATOR_HEIGHT_DP = 5f
        private const val INDICATOR_MARGIN_DP = 6f
    }

    // UI 相关
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    // 生命周期管理
    private val mainHandler = Handler(Looper.getMainLooper())
    private val receiver: BroadcastReceiver = IntentReceiver()
    
    // 数据管理
    private val fragmentList = mutableListOf<WeatherFragment>()
    private var cityModes = mutableListOf<CityMode>()
    private var currentIndex = 0
    private var isLoadLast = false
    private var isSigned = false
    private var isFirstLoad = true
    private var isCurrent = false
    private var isNeedReload = true
    private var isFragmentManagerBusy = false

    // 协程作用域
    private val fragmentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * 检查 Fragment 是否处于安全状态
     */
    private fun isFragmentSafe(): Boolean {
        return isAdded && !isDetached && activity != null && !isFragmentManagerBusy
    }

    /**
     * 安全地执行 Fragment 操作
     */
    private fun safeExecuteFragmentOperation(operation: () -> Unit) {
        if (!isFragmentSafe()) {
            Log.w(TAG, "Fragment not in safe state, skipping operation")
            return
        }
        
        try {
            isFragmentManagerBusy = true
            operation()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "FragmentManager transaction conflict: ${e.message}")
            // 延迟重试
            mainHandler.postDelayed({
                try {
                    if (isFragmentSafe()) {
                        operation()
                    }
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to execute operation after retry: ${e2.message}")
                } finally {
                    isFragmentManagerBusy = false
                }
            }, RETRY_DELAY_MS)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing fragment operation: ${e.message}")
        } finally {
            isFragmentManagerBusy = false
        }
    }

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
        initializeViews()
    }

    /**
     * 初始化视图
     */
    private fun initializeViews() {
        setupStatusBar()
        setupClickListeners()
        setupViewPager()
        registerEventBus()
    }

    /**
     * 设置状态栏
     */
    private fun setupStatusBar() {
        val statusBar = binding.privateStationBar
        val layoutParams = statusBar.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        layoutParams.height += BarUtils.getStatusBarHeight()
        statusBar.layoutParams = layoutParams
    }

    /**
     * 设置点击监听器
     */
    private fun setupClickListeners() {
        binding.layoutAddress.setOnClickListener { handleAddressClick() }
        binding.imgSetting.setOnClickListener { handleSettingClick() }
        binding.imgSign.setOnClickListener { handleSignClick() }
    }

    /**
     * 处理地址点击事件
     */
    private fun handleAddressClick() {
        if (!hasPermission()) {
            showPermissionDialog { navigateToCityList() }
            return
        }
        navigateToCityList()
    }

    /**
     * 处理设置点击事件
     */
    private fun handleSettingClick() {
        if (!hasPermission()) {
            showPermissionDialog { navigateToSettings() }
            return
        }
        navigateToSettings()
    }

    /**
     * 处理签到点击事件
     */
    private fun handleSignClick() {
        if (!hasPermission()) {
            showPermissionDialog { handleSignClick() }
            return
        }
        
        val cityMode = cityModes.getOrNull(currentIndex) ?: return
        val weatherModel = getWeatherModel(cityMode.getPoiId()) ?: return
        
        SigningActivity.redirectTo(
            requireActivity(),
            weatherModel.hourlies[0],
            cityMode,
            0
        )
    }

    /**
     * 检查权限
     */
    private fun hasPermission(): Boolean = SpUtils.getInstance().isAgreePermission()

    /**
     * 显示权限对话框
     */
    private fun showPermissionDialog(onGranted: () -> Unit) {
        (requireActivity() as? BaseActivity)?.showPermissionDialog(requireActivity(), onGranted)
    }

    /**
     * 导航到城市列表
     */
    private fun navigateToCityList() {
        CityListActivity.redirectTo(requireActivity())
    }

    /**
     * 导航到设置页面
     */
    private fun navigateToSettings() {
        startActivity(Intent(requireActivity(), SettingActivity::class.java))
    }

    /**
     * 设置 ViewPager
     */
    private fun setupViewPager() {
        val fragment = createWeatherFragment()
        fragmentList.add(fragment)
        
        val adapter = ViewPagerAdapter(requireActivity(), fragmentList)
        binding.viewPager.adapter = adapter
        
        binding.viewPager.registerOnPageChangeCallback(createPageChangeCallback())
    }

    /**
     * 创建天气 Fragment
     */
    private fun createWeatherFragment(): WeatherFragment {
        return WeatherFragment().apply {
            setStateBar(binding.stationBgView)
        }
    }

    /**
     * 创建页面切换回调
     */
    private fun createPageChangeCallback() = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (currentIndex != position) {
                currentIndex = position
                updateIndicator(position)
            }
        }
    }

    /**
     * 注册事件总线
     */
    private fun registerEventBus() {
        EventBus.getDefault().register(this)
        
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireActivity().registerReceiver(receiver, filter)
    }

    override fun onStart() {
        super.onStart()
        isCurrent = true
        handleFirstLoad()
        handleLocation()
        handleCityModesUpdate()
        scheduleViewReload()
    }

    /**
     * 处理首次加载
     */
    private fun handleFirstLoad() {
        if (isFirstLoad) {
            if (hasPermission()) {
                checkNewVersion()
            }
            isFirstLoad = false
        }
    }

    /**
     * 处理定位
     */
    private fun handleLocation() {
        if (LocationSpHelper.getLocation() != null && hasPermission()) {
            LocationHelper.getInstance().startWithDelay(requireActivity())
        }
    }

    /**
     * 处理城市模式更新
     */
    private fun handleCityModesUpdate() {
        val cityModesFromSp = LocationSpHelper.getCityListAndLocation()
        if (cityModesFromSp.size != cityModes.size || isNeedReload) {
            cityModes = cityModesFromSp.toMutableList()
            isNeedReload = true
        }
    }

    /**
     * 调度视图重载
     */
    private fun scheduleViewReload() {
        mainHandler.post {
            safeExecuteFragmentOperation {
                reloadView()
                binding.viewPager.currentItem = currentIndex
                updateIndicator(currentIndex)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isCurrent = true
    }

    override fun onPause() {
        super.onPause()
        isCurrent = false
    }

    override fun onStop() {
        if (hasPermission()) {
            LocationHelper.getInstance().stop()
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanup()
    }

    /**
     * 清理资源
     */
    private fun cleanup() {
        EventBus.getDefault().unregister(this)
        requireActivity().unregisterReceiver(receiver)
        fragmentScope.cancel()
        _binding = null
    }

    /**
     * 重载视图
     */
    private fun reloadView() {
        if (!isNeedReload || !isFragmentSafe()) {
            return
        }

        isNeedReload = false
        setupIndicator(cityModes.size)
        reloadFragment()
        updateCurrentIndex()
        reloadWeathers()
    }

    /**
     * 更新当前索引
     */
    private fun updateCurrentIndex() {
        currentIndex = if (isLoadLast) {
            fragmentList.size - 1
        } else {
            minOf(currentIndex, fragmentList.size - 1)
        }
        isLoadLast = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CityEvent) {
        handleCityEvent(event)
    }

    /**
     * 处理城市事件
     */
    private fun handleCityEvent(event: CityEvent) {
        if (event.showIndex() >= 0) {
            currentIndex = event.showIndex()
        }
        if (event.isAddCity()) {
            isLoadLast = true
            isNeedReload = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        Log.d(TAG, "Received MessageEvent")
        TipHelper.dismissProgressDialog()

        when {
            event.loadIntAd -> handleAdEvent()
            event.isSuccessLocation() -> handleLocationSuccess()
            event.isNeedReload -> isNeedReload = true
            event.getFetchCId().isNotEmpty() -> handleWeatherUpdate(event.getFetchCId())
        }
    }

    /**
     * 处理广告事件
     */
    private fun handleAdEvent() {
        // 广告相关逻辑
    }

    /**
     * 处理定位成功
     */
    private fun handleLocationSuccess() {
        val cityMode = LocationSpHelper.getLocation()
        if (cityMode != null && cityModes.isNotEmpty()) {
            cityModes[0] = cityMode
            reloadWeather(0)
        }
    }

    /**
     * 处理天气更新
     */
    private fun handleWeatherUpdate(cityId: String) {
        cityModes.forEachIndexed { index, cityMode ->
            if (cityMode.getPoiId() == cityId) {
                reloadWeather(index)
                return
            }
        }
    }

    /**
     * 重载 Fragment
     */
    private fun reloadFragment() {
        if (fragmentList.size == cityModes.size) return

        adjustFragmentList()
        safeExecuteFragmentOperation {
            val adapter = binding.viewPager.adapter as? ViewPagerAdapter
            adapter?.replaceAll(fragmentList)
        }
    }

    /**
     * 调整 Fragment 列表
     */
    private fun adjustFragmentList() {
        while (fragmentList.size > cityModes.size) {
            fragmentList.removeAt(fragmentList.size - 1)
        }
        while (fragmentList.size < cityModes.size) {
            fragmentList.add(createWeatherFragment())
        }
    }

    /**
     * 重载天气信息
     */
    private fun reloadWeathers() {
        updateSignStatus()
        showLoadingDialog()
        reloadAllWeathers()
        updateSignUI()
    }

    /**
     * 更新签到状态
     */
    private fun updateSignStatus() {
        val signDbHelper = SignDbHelper(requireActivity())
        val today = TimeUtils.longToString(System.currentTimeMillis(), "yyyy-MM-dd")
        isSigned = signDbHelper.hadSigning(today)
    }

    /**
     * 显示加载对话框
     */
    private fun showLoadingDialog() {
        TipHelper.showProgressDialog(requireActivity(), R.string.loading_data, false)
    }

    /**
     * 重载所有天气
     */
    private fun reloadAllWeathers() {
        cityModes.forEachIndexed { index, _ ->
            reloadWeather(index)
        }
    }

    /**
     * 更新签到 UI
     */
    private fun updateSignUI() {
        setSignedIn(isSigned)
    }

    /**
     * 重载指定索引的天气
     */
    private fun reloadWeather(index: Int) {
        mainHandler.post {
            val cityMode = cityModes.getOrNull(index) ?: return@post
            if (cityMode.getPoiId().isEmpty()) return@post

            val weatherModel = getWeatherModel(cityMode.getPoiId())
            val fragment = fragmentList.getOrNull(index) ?: return@post

            weatherModel?.let { fragment.changeWeather(it, cityMode) }
            
            if (index == currentIndex) {
                updateBackground(weatherModel)
            }
        }
    }

    /**
     * 获取天气模型
     */
    private fun getWeatherModel(cityId: String): WeatherModel? {
        return WeatherSpHelper.getWeatherModel(cityId)
    }

    /**
     * 更新背景
     */
    private fun updateBackground(model: WeatherModel?) {
        mainHandler.post {
            if (model?.dailies != null) {
                TipHelper.dismissProgressDialog(PROGRESS_DISMISS_DELAY_MS)
                val isNight = calculateIsNight(model.dailies[0])
                binding.imgBackground.setImageResource(ImageUtils.bgImageRes(model.getIconCd(), isNight))
            } else {
                binding.imgBackground.setImageResource(R.drawable.bg_normal)
                mainHandler.postDelayed({ TipHelper.dismissProgressDialog() }, PROGRESS_DISMISS_FALLBACK_MS)
            }
        }
    }

    /**
     * 计算是否为夜晚
     */
    private fun calculateIsNight(daily: Any?): Boolean {
        if (daily == null) return false
        
        val sunrise = TimeUtils.switchTime(daily.sunRise)
        val sunset = TimeUtils.switchTime(daily.sunSet)
        val current = System.currentTimeMillis()
        
        return current < sunrise || current > sunset
    }

    /**
     * 设置指示器
     */
    private fun setupIndicator(pagerSize: Int) {
        binding.indicatorCity.removeAllViews()

        val width = SizeUtils.dp2px(INDICATOR_WIDTH_DP)
        val height = SizeUtils.dp2px(INDICATOR_HEIGHT_DP)
        val margin = SizeUtils.dp2px(INDICATOR_MARGIN_DP)
        val layoutParams = RadioGroup.LayoutParams(width, height)
        
        repeat(pagerSize) { index ->
            val button = createIndicatorButton()
            if (index > 0) {
                layoutParams.leftMargin = margin
            }
            binding.indicatorCity.addView(button, layoutParams)
        }
        
        updateIndicator(currentIndex)
    }

    /**
     * 创建指示器按钮
     */
    private fun createIndicatorButton(): RadioButton {
        return RadioButton(requireActivity()).apply {
            isEnabled = false
            isChecked = false
            setBackgroundResource(R.drawable.sl_indicator_white)
            buttonDrawable = null
        }
    }

    /**
     * 更新指示器
     */
    private fun updateIndicator(position: Int) {
        if (position !in 0 until binding.indicatorCity.childCount) return

        binding.indicatorCity.check(binding.indicatorCity.getChildAt(position).id)

        val cityMode = cityModes.getOrNull(position) ?: return
        updateAddress(cityMode)
        
        val weatherModel = getWeatherModel(cityMode.getPoiId())
        updateBackground(weatherModel)

        val fragment = fragmentList.getOrNull(position)
        weatherModel?.let { fragment?.changeWeather(it, cityMode) }
    }

    /**
     * 更新地址显示
     */
    private fun updateAddress(cityMode: CityMode) {
        binding.imgLocation.visibility = if (cityMode.getLocation()) View.VISIBLE else View.GONE
        binding.tvAddress.text = cityMode.getMergerName()
    }

    /**
     * 设置签到状态
     */
    private fun setSignedIn(signedIn: Boolean) {
        binding.viewSignTip.visibility = if (signedIn) View.GONE else View.VISIBLE
    }

    /**
     * 检查新版本
     */
    private fun checkNewVersion() {
        fragmentScope.launch {
            try {
                fetchNewVersion { }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking new version: ${e.message}")
            }
        }
    }

    /**
     * 获取新版本信息
     */
    private fun fetchNewVersion(listener: CompletionListener) {
        // 这里应该调用版本检查的API
        // 暂时简化实现
        listener.onCompletion()
    }
} 