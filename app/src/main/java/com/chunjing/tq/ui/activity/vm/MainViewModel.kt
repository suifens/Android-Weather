package com.chunjing.tq.ui.activity.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.chunjing.tq.bean.CityWeatherUpdate
import com.chunjing.tq.bean.Daily
import com.chunjing.tq.bean.VersionBean
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.bean.juhe.JuheBean
import com.chunjing.tq.bean.juhe.JuheSoul
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.CityListLogic
import com.chunjing.tq.db.LocationRepository
import com.chunjing.tq.db.WeatherBgRepository
import com.chunjing.tq.db.WeatherRepository
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.ext.JUHE_SOUL
import com.chunjing.tq.ui.base.BaseViewModel
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.net.LoadState
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.WeatherUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

//  推荐弹窗
const val CACHE_RECOMMEND_TIME = "recommend_time"
const val CACHE_RECOMMEND_INTERVAL = "recommend_interval"

class MainViewModel : BaseViewModel() {
    private companion object {
        const val LOCATION_INTERVAL_SECONDS = 15 * 60
    }

    val cities = MutableLiveData<List<CityEntity>>()
    /** @deprecated 优先订阅 [weatherUpdate]；保留供旧代码读取快照 */
    val weatherMap = MutableLiveData<HashMap<String, WeatherBean>>()
    /** 单城天气变更，避免全量 map 广播触发所有 Tab / 列表刷新 */
    val weatherUpdate = MutableLiveData<CityWeatherUpdate>()
    /** 当前 Tab 及左右相邻 cityId，仅这些 Tab 会触发网络刷新 */
    val weatherRefreshWindow = MutableLiveData<Set<String>>(emptySet())
    val showIndex = MutableLiveData<Int>()

    var curCityId = ""
    val curCity = MutableLiveData<CityEntity>()
    val curWeather = MutableLiveData<WeatherBean>()
    val curBgEntity = MutableLiveData<WeatherBgEntity>()

    /** 各城市最新背景，避免 Tab 未选中时 [setBgEntity] 被丢弃后无法恢复 */
    private val bgByCityId = HashMap<String, WeatherBgEntity>()

    val curLocation = MutableLiveData<CityEntity>()

    /**
     * 一次定位成功并写入 [curLocation] 后递增；首页定位 Tab 据此重新拉天气（下拉刷新重新定位等场景不依赖 curLocation 是否被判定为“未变”）。
     */
    val locationWeatherRefreshNonce = MutableLiveData<Long?>(null)

    /** 默认 false，避免 Activity 订阅时粘性分发 null/true 与冷启动 [getLocation] 叠成两次请求 */
    val needLocation = MutableLiveData(false)

    //  心灵鸡汤
    val todaySoul = MutableLiveData<String?>()
    var lasLocation = 0L

    private val weatherRepository = WeatherRepository.getInstance()
    private val locationRepository = LocationRepository.getInstance()
    private val weatherBgRepository = WeatherBgRepository.getInstance()

    /** 定时定位请求被消费后复位，避免 needLocation 一直为 true 导致每次进首页再拉一次 */
    fun acknowledgeNeedLocationRequest() {
        needLocation.postValue(false)
    }

    /** 主界面 [MainActivity] 冷启动只触发一次定位，避免配置变更重建 Activity 时重复请求 */
    private var mainStartupLocationInvoked = false

    /** 添加城市返回首页时，优先选中该 cityId 对应 Tab（在 [getCitiesCache] 结果中解析） */
    @Volatile
    private var pendingSelectCityTabId: String? = null

    @Synchronized
    fun setPendingSelectCityTab(cityId: String) {
        pendingSelectCityTabId = cityId
    }

    @Synchronized
    fun consumePendingSelectCityTab(): String? {
        val id = pendingSelectCityTabId
        pendingSelectCityTabId = null
        return id
    }

    init {
    }

    fun setBgEntity(cityId: String, bgEntity: WeatherBgEntity) {
        if (bgEntity.imgPath.isBlank()) return
        bgByCityId[cityId] = bgEntity
        if (curCityId == cityId) {
            curBgEntity.postValue(bgEntity)
        }
    }

