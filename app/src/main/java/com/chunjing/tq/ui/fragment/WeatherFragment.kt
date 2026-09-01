package com.chunjing.tq.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.R
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.chunjing.tq.ad.AdManager
import com.chunjing.tq.adapter.Forecast15dAdapter
import com.chunjing.tq.adapter.ForecastHourlyAdapter
import com.chunjing.tq.adapter.LifeListAdapter
import com.chunjing.tq.bean.Daily
import com.chunjing.tq.bean.Hourly
import com.chunjing.tq.bean.LifeEntity
import com.chunjing.tq.bean.LifeItemBean
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.bean.juhe.JuheAlarmBean
import com.chunjing.tq.databinding.FragmentWeatherBinding
import com.chunjing.tq.databinding.LayoutAirQualityBinding
import com.chunjing.tq.databinding.LayoutCurrentBinding
import com.chunjing.tq.databinding.LayoutDayTempBinding
import com.chunjing.tq.databinding.LayoutForecast15dBinding
import com.chunjing.tq.databinding.LayoutForecastHourlyBinding
import com.chunjing.tq.databinding.LayoutLifeIndicatorBinding
import com.chunjing.tq.databinding.LayoutTomorrowTempBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.dialog.AlarmPopup
import com.chunjing.tq.dialog.DailyListPopup
import com.chunjing.tq.dialog.LifeDetailsPopup
import com.chunjing.tq.dialog.TravelPopup
import com.chunjing.tq.BuildConfig
import com.chunjing.tq.ext.LINK_CAILING
import com.chunjing.tq.ext.LINK_TAIFENG
import com.chunjing.tq.ext.checkGPSOpen
import com.chunjing.tq.ext.checkGPSPermission
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.LifeActivity
import com.chunjing.tq.ui.activity.PeripheralActivity
import com.chunjing.tq.ui.base.BaseVmFragment
import com.chunjing.tq.ui.base.BaseWebActivity
import com.chunjing.tq.ui.fragment.vm.WeatherViewModel
import com.chunjing.tq.utils.AdRemovalManager
import com.chunjing.tq.utils.AqiHelper
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.net.LoadState
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.WeatherUtils
import com.blankj.utilcode.util.NetworkUtils
import com.lxj.xpopup.XPopup

/**
 *  天气详情页面
 *
 * @constructor Create empty Weather fragment
 */
@SuppressLint("SetTextI18n")
class WeatherFragment : BaseVmFragment<FragmentWeatherBinding, WeatherViewModel>() {
    private val TAG = "WeatherFragment"

    private lateinit var mCityId: String
    private var mCity: CityEntity? = null
    private var mWeather: WeatherBean? = null
    private var bgEntity: WeatherBgEntity? = null
    private var alarmBean: JuheAlarmBean? = null

    private var mForecastHourlyAdapter: ForecastHourlyAdapter? = null
    private var mForecastAdapter15d: Forecast15dAdapter? = null
    private var mLifeAdapter: LifeListAdapter? = null

