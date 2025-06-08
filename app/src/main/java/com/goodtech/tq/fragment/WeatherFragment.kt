package com.goodtech.tq.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import com.goodtech.tq.adapter.WeatherAdapter
import com.goodtech.tq.app.App
import com.goodtech.tq.base.callback.DataCallback
import com.goodtech.tq.helpers.BtnLinkHelper
import com.goodtech.tq.helpers.WeatherSpHelper
import com.goodtech.tq.httpClient.WeatherHttpHelper
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.others.airQuality.AirQualityActivity
import com.goodtech.tq.modules.others.calendar.CalendarActivity
import com.goodtech.tq.modules.others.constellation.ConstellationActivity
import com.goodtech.tq.modules.others.taifeng.TyphoonActivity
import com.goodtech.tq.modules.others.test.MyTestActivity
import com.goodtech.tq.modules.signing.SigningActivity
import com.goodtech.tq.modules.video.DrawVideoFullScreenActivity
import com.goodtech.tq.modules.video.djx.DrawDramaActivity
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.views.popup.AlarmPopup
import com.lxj.xpopup.XPopup
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.goodtech.tq.ad.AdManager

class WeatherFragment : AdFeedFragment(), OnRefreshListener, WeatherHeaderListener {

    private val TAG = "WeatherFragment"
    private lateinit var mRefreshLayout: SmartRefreshLayout
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: WeatherAdapter
    private var mWeatherModel: WeatherModel? = null
    private var mStateBarBg: View? = null
    private var mCityMode: CityMode? = null
    private var mHadLoad = false
    private var isFirstLoad = true

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
                    4 -> {
                        if (!isFirstAdLoaded) {
                            Thread { loadFirstAd() }.start()
                        }
                    }
                    6 -> {
                        if (!isSecondAdLoaded) {
                            Thread { loadSecondAd() }.start()
                        }
                    }
                    9 -> {
                        if (!isThirdAdLoaded) {
                            Thread { loadThirdAd() }.start()
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
                } else {
                    // 其他item的间距为5dp
                    outRect.top = SizeUtils.dp2px(5f)
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
    }

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            isFirstLoad = false
            if (SpUtils.getInstance().isAgreePermission()) {
                mHandler.postDelayed({ 
                    Thread { 
                        initAdLoader()
                        loadBannerAd()
                    }.start() 
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
        val fetching = WeatherHttpHelper.getInstance().fetchWeather(mCityMode) { success, weather, errCode ->
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
        mWeatherModel = model
        mCityMode = cityMode
        updateData()
    }

    private val adWidth = SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat()) - 20

    private fun updateData() {
        if (mHadLoad) {
            mHandler.post {
                // 先尝试加载本地缓存数据
                if (mWeatherModel == null && mCityMode != null) {
                    val cachedModel = WeatherSpHelper.getWeatherModel(mCityMode!!.getPoiId())
                    if (cachedModel != null) {
                        mWeatherModel = cachedModel
                        mAdapter.setData(cachedModel, mCityMode)
                    }
                }
                
                // 如果有新数据则更新
                mWeatherModel?.let { model ->
                    mAdapter.setData(model, mCityMode)
                }
            }
        }
    }

    private fun loadBannerAd() {
        if (isAd0Loaded) return
        
        val width =  SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat()) - 20
        val height = 0 // 设置一个合适的banner高度

        AdManager.getInstance().loadExpressAd(
            requireActivity(),
            BuildConfig.PGE_HOME_BANNER_POS_ID,
            adWidth,
            height,
            object : DataCallback<TTNativeExpressAd> {
                override fun onComplete(data: TTNativeExpressAd?, errorMsg: String?) {
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
        if (isFirstAdLoaded) return
        
        loadFeedAd(BuildConfig.PGE_EXPRESS_POS_ID, adWidth, object : DataCallback<TTFeedAd> {
            override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
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
        if (isSecondAdLoaded) return
        
        loadFeedAd(BuildConfig.PGE_EXPRESS_POS_ID2, adWidth, object : DataCallback<TTFeedAd> {
            override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
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
        if (isThirdAdLoaded) return

        loadFeedAd(BuildConfig.PGE_EXPRESS_POS_ID3, adWidth, object : DataCallback<TTFeedAd> {
            override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
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
        Log.e(TAG, "initNativeExpressAD: ++++ ${System.currentTimeMillis()}")
    }

    private fun initAdLoader() {
        mLoadSuccess = false
        mLoadSuccess2 = false
        isFirstAdLoaded = false
        isSecondAdLoaded = false
        isThirdAdLoaded = false
        isAd0Loaded = false
        Log.e(TAG, "initAdLoader: ++++ ${System.currentTimeMillis()}")

        // 在后台线程中初始化广告加载器
        Thread {
            if (SpUtils.getInstance().isAgreePermission()) {
                initNativeExpressAD()
            }
        }.start()
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
                    SigningActivity.redirectTo(requireActivity(), mWeatherModel?.hourlies?.get(0), mCityMode, 0)
                }
                return
            }
            SigningActivity.redirectTo(it, mWeatherModel?.hourlies?.get(0), mCityMode, 0)
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
} 