    private fun applyBgForCurrentCity() {
        bgByCityId[curCityId]?.let { curBgEntity.postValue(it) }
    }

    private fun ensureBgForCity(cityId: String) {
        if (bgByCityId.containsKey(cityId)) return
        getCityWeather(cityId)?.let { weather ->
            launchSilent {
                resolveWeatherBg(weather)?.let { setBgEntity(cityId, it) }
            }
        }
    }

    fun setWeather(cityId: String, weather: WeatherBean) {
        if (curCityId != cityId) {
            return
        }
        curWeather.postValue(weather)
    }

    fun setCity(city: CityEntity) {
        curCityId = city.cityId
        curCity.postValue(city)
        applyBgForCurrentCity()
        ensureBgForCity(city.cityId)
    }

    fun setCityId(cityId: String) {
        curCityId = cityId
        applyBgForCurrentCity()
        ensureBgForCity(cityId)
    }

    fun addCity(city: CityEntity) {
        launchSilent {
            AppRepo.getInstance().addCity(city)
            getCitiesCache()
        }
    }

    /**
     * 同步写入城市并强制刷新 [cities]，避免异步 [addCity] 与 UI 跳转竞态导致列表不更新、首页一直 loading。
     */
    suspend fun addCityAndRefreshCities(city: CityEntity) {
        withContext(Dispatchers.IO) {
            AppRepo.getInstance().addCity(city)
        }
        awaitCitiesCacheRefresh(forcePost = true)
    }

    fun getCityWeather(cityId: String): WeatherBean? {
        return weatherRepository.getMemoryWeather(cityId)
    }

    fun getWeatherMapSnapshot(): Map<String, WeatherBean> = weatherRepository.getMemorySnapshot()

    private fun publishWeather(cityId: String, weather: WeatherBean) {
        weatherRepository.putMemory(cityId, weather)
        weatherUpdate.postValue(CityWeatherUpdate(cityId, weather))
    }

    /** 切换 Tab 时更新可刷新窗口（当前 ±1） */
    fun updateWeatherRefreshWindow(cities: List<CityEntity>, centerIndex: Int) {
        weatherRefreshWindow.postValue(
            CityListLogic.refreshWindowIds(cities.map { it.cityId }, centerIndex)
        )
    }

    /** 从 Room 预加载各城缓存到内存，供城市列表展示 */
    private suspend fun preloadWeatherCaches(cityIds: List<String>) {
        val loaded = weatherRepository.preloadFromCache(cityIds)
        for ((cityId, weather) in loaded) {
            weatherUpdate.postValue(CityWeatherUpdate(cityId, weather))
        }
    }

    fun getCitiesCache() {
        launchSilent {
            awaitCitiesCacheRefresh()
        }
    }

    /**
     * 从数据库拉取城市列表并更新 [cities]；用于添加城市后确保列表已含新城市再回首页。
     */
    suspend fun awaitCitiesCacheRefresh(forcePost: Boolean = false) {
        val list = withContext(Dispatchers.IO) {
            val out = ArrayList<CityEntity>()
            val location = AppRepo.getInstance().getCity(LOCATION_ID)
            if (location != null) {
                out.add(location)
                out.addAll(AppRepo.getInstance().getAdditionalCities())
            } else {
                out.addAll(AppRepo.getInstance().getCities())
            }
            out
        }
        if (!forcePost && isCityListEquivalentForTabs(cities.value, list)) {
            return
        }
        cities.postValue(list)
        preloadWeatherCaches(list.map { it.cityId })
        val centerIndex = list.indexOfFirst { it.cityId == curCityId }.let { if (it >= 0) it else 0 }
        updateWeatherRefreshWindow(list, centerIndex)
    }

    /** 城市 Tab 顺序与展示用字段一致时不再 post，避免触发首页 ViewPager 无意义重建 */
    private fun isCityListEquivalentForTabs(
        old: List<CityEntity>?,
        new: List<CityEntity>
    ): Boolean {
        return CityListLogic.isEquivalentForTabs(
            old?.map {
                CityListLogic.CityTabSnapshot(
                    it.cityId, it.sortOrder, it.latitude, it.longitude, it.cityName, it.mergerName
                )
            },
            new.map {
                CityListLogic.CityTabSnapshot(
                    it.cityId, it.sortOrder, it.latitude, it.longitude, it.cityName, it.mergerName
                )
            }
        )
    }