    private val mHourList by lazy { ArrayList<Hourly>() }
    private val mDailyList by lazy { ArrayList<Daily>() }
    //  当前
    private lateinit var mCurrentBinding: LayoutCurrentBinding
    private var travelPopup : TravelPopup? = null
    //  今天
    private lateinit var mTodayBinding: LayoutDayTempBinding
    //  明天天气
    private lateinit var mTomorrowBinding: LayoutTomorrowTempBinding
    //  24小时
    private lateinit var forecastHourlyBinding: LayoutForecastHourlyBinding
    //  体感
    private lateinit var airQualityBinding: LayoutAirQualityBinding
    //  15天
    private lateinit var forecast15dBinding: LayoutForecast15dBinding
    //  生活服务
    private lateinit var lifeIndicatorBinding: LayoutLifeIndicatorBinding
    private var isAdLoadAttempted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mCityId = it.getString("param_city_id").toString()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            WeatherFragment().apply {
                arguments = Bundle().apply {
                    putString("param_city_id", param1)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    override fun onPause() {
        super.onPause()
        dismissLoading()
    }

    override fun onResume() {
        super.onResume()

        bgEntity?.let {
            mainViewModel.setBgEntity(mCityId, it)
        }
        mWeather?.let {
            mainViewModel.setWeather(mCityId, it)
        }

        if (travelPopup != null && ContentUtil.travelCity != null) {
            travelPopup!!.setupTravelCity(ContentUtil.travelCity!!)
            ContentUtil.travelCity = null
        }

        viewModel.lifeLiveData.value?.let {
            mLifeAdapter?.updateData(it)
        }

        // 从去广告页返回时，若在去广告期间则隐藏广告
        if (AdRemovalManager.isAdRemovalActive()) {
            hideAd()
            return
        }
        // 第一次显示时异步请求广告
        if (!isAdLoadAttempted) {
            isAdLoadAttempted = true
            view?.post { loadTTFeedAd() }
        }
    }

    override fun bindView() = FragmentWeatherBinding.inflate(layoutInflater)

    override fun initView(view: View?) {
        // must use activity

        mCurrentBinding = LayoutCurrentBinding.bind(mBinding.root)
        mCurrentBinding.warningBtn.setOnClickListener {
            showWarning()
        }

        mTodayBinding = LayoutDayTempBinding.bind(mBinding.root)
        mTomorrowBinding = LayoutTomorrowTempBinding.bind(mBinding.root)
        forecastHourlyBinding = LayoutForecastHourlyBinding.bind(mBinding.root)
        forecast15dBinding = LayoutForecast15dBinding.bind(mBinding.root)
        airQualityBinding = LayoutAirQualityBinding.bind(mBinding.root)
        lifeIndicatorBinding = LayoutLifeIndicatorBinding.bind(mBinding.root)

        setupAdapters()
    }

    private fun setupAdapters() {
        mForecastHourlyAdapter = ForecastHourlyAdapter(requireContext(), mHourList)
        forecastHourlyBinding.rvForecastHour.adapter = mForecastHourlyAdapter
        forecastHourlyBinding.rvForecastHour.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        mForecastAdapter15d = Forecast15dAdapter(requireContext(), mDailyList)
        forecast15dBinding.rvForecast15.adapter = mForecastAdapter15d
        forecast15dBinding.rvForecast15.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        //  陈列图
        forecast15dBinding.imgShowDayList.setOnClickListener { showDayList() }

        val lifeEntity = LifeEntity()
        mLifeAdapter = LifeListAdapter(lifeEntity, false) {
            showLifePopup(it)
        }
        lifeIndicatorBinding.lifeRecyclerView.adapter = mLifeAdapter
        lifeIndicatorBinding.lifeRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        //  添加间距
        lifeIndicatorBinding.lifeRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view) // 获取view 在adapter中的位置。
                val columnSpacing = SizeUtils.dp2px(7f)
                if (position == 0) {
                    outRect.left = SizeUtils.dp2px(24f)
                } else {
                    outRect.left = columnSpacing
                }
                outRect.right = columnSpacing
            }
        })
        //  显示全部
        lifeIndicatorBinding.tvLifeList.setOnClickListener {
            requireActivity().startActivity<LifeActivity>()
        }
    }

    /**
     * 加载并展示 TTFeedAd 信息流广告（参考 Demo 中 WeatherFragment 的广告实现）
     * 若在去广告有效期内则不加载
     */
    private fun loadTTFeedAd() {
        if (!isAdded) return
        if (AdRemovalManager.isAdRemovalActive()) {
            mBinding.adBannerContainer.visibility = View.GONE
            mBinding.adBannerContainer.removeAllViews()
            return
        }
        val activity = requireActivity()
        val width = SizeUtils.px2dp(ScreenUtils.getScreenWidth().toFloat()) - 40
        AdManager.loadTTFeedAd(activity, BuildConfig.PGE_FEED_POS_ID, width, object : AdManager.AdCallback<TTFeedAd> {
            override fun onSuccess(ad: TTFeedAd) {
                if (!isAdded) return
                if (AdRemovalManager.isAdRemovalActive()) {
                    hideAd()
                    return
                }
                mBinding.adBannerContainer.visibility = View.VISIBLE
                AdManager.showTTFeedAd(requireActivity(), mBinding.adBannerContainer, ad)
            }

            override fun onFail(code: Int, msg: String) {
                Log.e(TAG, "TTFeedAd 加载失败: code=$code, msg=$msg")
            }
        })
    }

    /**
     * 隐藏广告
     */
    private fun hideAd() {
        mBinding.adBannerContainer.visibility = View.GONE
        mBinding.adBannerContainer.removeAllViews()
    }

    override fun initEvent() {
        AdRemovalManager.adRemovalChanged.observe(viewLifecycleOwner) {
            if (AdRemovalManager.isAdRemovalActive()) {
                hideAd()
            }
        }

        mBinding.refreshLayout.setOnRefreshListener {
            val host = requireActivity()
            if (mCityId == LOCATION_ID &&
                NetworkUtils.isConnected() &&
                host.checkGPSOpen() &&
                host.checkGPSPermission()
            ) {
                // 本地城市下拉刷新时优先触发重新定位，避免沿用旧坐标
                mainViewModel.getLocation()
            } else {
                loadData()
            }
        }

        //  城市
        viewModel.curCity.observe(this) {
            mCity = it
//            if (it.cityId == mainViewModel.curCityId) {
//                mainViewModel.setCity(it)
//            }
        }

        viewModel.curBgEntity.observe(this) {
            bgEntity = it
            mainViewModel.setBgEntity(mCityId, it)
        }

        viewModel.weatherNow.observe(this) {
            showWeatherNow(it)
            viewModel.getWeatherBgEntity(it, mCityId, false)
            mainViewModel.setWeather(mCityId, it)
        }

        // MainViewModel 拉到的天气（含添加城市后 awaitFetchWeather）与 Fragment 内 WeatherViewModel 对齐
        mainViewModel.weatherUpdate.observe(viewLifecycleOwner) { update ->
            if (update.cityId == mCityId) {
                viewModel.applyMainWeather(update.weather, mCityId)
            }
        }

        mainViewModel.weatherRefreshWindow.observe(viewLifecycleOwner) { window ->
            if (mCityId in window) {
                viewModel.loadCache(mCityId)
            }
        }

        viewModel.warnings.observe(this) {
            mCurrentBinding.warningBtn.visibility = View.VISIBLE
            alarmBean = it
        }

        viewModel.lifeLiveData.observe(this) {
            Log.e("WeatherFragment", "-------mLife Adapter" + if (mLifeAdapter != null) "存在" else "不存在")
            mLifeAdapter?.updateData(it)
        }

        viewModel.loadState.observe(this) {
            when (it) {
                is LoadState.Start -> {
                    mBinding.refreshLayout.isRefreshing = true
                }
                is LoadState.Error -> {

                }
                is LoadState.Finish -> {
                    mBinding.refreshLayout.isRefreshing = false
                }
            }
        }

        viewModel.todayAqi.observe(this) {
            Log.e(TAG, "-------initEvent: todayAqi = $it")
            showAirNow(it)
        }

        viewModel.nearbyCities.observe(this) {

        }

        // 定位成功后由 MainViewModel 递增 nonce，避免仅依赖 curLocation 时偶发不触发重新拉天气
        mainViewModel.locationWeatherRefreshNonce.observe(this) { nonce ->
            if (nonce == null) return@observe
            val city = mainViewModel.curLocation.value ?: return@observe
            if (mCityId == LOCATION_ID && city.isLocal()) {
                viewModel.refreshWithCity(city)
            }
        }
    }

    override fun loadData() {
        if (mainViewModel.weatherRefreshWindow.value?.contains(mCityId) == true) {
            viewModel.loadCache(mCityId)
        } else {
            viewModel.loadCacheDisplayOnly(mCityId)
        }
    }

    fun showWeatherNow(now: WeatherBean) {
        mWeather = now
        //  当前天气
        showCurrentBinding(now)
        //  2天天气
        showForecast(now)
        //  小时
        showHourly(now.hourlies)
        //  显示15天天气
        showDaily(now.dailies)
        //  体感温度等
        showMetric(now)

        now.today()?.let {
            val currentTime = TimeUtils.millis2String(System.currentTimeMillis(), "HH:mm")
            val sunrise = TimeUtils.millis2String(DateUtil.switchTime(it.sunRise), "HH:mm")
            val sunset = TimeUtils.millis2String(DateUtil.switchTime(it.sunSet), "HH:mm")

            forecastHourlyBinding.tvSunrise.text = "日出 $sunrise"
            forecastHourlyBinding.tvSunset.text = "日落 $sunset"
//            forecastHourlyBinding.sunView.setTimes(sunrise, sunset, currentTime)
        }
    }

    private fun showCurrentBinding(weather: WeatherBean) {

        val observation = weather.observation
        mCurrentBinding.tempTv.text = "${observation.metric.temp}°"
        mCurrentBinding.iconImgV.setImageResource(WeatherUtils.getIcon(observation.wxIcon))
        mCurrentBinding.phraseTv.text = observation.getWxcPhrase()
        mCurrentBinding.weatherDetailTv.text = "${observation.wdirCardinal}风" +
                "${WeatherUtils.windGrade(observation.metric.wspd)}级\n湿度${observation.rh}%"

        //  台风
        mCurrentBinding.taifengBtn.setOnClickListener {
            BaseWebActivity.startActivity(requireContext(),
                LINK_TAIFENG,
                resources.getString(R.string.title_taifeng),
                "Ac_Taifeng")
        }
        //  周边天气
        mCurrentBinding.zhoubianBtn.setOnClickListener {
            viewModel.nearbyCities.value?.let { cities ->
                showLoading(true)
                PeripheralActivity.startActivity(requireActivity(), cities)
            }
        }
        //  彩铃
//        mCurrentBinding.cailingBtn.setOnClickListener {
//            BaseWebActivity.startActivity(requireContext(),
//                LINK_CAILING,
//                resources.getString(R.string.title_cailing),
//                "Ac_Cailing")
//        }
        //  出行
        mCurrentBinding.chuxingBtn.setOnClickListener {
            travelPopup = TravelPopup(requireContext())
            travelPopup!!.setupCity(mCity!!)
            XPopup.Builder(requireContext())
                .isDestroyOnDismiss(true)   //  只使用一次
                .asCustom(travelPopup)
                .show()

        }
    }

    /**
     * 两天预报
     */
    private fun showForecast(weather: WeatherBean) {
        val today = weather.today()
        if (today != null) {
            val time = System.currentTimeMillis()
            mTodayBinding.tvDay.text = TimeUtils.millis2String(time, "dd")
            mTodayBinding.tvMonth.text = DateUtil.getSimpleMonth(TimeUtils.millis2String(time, "MM").toInt())
            //  早上、下午
            val part = today.dayPart ?: today.nightPart
            if (part != null) {
                mTodayBinding.tvParse.text = "${part.getPhrasesChar()}  ${today.metric?.maxTemp}/${today.metric?.minTemp}°"
                mTodayBinding.tvNotice.text = "${part.getPhrasesChar()}。最高 ${today.metric?.maxTemp}°。" +
                        "${part.wdirCardinal}风${part.num}级 | 湿度${part.rh}%"
                mTodayBinding.tvParse.isSelected = true
                mTodayBinding.tvNotice.isSelected = true
            }
        }

        val tomorrow = weather.tomorrow()
        if (tomorrow != null) {
            val time = System.currentTimeMillis() + 24 * 60 * 60 * 1000
            mTomorrowBinding.tDayTv.text = TimeUtils.millis2String(time, "dd")
            mTomorrowBinding.tMonthTv.text = DateUtil.getSimpleMonth(TimeUtils.millis2String(time, "MM").toInt())
            //  早上、下午
            val part = if (weather.isDay()) tomorrow.dayPart else tomorrow.nightPart
            if (part != null) {
                mTomorrowBinding.tParseTv.text = "${part.getPhrasesChar()}  ${tomorrow.metric?.maxTemp}/${tomorrow.metric?.minTemp}°"
                mTomorrowBinding.tNoticeTv.text = "${part.getPhrasesChar()}。最高 ${tomorrow.metric?.maxTemp}°。" +
                        "${part.wdirCardinal}风${part.num}级 | 湿度${part.rh}%"
                mTomorrowBinding.tParseTv.isSelected = true
                mTomorrowBinding.tNoticeTv.isSelected = true
            }
        }
    }

    /**
     * 逐小时天气
     */
    private fun showHourly(hourlyList: List<Hourly>) {
        var minTmp = hourlyList[0].temp
        var maxTmp = minTmp
        for (i in hourlyList.indices) {
            val tmp = hourlyList[i].temp
            minTmp = tmp.coerceAtMost(minTmp)
            maxTmp = tmp.coerceAtLeast(maxTmp)
        }
        mHourList.clear()
        mHourList.addAll(hourlyList)
        mForecastHourlyAdapter?.setRange(minTmp, maxTmp)
    }

    private fun showDaily(dailyList: List<Daily>) {
        mDailyList.clear()
        mDailyList.addAll(dailyList)

        val daily = dailyList[0]
        var min = daily.minTemp
        var max = daily.maxTemp
        mDailyList.forEach {
            min = min.coerceAtMost(it.minTemp)
            max = max.coerceAtLeast(it.maxTemp)
        }
        mForecastAdapter15d?.setRange(min, max)
    }

    /**
     * 体感温度等
     */
    private fun showMetric(weather: WeatherBean) {
        //  体感温度
        airQualityBinding.tvBodyTemp.text = "${weather.metric.feelsLike}°"
        //  大风
        airQualityBinding.tvWind.text = "${WeatherUtils.windGrade(weather.metric.wspd)}"
        //  气压
        airQualityBinding.tvPressure.text = "${weather.metric.pressure}"
        //  紫外线指数
        airQualityBinding.tvUvIndex.text = "${weather.observation.uvIndex}"
        //  湿度
        airQualityBinding.tvRh.text = "${weather.observation.rh}%"
        //  露点
        airQualityBinding.tvDewpt.text = "${weather.metric.dewpt}"
        //  能见度
        airQualityBinding.tvVisibility.text = "${weather.metric.vis}"
    }
    /**
     * 空气质量
     */
    private fun showAirNow(aqi: Int) {
        val quality = AqiHelper.getQuality(aqi)
        mTodayBinding.tvQuality.text = "$quality $aqi"
        val color = AqiHelper.getColorResId(aqi)
        mTodayBinding.tvQuality.setTextColor(ContextCompat.getColor(requireContext(), color))
        mTodayBinding.tvQuality.visibility = View.VISIBLE
    }

    //  显示天气列表
    private fun showDayList() {
        mWeather?.let { it1 ->
            val popup = DailyListPopup(requireContext())
            popup.setupData(it1.dailies)
            XPopup.Builder(requireContext())
                .isDestroyOnDismiss(true)   //  只使用一次
                .asCustom(popup)
                .show()
        }
    }
    //  显示预警
    private fun showWarning() {
        alarmBean?.let {
            val popup = AlarmPopup(requireContext())
            popup.setupData(it)
            XPopup.Builder(requireContext())
                .isDestroyOnDismiss(true)   //  只使用一次
                .asCustom(popup)
                .show()
        }
    }

    //  显示生活服务详情
    private fun showLifePopup(it: LifeItemBean) {
        val popup = LifeDetailsPopup(requireActivity())
        popup.lifeDetails = it
        val entity = this.viewModel.lifeLiveData.value
        if (entity != null) {
            popup.lifeTitle = entity.lifeTitleWith(it)
            popup.lifeImgRes = entity.lifeImageWith(it)
        }
        mainViewModel.curCity.value?.let { city ->
            popup.cityName = if (city.isLocal()) city.mergerName else city.cityName
        }
        mainViewModel.curWeather.value?.let { weather ->
            popup.observation = weather.observation
        }

        XPopup.Builder(requireActivity())
            .isDestroyOnDismiss(true)   //  只使用一次
            .asCustom(popup)
            .show()
    }
}