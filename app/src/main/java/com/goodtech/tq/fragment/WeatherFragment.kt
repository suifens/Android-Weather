package com.goodtech.tq.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewStub
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.dp.DPSdk
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.goodtech.tq.BuildConfig
import com.goodtech.tq.R
import com.goodtech.tq.activity.BaseActivity
import com.goodtech.tq.ad.AdFeedFragment
import com.goodtech.tq.app.App
import com.goodtech.tq.base.callback.DataCallback
import com.goodtech.tq.fragment.view.CurrentItemView
import com.goodtech.tq.fragment.view.DailyListItemView
import com.goodtech.tq.fragment.view.HoursItemView
import com.goodtech.tq.fragment.view.LineTempItemView
import com.goodtech.tq.fragment.view.ObservationView
import com.goodtech.tq.fragment.view.RecentItemView
import com.goodtech.tq.helpers.BtnLinkHelper
import com.goodtech.tq.httpClient.WeatherHttpHelper
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.others.airQuality.AirQualityActivity
import com.goodtech.tq.modules.others.airQuality.view.AirLifeView
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

class WeatherFragment : AdFeedFragment(), OnRefreshListener, WeatherHeaderListener {

    private val TAG = "WeatherFragment"
    private lateinit var mRefreshLayout: SmartRefreshLayout
    private lateinit var mScrollView: NestedScrollView
    private var mWeatherModel: WeatherModel? = null
    private var mStateBarBg: View? = null
    private var mCityMode: CityMode? = null
    private var mHadLoad = false
    private var isFirstLoad = true

    // Views
    private lateinit var mContainerView: View
    private lateinit var mCurrentView: CurrentItemView
    private lateinit var mRecentView: RecentItemView
    private lateinit var mHoursView: HoursItemView
    private lateinit var mFeedContainer: FrameLayout
    private lateinit var mFeedContainer2: FrameLayout
    private lateinit var mDailyListView: DailyListItemView
    private lateinit var mLineTempView: LineTempItemView
    private lateinit var mLifeView: AirLifeView
    private lateinit var mObservationView: ObservationView

    // Ad related
    private var mLoadSuccess = false
    private var mLoadSuccess2 = false
    private var mIsLoadedAndShow = false
    private var mIsLoadedAndShow2 = false
    private var mGMNativeAd: TTFeedAd? = null
    private var mGMNativeAd2: TTFeedAd? = null

    override fun getViewLayoutRes(): Int = R.layout.fragment_weather

    override fun setupCacheViews() {
        super.setupCacheViews()
        mRefreshLayout = mCacheView as SmartRefreshLayout
        mScrollView = mCacheView.findViewById(R.id.scroll_view)
    }

    fun setStateBar(stateBar: View) {
        mStateBarBg = stateBar
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRefreshLayout.setOnRefreshListener(this)

        mScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
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
        val viewStub = mCacheView.findViewById<ViewStub>(R.id.stub_weather_data)
        if (viewStub?.parent != null) {
            val inflate = viewStub.inflate()
            initView(inflate)
            mHadLoad = true
            updateData()
        }
        if (isFirstLoad) {
            isFirstLoad = false
            if (SpUtils.getInstance().isAgreePermission()) {
                mHandler.postDelayed({ Thread { initAdLoader() }.start() }, 500)
            }
        }
    }

    private fun initView(view: View) {
        mContainerView = view.findViewById(R.id.weatherContainer)
        mContainerView.visibility = View.INVISIBLE
        mCurrentView = view.findViewById(R.id.item_current)
        mCurrentView.setItemListener(this)
        mRecentView = view.findViewById(R.id.item_recent)
        mHoursView = view.findViewById(R.id.item_hours)
        mFeedContainer = view.findViewById(R.id.item_ad1)
        mFeedContainer2 = view.findViewById(R.id.item_ad2)
        mObservationView = view.findViewById(R.id.item_observation)
        mDailyListView = view.findViewById(R.id.item_daily_list)
        mLineTempView = view.findViewById(R.id.item_line_temp)
        mLifeView = view.findViewById(R.id.view_life)

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

    fun changeWeather(model: WeatherModel, cityMode: CityMode) {
        mWeatherModel = model
        mCityMode = cityMode
        updateData()
    }

    private fun updateData() {
        if (mHadLoad && mCurrentView != null && mWeatherModel != null) {
            mHandler.post {
                // 在HoursItemView显示时加载第一个广告
                if (mWeatherModel?.hourlies != null) {
                    loadFirstAd()
                }

                // 在DailyListItemView显示时加载第二个广告
                if (mWeatherModel?.dailies != null) {
                    loadSecondAd()
                }

                mContainerView.visibility = View.VISIBLE

                mCurrentView.setData(mWeatherModel!!)
                mRecentView.setData(mWeatherModel!!)
                mWeatherModel?.hourlies?.let { mHoursView.setHourlies(mWeatherModel!!) }
                mCityMode?.let { mObservationView.setData(mWeatherModel!!) }
                mWeatherModel?.dailies?.let {
                    mDailyListView.setData(mWeatherModel!!)
                    mLineTempView.setData(mWeatherModel!!)
                }
                mWeatherModel?.lifeModel?.let { mLifeView.setupLife(it, Color.WHITE) }
            }
        }
    }

    private fun loadFirstAd() {
        val width = SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat())
        loadFeedAd(BuildConfig.PGE_EXPRESS_POS_ID, width, object : DataCallback<TTFeedAd> {
            override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
                if (data != null) {
                    showAd(mFeedContainer, true, false, data)
                }
            }
        })
    }

    private fun loadSecondAd() {
        val width = SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat())
        loadFeedAd(BuildConfig.PGE_EXPRESS_POS_ID3, width, object : DataCallback<TTFeedAd> {
            override fun onComplete(data: TTFeedAd?, errorMsg: String?) {
                if (data != null) {
                    showAd(mFeedContainer2, true, false, data)
                }
            }
        })
    }

    private fun initNativeExpressAD() {
        mLoadSuccess = false
        mLoadSuccess2 = false
        mFeedContainer.removeAllViews()
        mFeedContainer2.removeAllViews()
        Log.e(TAG, "initNativeExpressAD: ++++ ${System.currentTimeMillis()}")
    }

    private fun initAdLoader() {
        mLoadSuccess = false
        mLoadSuccess2 = false
        mFeedContainer.removeAllViews()
        mFeedContainer2.removeAllViews()
        Log.e(TAG, "initAdLoader: ++++ ${System.currentTimeMillis()}")
    }

    private fun removeAdView(ad: TTFeedAd?) {
//        ad?.let {
//            if (it.getMediationManager() != null) {
//                it.getMediationManager().destroy()
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeAdView(mGMNativeAd)
        removeAdView(mGMNativeAd2)
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