    //  获取天气缓存
    fun getWeatherCache(cityId: String, callback: ((WeatherBean?) -> Unit)? = null) {
        launchSilent {
            weatherRepository.getCachedWeather(cityId).let {
                callback?.invoke(it)
            }
        }
    }

    /**
     * 同步拉取并落库天气（供添加城市等场景在同协程内完成，避免 [launchSilent] 内异常被静默吞掉导致界面无数据）。
     */
    suspend fun awaitFetchWeather(city: CityEntity): WeatherBean? {
        val result = weatherRepository.fetchAndCache(city) ?: return null
        weatherUpdate.postValue(CityWeatherUpdate(city.cityId, result))
        return result
    }

    fun fetchWeather(city: CityEntity, callback: ((WeatherBean?) -> Unit)? = null) {
        launchSilent {
            try {
                val r = awaitFetchWeather(city)
                callback?.invoke(r)
            } catch (e: Exception) {
                Log.e("MainViewModel", "fetchWeather: ${e.message}", e)
                callback?.invoke(null)
            }
        }
    }

    /**
     * 获取心灵鸡汤
     */
    fun fetchSoul() {
        launchSilent {
            val url = JUHE_SOUL
            val result = HttpUtils.get<JuheBean<JuheSoul>>(url)
            result?.result?.let {
                todaySoul.postValue(it.text)
            }
        }
    }

    /**
     * 判断是否需要定位
     */
    fun checkLocation() {
        launchSilent {
            var temp = false
            if (System.currentTimeMillis() - lasLocation > 10 * 60 * 1000) {
                temp = true
            }
            needLocation.postValue(temp)
        }
    }

    /**
     * 获取缓存中的天气
     */
    fun loadWeather(cityId: String, callback: ((WeatherBean?) -> Unit)? = null) {
        launchSilent {
            val cache = weatherRepository.getCachedWeather(cityId)
            cache?.let {
                publishWeather(cityId, cache)
            }
            callback?.invoke(cache)
        }
    }

    suspend fun resolveWeatherBg(weather: WeatherBean): WeatherBgEntity? {
        val tempType = WeatherUtils.getTempType(weather.observation.wxIcon)
        val timeType = WeatherUtils.getTimeType(weather.timeType())
        return weatherBgRepository.resolveWeatherBg(tempType, timeType)
    }

    /**
     * 通过天气信息获取天气背景
     */
    fun getWeatherBg(
        weather: WeatherBean,
        callback: ((WeatherBgEntity?) -> Unit)? = null
    ) {
        launchSilent {
            callback?.invoke(resolveWeatherBg(weather))
        }
    }

    fun getDailyWeatherBg(daily: Daily, callback: ((WeatherBgEntity?) -> Unit)? = null) {
        launchSilent {
            val iconCd = if (daily.weatherPart != null) daily.weatherPart!!.iconCd else 0
            val tempType = WeatherUtils.getTempType(iconCd)
            val isDay = daily.dayPart != null
            val timeType = WeatherUtils.getTimeType(isDay)
            callback?.invoke(weatherBgRepository.resolveWeatherBg(tempType, timeType))
        }
    }


    fun checkVersion(callback: ((String?) -> Unit)? = null) {
        launchSilent {
            val url = "https://app.yiguxm.com/chunjing/chunjing_banbenqingqiu.json"
            val result = HttpUtils.get<VersionBean>(url)
            result?.let {
                callback?.invoke(it.data.newVersion)
            }
        }
    }

    // <editor-fold default-state="collapsed" desc="定位">

    /**
     * 若尚未因主界面冷启动发起过定位，则标记为已发起并返回 true；用于与下拉刷新、定时刷新区分。
     */
    @Synchronized
    fun consumeMainStartupLocationInvoke(): Boolean {
        if (mainStartupLocationInvoked) {
            return false
        }
        mainStartupLocationInvoked = true
        return true
    }

