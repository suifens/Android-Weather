package com.chunjing.tq.ui.activity.vm

import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.bean.CalendarBgBean
import com.chunjing.tq.bean.Daily
import com.chunjing.tq.bean.VersionBean
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.bean.WeatherBgBean
import com.chunjing.tq.bean.juhe.JuheBean
import com.chunjing.tq.bean.juhe.JuheSoul
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CalendarBgEntity
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.ext.JUHE_SOUL
import com.chunjing.tq.ext.WEATHER_URL
import com.chunjing.tq.ui.base.BaseViewModel
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_DAY
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_NOW
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.BaseApp
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.net.LoadState
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.WeatherUtils
import com.mapzen.android.lost.api.LocationListener
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

//  推荐弹窗
const val CACHE_RECOMMEND_TIME = "recommend_time"
const val CACHE_RECOMMEND_INTERVAL = "recommend_interval"

class MainViewModel : BaseViewModel() {
    private companion object {
        const val LOCATION_INTERVAL_SECONDS = 15 * 60
        const val MAX_LAST_KNOWN_AGE_MS = 2 * 60 * 1000L
    }

    val cities = MutableLiveData<List<CityEntity>>()
    val weatherMap = MutableLiveData<HashMap<String, WeatherBean>>()
    val showIndex = MutableLiveData<Int>()

    var curCityId = ""
    val curCity = MutableLiveData<CityEntity>()
    val curWeather = MutableLiveData<WeatherBean>()
    val curBgEntity = MutableLiveData<WeatherBgEntity>()

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

    /// key: tempType-timeType
    private val mBgMap = HashMap<String, WeatherBgEntity>()

    private val mWeatherMap = HashMap<String, WeatherBean>()
    private var lostApiClient: LostApiClient? = null
    private var locationListener: LocationListener? = null

    /** 合并短时间内的多次 [getLocation]（如冷启动 + needLocation 粘性、连续 connect/onConnected） */
    @Volatile
    private var locationRequestActive = false

    @Synchronized
    private fun tryBeginLocationRequest(): Boolean {
        if (locationRequestActive) {
            return false
        }
        locationRequestActive = true
        return true
    }

