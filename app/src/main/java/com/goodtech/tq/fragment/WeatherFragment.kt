package com.goodtech.tq.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.dp.DPSdk
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.goodtech.tq.BuildConfig
import com.goodtech.tq.R
import com.goodtech.tq.activity.BaseActivity
import com.goodtech.tq.ad.AdFeedFragment
import com.goodtech.tq.ad.AdManager
import com.goodtech.tq.adapter.WeatherAdapter
import com.goodtech.tq.app.App
import com.goodtech.tq.base.callback.DataCallback
import com.goodtech.tq.helpers.BtnLinkHelper
import com.goodtech.tq.helpers.WeatherSpHelper
import com.goodtech.tq.httpClient.WeatherHttpHelper
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.location.helper.LocationHelper
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.JuheAlarmModel
import com.goodtech.tq.models.LifeItemBean
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.others.airQuality.AirQualityActivity
import com.goodtech.tq.modules.others.calendar.CalendarActivity
import com.goodtech.tq.modules.others.constellation.ConstellationActivity
import com.goodtech.tq.modules.others.muyu.MuyuActivity
import com.goodtech.tq.modules.others.taifeng.TyphoonActivity
import com.goodtech.tq.modules.others.test.MyTestActivity
import com.goodtech.tq.modules.signing.SigningActivity
import com.goodtech.tq.modules.video.DrawVideoFullScreenActivity
import com.goodtech.tq.modules.video.djx.DrawDramaActivity
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.views.popup.AlarmPopup
import com.goodtech.tq.views.popup.LifeDetailsPopup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lxj.xpopup.XPopup
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherFragment : AdFeedFragment(), OnRefreshListener, WeatherHeaderListener {

    private val TAG = "WeatherFragment"
    private lateinit var mRefreshLayout: SmartRefreshLayout
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: WeatherAdapter
    private var mWeatherModel: WeatherModel? = null
    private var mStateBarBg: View? = null
    private var mCityMode: CityMode? = null
    private var mHadLoad = false

    // Ad related
    private var mLoadSuccess = false
    private var mLoadSuccess2 = false
    private var mBannerAd: TTNativeExpressAd? = null
    private var mGMNativeAd: TTFeedAd? = null
    private var mGMNativeAd2: TTFeedAd? = null
    private var mGMNativeAd3: TTFeedAd? = null
    private var isFirstAdLoaded = false  // 第一个广告是否已加载
    private var isSecondAdLoaded = false // 第二个广告是否已加载
    private var isThirdAdLoaded = false // 第三个广告是否已加载
    private var isAd0Loaded = false // Ad0是否已加载
    private var isFirstAdLoading = false
    private var isSecondAdLoading = false
    private var isThirdAdLoading = false
    private var isAd0Loading = false

    override fun getViewLayoutRes(): Int = R.layout.fragment_weather

    override fun setupCacheViews() {
        super.setupCacheViews()
        mRefreshLayout = mCacheView as SmartRefreshLayout
        mRecyclerView = mCacheView.findViewById(R.id.recycler_view)

        // 设置RecyclerView的缓存策略
        mRecyclerView.setItemViewCacheSize(11) // 设置较大的缓存大小
        mRecyclerView.recycledViewPool.setMaxRecycledViews(0, 0) // 禁用视图回收池
        mRecyclerView.setHasFixedSize(true) // 固定大小，避免重新测量

        mAdapter = WeatherAdapter()
        mAdapter.setWeatherHeaderListener(this)
        mAdapter.setAdLoadCallback(object : WeatherAdapter.AdLoadCallback {
            override fun onAdLoaded(position: Int) {
                when (position) {
                    0 -> {
//                        if (!isAd0Loaded && SpUtils.getInstance().isAgreePermission()) {
//                            Thread { loadBannerAd() }.start()
//                        }
                    }

                    4 -> {
                        if (!isFirstAdLoaded && SpUtils.getInstance().isAgreePermission()) {
                            mHandler.post { loadFirstAd() }
                        }
                    }

                    6 -> {
                        if (!isSecondAdLoaded && SpUtils.getInstance().isAgreePermission()) {
                            mHandler.post { loadSecondAd() }
                        }
                    }

                    9 -> {
                        if (!isThirdAdLoaded && SpUtils.getInstance().isAgreePermission()) {
                            mHandler.post { loadThirdAd() }
                        }
                    }
                }
            }
        })
        mRecyclerView.adapter = mAdapter

        // 添加间距
        mRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position == 0) {
                    // 第一个item的top间距为60dp
                    outRect.top = SizeUtils.dp2px(85f)
                    outRect.left = SizeUtils.dp2px(0f)
                    outRect.right = SizeUtils.dp2px(0f)
                } else {
                    // 其他item的间距为5dp
                    outRect.top = SizeUtils.dp2px(5f)
                    outRect.left = SizeUtils.dp2px(10f)
                    outRect.right = SizeUtils.dp2px(10f)
                }
                outRect.bottom = SizeUtils.dp2px(5f)
            }
        })

        initView()
    }

    fun setStateBar(stateBar: View) {
        mStateBarBg = stateBar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRefreshLayout.setOnRefreshListener(this)

        mRecyclerView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            mStateBarBg?.let { stateBar ->
                when {
                    scrollY <= stateBar.height && scrollY > 10 -> {
                        val alpha = scrollY.toFloat() / stateBar.height
                        stateBar.alpha = alpha
                    }

                    scrollY > stateBar.height -> stateBar.alpha = 1f
                    else -> stateBar.alpha = 0f
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val cityMode = mCityMode
            val model = if (mWeatherModel == null && cityMode != null) {
                WeatherSpHelper.getWeatherModel(cityMode.poiId)
            } else {
                mWeatherModel
            }
            val modelWithAlarm = model?.apply { attachAlarm(this) }
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                if (modelWithAlarm != null) {
                    mWeatherModel = modelWithAlarm
                    mAdapter.setData(modelWithAlarm, mCityMode)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        
        // 检查去广告状态，如果有效则移除已加载的广告
        if (com.goodtech.tq.utils.AdRemovalManager.isAdRemovalActive()) {
            removeAllAds()
        }
        if (isFirstLoad) {
            isFirstLoad = false
            if (SpUtils.getInstance().isAgreePermission()) {
                mHandler.postDelayed({
                    initAdLoader()
                }, 500)
            }
        }
        if (!mHadLoad) {
            mHadLoad = true
            updateData()
        }
    }

    private fun initView() {
        if (SpUtils.getInstance().isAgreePermission()) {
            initNativeExpressAD()
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        // 当前页是定位城市时，下拉需要先强制刷新定位
        if (mCityMode?.location == true && isAdded) {
            activity?.let { LocationHelper.getInstance().startWithDelay(it, true) }
        }
        val fetching =
            WeatherHttpHelper.getInstance().fetchWeather(mCityMode, true) { success, weather, errCode ->
                mHandler.post {
                    if (weather != null && mCityMode != null) {
                        changeWeather(weather, mCityMode!!)
                    }
                    refreshLayout.finishRefresh()
                }
            }

        if (!fetching) {
            mHandler.postDelayed({ refreshLayout.finishRefresh() }, 300)
        }
    }

    fun changeWeather(model: WeatherModel?, cityMode: CityMode) {
        mCityMode = cityMode
        if (model != null) {
            mWeatherModel = model
        }
        updateData()
    }

    private val adWidth = SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat()) - 20

    private fun updateData() {
        if (mHadLoad) {
            val cityMode = mCityMode ?: return
            lifecycleScope.launch(Dispatchers.IO) {
                val effectiveModel = WeatherSpHelper.getWeatherModel(cityMode.poiId) ?: mWeatherModel
                val modelWithAlarm = effectiveModel?.apply { attachAlarm(this) }
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    if (mCityMode?.poiId != cityMode.poiId) return@withContext
                    when {
                        modelWithAlarm != null -> {
                            mWeatherModel = modelWithAlarm
                            mAdapter.setData(modelWithAlarm, cityMode)
                        }
                        mWeatherModel != null -> {
                            // 定位城市 poiId 常为固定 cid，缓存未写入前仍用旧 Model，但必须刷新 CityMode（名称/坐标）
                            mAdapter.setData(mWeatherModel, cityMode)
                        }
                        else -> {
                            mAdapter.setData(null, cityMode)
                        }
                    }
                }
            }
        }
    }

    private fun attachAlarm(model: WeatherModel) {
        val alarm = WeatherSpHelper.getAlarm(model.poiId) ?: return
        val list = Gson().fromJson<ArrayList<JuheAlarmModel?>?>(
            alarm,
            object : TypeToken<ArrayList<JuheAlarmModel?>?>() {}.getType()
        )
        if (!list.isNullOrEmpty()) {
            model.alarmModel = list[0]
        }
    }

    private fun loadBannerAd() {
        if (isAd0Loaded || isAd0Loading) return
        
        // 检查是否在去广告有效期内
        if (com.goodtech.tq.utils.AdRemovalManager.isAdRemovalActive()) {
            return
        }
        isAd0Loading = true

        val height = 0 // 设置一个合适的banner高度

        AdManager.getInstance().loadExpressAd(
            requireActivity(),
            BuildConfig.PGE_HOME_BANNER_POS_ID,
            adWidth,
            height,
            object : DataCallback<TTNativeExpressAd> {
                override fun onComplete(data: TTNativeExpressAd?, errorMsg: String?) {
                    isAd0Loading = false
                    if (data != null) {
                        mHandler.post {
                            mBannerAd = data
                            mAdapter.setAd0(data)
                            isAd0Loaded = true
                        }
                    }
                }
            }
        )
    }

    private fun loadFirstAd() {
        if (isFirstAdLoaded || isFirstAdLoading) return
        
        // 检查是否在去广告有效期内
        if (com.goodtech.tq.utils.AdRemovalManager.isAdRemovalActive()) {
            return
        }
        isFirstAdLoading = true

        loadFeedAd(BuildConfig.PGE_EXPRESS_POS_ID, adWidth, object : DataCallback<TTFeedAd> {
            override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
                isFirstAdLoading = false
                if (data != null) {
                    mHandler.post {
                        mGMNativeAd = data
                        mAdapter.setAd1(data)
                        isFirstAdLoaded = true
                    }
                }
            }
        })
    }

    private fun loadSecondAd() {
        if (isSecondAdLoaded || isSecondAdLoading) return
        
        // 检查是否在去广告有效期内
        if (com.goodtech.tq.utils.AdRemovalManager.isAdRemovalActive()) {
            return
        }
        isSecondAdLoading = true

        loadFeedAd(BuildConfig.PGE_EXPRESS_POS_ID2, adWidth, object : DataCallback<TTFeedAd> {
            override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
                isSecondAdLoading = false
                if (data != null) {
                    mHandler.post {
                        mGMNativeAd2 = data
                        mAdapter.setAd2(data)
                        isSecondAdLoaded = true
                    }
                }
            }
        })
    }

    private fun loadThirdAd() {
        if (isThirdAdLoaded || isThirdAdLoading) return
        
        // 检查是否在去广告有效期内
        if (com.goodtech.tq.utils.AdRemovalManager.isAdRemovalActive()) {
            return
        }
        isThirdAdLoading = true

        loadFeedAd(BuildConfig.PGE_EXPRESS_POS_ID3, adWidth, object : DataCallback<TTFeedAd> {
            override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
                isThirdAdLoading = false
                if (data != null) {
                    mHandler.post {
                        mGMNativeAd3 = data
                        mAdapter.setAd3(data)
                        isThirdAdLoaded = true
                    }
                }
            }
        })
    }

    private fun initNativeExpressAD() {
        mLoadSuccess = false
        mLoadSuccess2 = false
        isFirstAdLoaded = false
        isSecondAdLoaded = false
        isThirdAdLoaded = false
        isAd0Loaded = false
        isFirstAdLoading = false
        isSecondAdLoading = false
        isThirdAdLoading = false
        isAd0Loading = false
        Log.e(TAG, "initNativeExpressAD: ++++ ${System.currentTimeMillis()}")
    }

    private fun initAdLoader() {
        mLoadSuccess = false
        mLoadSuccess2 = false
        isFirstAdLoaded = false
        isSecondAdLoaded = false
        isThirdAdLoaded = false
        isAd0Loaded = false
        isFirstAdLoading = false
        isSecondAdLoading = false
        isThirdAdLoading = false
        isAd0Loading = false
        Log.e(TAG, "initAdLoader: ++++ ${System.currentTimeMillis()}")
        if (SpUtils.getInstance().isAgreePermission()) {
            initNativeExpressAD()
        }
    }

    private fun removeAdView(ad: TTFeedAd?) {
        ad?.let {
            try {
                it.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "removeAdView error: ${e.message}")
            }
        }
    }
    
    /**
     * 如果需要，移除所有已加载的广告（公开方法，供外部调用）
     * 仅在 mAdapter 已初始化时执行，避免 ViewPager 中未创建视图的 Fragment 崩溃
     */
    fun removeAllAdsIfNeeded() {
        if (!::mAdapter.isInitialized) return
        if (com.goodtech.tq.utils.AdRemovalManager.isAdRemovalActive()) {
            removeAllAds()
        }
    }
    
    /**
     * 移除所有已加载的广告
     */
    private fun removeAllAds() {
        if (!::mAdapter.isInitialized) return
        // 移除 Banner 广告
        mBannerAd?.let {
            try {
                it.expressAdView?.let { adView ->
                    (adView.parent as? ViewGroup)?.removeView(adView)
                }
                it.destroy()
                mBannerAd = null
                mAdapter.setAd0(null)
                isAd0Loaded = false
                isAd0Loading = false
            } catch (e: Exception) {
                Log.e(TAG, "removeBannerAd error: ${e.message}")
            }
        }
        
        // 移除信息流广告
        removeAdView(mGMNativeAd)
        mGMNativeAd = null
        mAdapter.setAd1(null)
        isFirstAdLoaded = false
        isFirstAdLoading = false
        
        removeAdView(mGMNativeAd2)
        mGMNativeAd2 = null
        mAdapter.setAd2(null)
        isSecondAdLoaded = false
        isSecondAdLoading = false
        
        removeAdView(mGMNativeAd3)
        mGMNativeAd3 = null
        mAdapter.setAd3(null)
        isThirdAdLoaded = false
        isThirdAdLoading = false
        
        // 刷新适配器
        mAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBannerAd?.let {
            try {
                it.expressAdView?.let { adView ->
                    (adView.parent as? ViewGroup)?.removeView(adView)
                }
                it.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "removeAdView error: ${e.message}")
            }
        }
        removeAdView(mGMNativeAd)
        removeAdView(mGMNativeAd2)
        removeAdView(mGMNativeAd3)
        isFirstAdLoaded = false
        isSecondAdLoaded = false
        isThirdAdLoaded = false
        isAd0Loaded = false
        isFirstAdLoading = false
        isSecondAdLoading = false
        isThirdAdLoading = false
        isAd0Loading = false
    }

    // WeatherHeaderListener implementations
    override fun onTyphoon() {
        activity?.let {
            startActivity(Intent(it, TyphoonActivity::class.java))
        }
    }

    override fun onAirQuality() {
        activity?.let {
            mWeatherModel?.aqi?.let { it1 -> AirQualityActivity.redirectTo(it, mCityMode, it1) }
        }
    }

    override fun onCalendar() {
        activity?.let {
            startActivity(Intent(it, CalendarActivity::class.java))
        }
    }

    override fun onFortune() {
        activity?.let {
            startActivity(Intent(it, ConstellationActivity::class.java))
        }
    }

    override fun onSignIn() {
        activity?.let {
            if (!SpUtils.getInstance().isAgreePermission()) {
                (it as BaseActivity).showPermissionDialog(it) {
                    SigningActivity.redirectTo(
                        requireActivity(),
                        mWeatherModel?.hourlies?.get(0),
                        mCityMode,
                        0
                    )
                }
                return
            }
            SigningActivity.redirectTo(it, mWeatherModel?.hourlies?.get(0), mCityMode, 0)
        }
    }

    override fun onMuyu() {
        activity?.let {
            startActivity(Intent(it, MuyuActivity::class.java))
        }
    }

    override fun onTaxi() {
        var link = "https://kzurl10.cn/Z9Nks"
        var title = "免费打车券"
        BtnLinkHelper.getBtnLink(0)?.let {
            link = it.h5link
            title = it.tempType
        }
        MyTestActivity.redirectTo(requireActivity(), link, title, "DaChe")
    }

    override fun onMeituan() {
        var link = "https://kurl04.cn/ZRSxc"
        var title = "美团大额券"
        BtnLinkHelper.getBtnLink(1)?.let {
            link = it.h5link
            title = it.tempType
        }
        MyTestActivity.redirectTo(requireActivity(), link, title, "MeiTuan")
    }

    override fun onEleme() {
        var link = "https://kzurl05.cn/ZRJjc"
        var title = "饿了么大红包"
        BtnLinkHelper.getBtnLink(2)?.let {
            link = it.h5link
            title = it.tempType
        }
        MyTestActivity.redirectTo(requireActivity(), link, title, "Eleme")
    }

    override fun onWarningBtn() {
        mWeatherModel?.alarmModel?.let {
            val popup = AlarmPopup(requireActivity())
            popup.setupData(it)
            XPopup.Builder(requireActivity())
                .isDestroyOnDismiss(true)
                .asCustom(popup)
                .show()
        }
    }

    override fun onShortPlayer() {
        if (!DJXSdk.isStartSuccess()) {
            App.instance.initDJX()
        }
        DrawDramaActivity.start(requireActivity())
    }

    override fun onMiniVideo() {
        if (!DPSdk.isStartSuccess()) {
            App.instance.initDP()
        }
        val intent = Intent(requireActivity(), DrawVideoFullScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onLifeItem(lifeItemBean: LifeItemBean?) {
        val popup = LifeDetailsPopup(requireActivity())
        popup.lifeDetails = lifeItemBean
        val entity = mWeatherModel?.lifeModel
        if (entity != null) {
            popup.lifeTitle = entity.lifeTitleWith(lifeItemBean)
            popup.lifeImgRes = entity.lifeImageWith(lifeItemBean)
        }
        mCityMode?.let { city ->
            popup.cityName = city.mergerName
        }
        mWeatherModel?.let { weather ->
            popup.observation = weather.observation
        }

        XPopup.Builder(requireActivity())
            .isDestroyOnDismiss(true)   //  只使用一次
            .asCustom(popup)
            .show()
    }

    override fun onPeriphery() {

    }
} 