    fun getLocation() {
        locationRepository.requestLocation(object : LocationRepository.Callbacks {
            override fun onStart() {
                loadState.postValue(LoadState.Start("正在获取位置..."))
            }

            override fun onLocation(location: android.location.Location) {
                launchSilent {
                    try {
                        val city = withContext(Dispatchers.IO) {
                            locationRepository.location2CityEntity(location)
                        }
                        val resolvedCode = locationRepository.resolveLocationCityCode(city)
                        if (resolvedCode.isNotBlank()) {
                            city.cityCode = resolvedCode
                        }
                        curLocation.postValue(city)
                        AppRepo.getInstance().addCity(city)
                        lasLocation = System.currentTimeMillis()
                        awaitCitiesCacheRefresh()
                        locationWeatherRefreshNonce.postValue(System.nanoTime())
                    } finally {
                        locationRepository.endLocationRequest()
                        loadState.postValue(LoadState.Finish)
                    }
                }
            }

            override fun onError(message: String) {
                loadState.postValue(LoadState.Error(message))
            }

            override fun onFinish() {
                // 成功路径在协程 finally 里 Finish；失败路径这里再补一次
                if (loadState.value !is LoadState.Finish) {
                    loadState.postValue(LoadState.Finish)
                }
            }
        })
    }

    fun getCacheLocation() {
        launchSilent {
            AppRepo.getInstance().getCity(LOCATION_ID)?.let {
                curLocation.postValue(it)
            }
        }
    }

    /**
     * 需要显示推荐弹窗
     */
    fun needShowRecommendAlert(): Boolean {
        var needShow = false
        val showTime = SpUtils.instance.getLong(CACHE_RECOMMEND_TIME, 0)
        if (System.currentTimeMillis() > showTime) {
            SpUtils.instance.putLong(
                CACHE_RECOMMEND_TIME,
                System.currentTimeMillis() + 7 * DateUtil.dayMillis()
            )
            needShow = true
        }
        return needShow
    }

    // </editor-fold>

    /// 获取背景图
    fun fetchWeatherBg(callback: ((Boolean?) -> Unit)? = null) {
        launchSilent {
            val updated = weatherBgRepository.fetchAndSyncWeatherBg()
            callback?.invoke(updated)
        }
    }

    /**
     * 下载视频
     * cover: 是否覆盖
     */
    fun downAloneVideo(info: WeatherBgEntity, cover: Boolean) {
        weatherBgRepository.downloadBgVideo(info, cover) { entity ->
            launchSilent {
                weatherBgRepository.saveWeatherBg(entity)
            }
        }
    }

    fun changeWeatherBg(entity: WeatherBgEntity) {
        launchSilent {
            weatherBgRepository.saveWeatherBg(entity)
        }
    }

    /// 获取日历背景图
    fun fetchCalendarBg(callback: ((Boolean?) -> Unit)? = null) {
        launchSilent {
            val updated = weatherBgRepository.fetchAndSyncCalendarBg()
            callback?.invoke(updated)
        }
    }

    fun getCalendarBg(
        holidayTime: String,
        callback: ((String?) -> Unit)? = null
    ) {
        launchSilent {
            val entity = weatherBgRepository.getCalendarBg(holidayTime)
            callback?.invoke(entity?.imgPath)
        }
    }

    private var timer:Timer? = null
    private var currentSecond=0

    fun startTimer(){
        if (timer != null) {
            return
        }
        timer = Timer()
        currentSecond=0

        val timerTask=object :TimerTask(){
            override fun run() {
                currentSecond++
//                if (onTimeChangeListener!=null){
//                    onTimeChangeListener.onTimeChanged(currentSecond)
//                }
                if (currentSecond >= LOCATION_INTERVAL_SECONDS) {
                    needLocation.postValue(true)
                    currentSecond = 0
                }
            }
        }
        timer!!.schedule(timerTask,1000,1000)
    }
//    interface OnTimeChangeListener{
//        fun onTimeChanged(second:Int)
//    }
//    private lateinit var onTimeChangeListener: OnTimeChangeListener
//    fun setOnTimeChangeListener(onTimeChangeListener: OnTimeChangeListener){
//        this.onTimeChangeListener=onTimeChangeListener
//    }

    override fun onCleared() {
        super.onCleared()
        locationRepository.endLocationRequest()
        timer?.cancel()
    }

}