    @Synchronized
    private fun endLocationRequest() {
        locationRequestActive = false
    }

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
        if (curCityId != cityId) {
            return
        }
        curBgEntity.postValue(bgEntity)
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
    }

    fun setCityId(cityId: String) {
        curCityId = cityId
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
        return mWeatherMap[cityId]
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
    }

    /** 城市 Tab 顺序与展示用字段一致时不再 post，避免触发首页 ViewPager 无意义重建 */
    private fun isCityListEquivalentForTabs(
        old: List<CityEntity>?,
        new: List<CityEntity>
    ): Boolean {
        if (old == null || old.size != new.size) {
            return false
        }
        for (i in new.indices) {
            val a = old[i]
            val b = new[i]
            if (a.cityId != b.cityId) return false
            if (a.latitude != b.latitude || a.longitude != b.longitude) return false
            if (a.cityName != b.cityName || a.mergerName != b.mergerName) return false
        }
        return true
    }

    //  获取天气缓存
    fun getWeatherCache(cityId: String, callback: ((WeatherBean?) -> Unit)? = null) {
        launchSilent {
            AppRepo.getInstance().getCache<WeatherBean?>(CACHE_WEATHER_NOW + cityId).let {
                callback?.invoke(it)
            }
        }
    }

    /**
     * 同步拉取并落库天气（供添加城市等场景在同协程内完成，避免 [launchSilent] 内异常被静默吞掉导致界面无数据）。
     */
    suspend fun awaitFetchWeather(city: CityEntity): WeatherBean? = fetchWeatherCore(city)

    private suspend fun fetchWeatherCore(city: CityEntity): WeatherBean? {
        if (city.latitude.isBlank() || city.longitude.isBlank()) {
            Log.e("MainViewModel", "fetchWeatherCore: blank lat/lon cityId=${city.cityId}")
            return null
        }
        val url = String.format(WEATHER_URL, city.latitude, city.longitude)
        val result = withContext(Dispatchers.IO) {
            HttpUtils.get<WeatherBean>(url)
        } ?: return null

        withContext(Dispatchers.IO) {
            result.updateTime = System.currentTimeMillis()

            val todayStr = TimeUtils.millis2String(System.currentTimeMillis(), "MM月dd日")
            val todayKey = "$CACHE_WEATHER_DAY${city.cityId}_$todayStr"
            val yesterdayStr = DateUtil.getYesterday()
            var needAddDay = true
            for (i in 0 until result.dailies.size) {
                val daily = result.dailies[i]
                val time = daily.time
                if (time == yesterdayStr) {
                    needAddDay = false
                }
                if (time == todayStr) {
                    val todayDaily = AppRepo.getInstance().getCache<Daily?>(todayKey)
                    if (todayDaily == null) {
                        AppRepo.getInstance()
                            .saveCache("$CACHE_WEATHER_DAY${city.cityId}_$todayStr", daily)
                    }
                    break
                }
            }
            if (needAddDay) {
                val yesterdayKey = "$CACHE_WEATHER_DAY${city.cityId}_$yesterdayStr"
                AppRepo.getInstance().getCache<Daily?>(yesterdayKey)?.let {
                    result.dailies.add(0, it)
                }
            }
            Log.e("TAG", "fetchWeather: ${result.dailies.size}")
            AppRepo.getInstance().saveCache(CACHE_WEATHER_NOW + city.cityId, result)
        }

        mWeatherMap[city.cityId] = result
        weatherMap.postValue(HashMap(mWeatherMap))
        return result
    }

    fun fetchWeather(city: CityEntity, callback: ((WeatherBean?) -> Unit)? = null) {
        launchSilent {
            try {
                val r = fetchWeatherCore(city)
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
            val cache = AppRepo.getInstance().getCache<WeatherBean?>(CACHE_WEATHER_NOW + cityId)
            cache?.let {
                mWeatherMap[cityId] = cache
                weatherMap.postValue(HashMap(mWeatherMap))
            }
            callback?.invoke(cache)
        }
    }

    /**
     * 通过天气信息获取天气背景
     *
     * @param weather 天气
     * @param isItem  是否是item
     * @param callback 回调
     */
    fun getWeatherBg(
        weather: WeatherBean,
        callback: ((WeatherBgEntity?) -> Unit)? = null
    ) {
        launchSilent {
            val tempType = WeatherUtils.getTempType(weather.observation.wxIcon)
            val timeType = WeatherUtils.getTimeType(weather.timeType())
            val key = "$tempType-$timeType"
            val entity = if (mBgMap.contains(key))
                mBgMap[key]
            else
                AppRepo.getInstance().getWeatherBg(tempType, timeType)

            if (entity == null) {
                fetchWeatherBg {
                    viewModelScope.launch() {
                        if (it != null && it) {
                            val result =
                                AppRepo.getInstance().getWeatherBg(tempType, timeType)
                            callback?.invoke(result)
                        } else {
                            callback?.invoke(null)
                        }
                    }
                }
            } else {
                callback?.invoke(entity)
            }
        }
    }

    fun getDailyWeatherBg(daily: Daily, callback: ((WeatherBgEntity?) -> Unit)? = null) {
        launchSilent {
            val iconCd = if (daily.weatherPart != null) daily.weatherPart!!.iconCd else 0
            val tempType = WeatherUtils.getTempType(iconCd)
            val isDay = daily.dayPart != null
            val timeType = WeatherUtils.getTimeType(isDay)
            val entity = AppRepo.getInstance().getWeatherBg(tempType, timeType)
            if (entity == null) {
                fetchWeatherBg {
                    viewModelScope.launch() {
                        if (it != null && it) {
                            val result =
                                AppRepo.getInstance().getWeatherBg(tempType, timeType)
                            callback?.invoke(result)
                        } else {
                            callback?.invoke(null)
                        }
                    }
                }
            } else {
                callback?.invoke(entity)
            }
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
        if (!tryBeginLocationRequest()) {
            return
        }
        loadState.postValue(LoadState.Start("正在获取位置..."))
        val client = lostApiClient ?: createLostApiClient()
        if (client.isConnected()) {
            requestSingleUpdateFallback(client)
            return
        }
        try {
            client.connect()
        } catch (securityException: SecurityException) {
            endLocationRequest()
            loadState.postValue(LoadState.Error("定位权限不足，请重试"))
            loadState.postValue(LoadState.Finish)
        } catch (e: Exception) {
            endLocationRequest()
            loadState.postValue(LoadState.Error("获取定位失败,请重试"))
            loadState.postValue(LoadState.Finish)
        }
    }

    private fun createLostApiClient(): LostApiClient {
        val client = LostApiClient.Builder(BaseApp.context)
            .addConnectionCallbacks(object : LostApiClient.ConnectionCallbacks {
                override fun onConnected() {
                    requestSingleUpdateFallback(lostApiClient ?: return)
                }

                override fun onConnectionSuspended() {
                    endLocationRequest()
                    loadState.postValue(LoadState.Error("获取定位失败,请重试"))
                    loadState.postValue(LoadState.Finish)
                }
            })
            .build()
        lostApiClient = client
        return client
    }

    private fun requestSingleUpdateFallback(client: LostApiClient) {
        val request = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000L)
            .setFastestInterval(1000L)

        clearLocationListener()
        locationListener = LocationListener { location ->
            clearLocationListener()
            if (location != null) {
                handleLocationSuccess(location)
            } else {
                endLocationRequest()
                loadState.postValue(LoadState.Error("获取定位失败,请重试"))
                loadState.postValue(LoadState.Finish)
            }
        }

        try {
            val lastKnown = LocationServices.FusedLocationApi.getLastLocation(client)
            val isRecentLocation = lastKnown != null &&
                    (System.currentTimeMillis() - lastKnown.time) <= MAX_LAST_KNOWN_AGE_MS
            if (isRecentLocation) {
                handleLocationSuccess(lastKnown)
                return
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                client,
                request,
                locationListener ?: return
            )
        } catch (securityException: SecurityException) {
            clearLocationListener()
            endLocationRequest()
            loadState.postValue(LoadState.Error("定位权限不足，请重试"))
            loadState.postValue(LoadState.Finish)
        } catch (e: Exception) {
            clearLocationListener()
            endLocationRequest()
            loadState.postValue(LoadState.Error("获取定位失败,请重试"))
            loadState.postValue(LoadState.Finish)
        }
    }

    private fun clearLocationListener() {
        val client = lostApiClient
        val listener = locationListener
        if (client != null && client.isConnected() && listener != null) {
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(client, listener)
            } catch (_: Exception) {
                // ignore cleanup errors
            }
        }
        locationListener = null
    }

    private fun handleLocationSuccess(location: Location) {
        clearLocationListener()
        val client = lostApiClient
        if (client != null && client.isConnected()) {
            try {
                client.disconnect()
            } catch (_: Exception) {
                // ignore disconnect errors
            }
        }
        launchSilent {
            try {
                val city = location2CityEntity(location)
                val resolvedCode = resolveLocationCityCode(city)
                if (resolvedCode.isNotBlank()) {
                    city.cityCode = resolvedCode
                }
                curLocation.postValue(city)
                AppRepo.getInstance().addCity(city)
                lasLocation = System.currentTimeMillis()
                awaitCitiesCacheRefresh()
                locationWeatherRefreshNonce.postValue(System.nanoTime())
            } finally {
                endLocationRequest()
            }
        }
        loadState.postValue(LoadState.Finish)
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
//            val interval = SpUtils.instance.getInt(CACHE_RECOMMEND_INTERVAL, 3)
            SpUtils.instance.putLong(
                CACHE_RECOMMEND_TIME,
                System.currentTimeMillis() + 7 * DateUtil.dayMillis()
            )
//            SpUtils.instance.putInt(CACHE_RECOMMEND_INTERVAL, interval + 3)
            needShow = true
        }
        return needShow
    }

//    private var number = 0
    private fun location2CityEntity(location: Location): CityEntity {
        val cityEntity = CityEntity()
        var cityName = ""
        var district = ""
        var poiName = ""
        var cityCode = ""
//        number += 1

        try {
            val geocoder = Geocoder(BaseApp.context, Locale.getDefault())
//            val latitude = if (number%2 == 1) location.latitude else 39.9
//            val longitude = if (number%2 == 1) location.longitude else 116.4
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()
            cityName = address?.locality ?: address?.subAdminArea ?: ""
            district = address?.subLocality ?: address?.subAdminArea ?: ""
            poiName = address?.featureName ?: ""
            cityCode = address?.postalCode ?: ""
        } catch (e: Exception) {
            // ignore geocoder errors and keep fallback values
        }

        val fallbackName = cityName.ifBlank { "当前位置" }
        cityEntity.cityId = LOCATION_ID
        cityEntity.cityName = fallbackName
        cityEntity.cityCode = cityCode
        cityEntity.shortName = fallbackName
        cityEntity.mergerName =
            listOf(district, poiName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { fallbackName }
        cityEntity.latitude = location.latitude.toString()
        cityEntity.longitude = location.longitude.toString()
        cityEntity.setLocal()
        Log.e("TAG", "location2CityEntity: city = ${cityEntity.toString()}")
        return cityEntity
    }

    /**
     * 定位得到的 cityCode 以 city.db 为准（010/021/0755...）。
     * Geocoder 的 postalCode 常为空或为邮编，不适合作为业务 cityCode。
     */
    private suspend fun resolveLocationCityCode(city: CityEntity): String {
        val candidates = linkedSetOf(
            city.cityName,
            city.shortName,
            city.mergerName,
            city.mergerName.substringBefore(" ").trim(),
            city.cityName.removeSuffix("市").removeSuffix("县").removeSuffix("区").trim(),
            city.shortName.removeSuffix("市").removeSuffix("县").removeSuffix("区").trim()
        ).filter { it.isNotBlank() }

        for (keyword in candidates) {
            val match = AppRepo.getInstance()
                .searchCity(keyword)
                .firstOrNull { it.cityCode.isNotBlank() && !it.isLocal() }
            if (match != null) {
                return match.cityCode
            }
        }
        return ""
    }

    // </editor-fold>

    /// 获取背景图
    fun fetchWeatherBg(callback: ((Boolean?) -> Unit)? = null) {
        launchSilent {
            val url = "https://app.yiguxm.com/chunjing/beijing.json"
            val result = HttpUtils.get<WeatherBgBean>(url)
            if (result != null) {
//                val list = AppRepo.getInstance().getAllBgWeathers()
//                list.forEach { bgWeather ->
//                    if (bgWeather.videoPath.endsWith("mp4")
//                        && bgWeather.videoPath.startsWith("http")) {
//                        downAloneVideo(bgWeather, false)
//                    }
//                }

                val lastTime = AppRepo.getInstance().getCache<String?>("Weather_Bg_Update")
                val updateTime = TimeUtils.string2Millis(result.updateTime, "yyyy-MM-dd")
                if (lastTime == null || updateTime > TimeUtils.string2Millis(
                        lastTime,
                        "yyyy-MM-dd"
                    )
                ) {
                    for (entity in result.imgList) {
//                        if (entity.videoPath.contains(".mp4")) {
//                            downAloneVideo(entity, true)
//                        }
                        AppRepo.getInstance().addWeatherBg(entity)
                    }
                    AppRepo.getInstance().saveCache("Weather_Bg_Update", result.updateTime)
                    callback?.invoke(true)
                    return@launchSilent
                } else {
                    callback?.invoke(false)
                }
            } else {
                callback?.invoke(false)
            }
        }
    }

    /**
     * 下载视频
     * cover: 是否覆盖
     */
    fun downAloneVideo(info: WeatherBgEntity, cover: Boolean) {
        val url = info.videoPath
        val splitList = url.split("/")
        val fileName = splitList.last()
        if (!fileName.endsWith("mp4")) {
            return
        }

        val filePath = File("${ContentUtil.getVideoDir()}/$fileName")
        if (FileUtils.isFileExists(filePath) && !cover) {
            /// 视频存在
            info.videoPath = filePath.absolutePath
            changeWeatherBg(info)
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .get()
            .url(url)
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("moer", "onFailure: ")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val inputFile = File("${ContentUtil.getVideoDir()}/$fileName")

                val `is` = response.body!!.byteStream()
                val fos = FileOutputStream(inputFile)
                var len = 0
                val buffer = ByteArray(2048)
                while (-1 != `is`.read(buffer).also { len = it }) {
                    fos.write(buffer, 0, len)
                }
                fos.flush()
                fos.close()
                `is`.close()
                if (inputFile.length() > 0) {
                    Log.e("TAG", "onResponse: ${info.imgPath} ------ ${inputFile.absolutePath}")
                    info.videoPath = inputFile.absolutePath
                    changeWeatherBg(info)
                }
            }
        })
    }

    fun changeWeatherBg(entity: WeatherBgEntity) {
        launchSilent {
            AppRepo.getInstance().addWeatherBg(entity)
        }
    }

    /// 获取日历背景图
    fun fetchCalendarBg(callback: ((Boolean?) -> Unit)? = null) {
        launchSilent {
            val url = "https://app.yiguxm.com/chunjing/rili.json"
            val result = HttpUtils.get<CalendarBgBean>(url)
            if (result != null) {
                val lastTime = AppRepo.getInstance().getCache<String?>("Calendar_Bg_Update")
                val updateTime = TimeUtils.string2Millis(result.updateTime, "yyyy-MM-dd")
                if (lastTime == null || updateTime > TimeUtils.string2Millis(
                        lastTime,
                        "yyyy-MM-dd"
                    )
                ) {
                    AppRepo.getInstance().saveCache("Calendar_Bg_Update", result.updateTime)
                    for (entity in result.holidayList) {
                        if (entity.duration.toInt() in 2..9) {
                            val time = TimeUtils.string2Millis(entity.holidayTime, "yyyy-MM-dd")
                            for (i in 0 until entity.duration.toInt()) {
                                val nextTime = time + DateUtil.dayMillis() * i
                                entity.holidayTime = TimeUtils.millis2String(nextTime, "yyyy-MM-dd")
                                val newEntity = CalendarBgEntity()
                                newEntity.holiday = entity.holiday
                                newEntity.holidayTime = entity.holidayTime
                                newEntity.duration = entity.duration
                                newEntity.imgPath = entity.imgPath

                                AppRepo.getInstance().addCalendarBg(newEntity)
                            }
                        } else {
                            AppRepo.getInstance().addCalendarBg(entity)
                        }
                    }
                    callback?.invoke(true)
                    return@launchSilent
                } else {
                    callback?.invoke(false)
                }
            } else {
                callback?.invoke(false)
            }
        }
    }

    fun getCalendarBg(
        holidayTime: String,
        callback: ((String?) -> Unit)? = null
    ) {
        launchSilent {
            val entity = AppRepo.getInstance().getCalendarBg(holidayTime)
            if (entity != null) {
                callback?.invoke(entity.imgPath)
            } else {
                callback?.invoke(null)
            }
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
        clearLocationListener()
        try {
            lostApiClient?.disconnect()
        } catch (_: Exception) {
            // ignore disconnect errors
        }
        endLocationRequest()
        timer?.cancel()
    }

}