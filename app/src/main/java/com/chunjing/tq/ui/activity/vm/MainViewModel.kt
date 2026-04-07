package com.chunjing.tq.ui.activity.vm

import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.MyApp
import com.chunjing.tq.bean.*
import com.chunjing.tq.bean.juhe.JuheBean
import com.chunjing.tq.bean.juhe.JuheSoul
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CalendarBgEntity
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.ext.JUHE_SOUL
import com.chunjing.tq.ext.LAST_LOCATION_TIME
import com.chunjing.tq.ext.WEATHER_URL
import com.chunjing.tq.ui.base.BaseViewModel
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_DAY
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_NOW
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.net.LoadState
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.WeatherUtils
import com.mapzen.android.lost.api.LocationListener
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

//  推荐弹窗
const val CACHE_RECOMMEND_TIME = "recommend_time"
const val CACHE_RECOMMEND_INTERVAL = "recommend_interval"

class MainViewModel(val app: Application) : BaseViewModel(app) {

    val cities = MutableLiveData<List<CityEntity>>()
    val weatherMap = MutableLiveData<HashMap<String, WeatherBean>>()
    val showIndex = MutableLiveData<Int>()

    var curCityId = ""
    val curCity = MutableLiveData<CityEntity>()
    val curWeather = MutableLiveData<WeatherBean>()
    val curBgEntity = MutableLiveData<WeatherBgEntity>()

    val curLocation = MutableLiveData<CityEntity>()
    val needLocation = MutableLiveData<Boolean>()

    //  心灵鸡汤
    val todaySoul = MutableLiveData<String?>()
    var lasLocation = 0L

    /// key: tempType-timeType
    private val mBgMap = HashMap<String, WeatherBgEntity>()

    private val mWeatherMap = HashMap<String, WeatherBean>()
    private var lostApiClient: LostApiClient? = null
    private var locationListener: LocationListener? = null

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

    fun getCityWeather(cityId: String): WeatherBean? {
        return mWeatherMap[cityId]
    }

    fun getCitiesCache() {
        launchSilent {
            val list = ArrayList<CityEntity>()
            val location = AppRepo.getInstance().getCity(LOCATION_ID)
            if (location != null) {
                list.add(location)
                AppRepo.getInstance().getAdditionalCities().let { result ->
                    list.addAll(result)
                    cities.postValue(list)
                }
            } else {
                AppRepo.getInstance().getCities().let { result ->
                    list.addAll(result)
                    cities.postValue(list)
                }
            }
        }
    }

    //  获取天气缓存
    fun getWeatherCache(cityId: String, callback: ((WeatherBean?) -> Unit)? = null) {
        launchSilent {
            AppRepo.getInstance().getCache<WeatherBean?>(CACHE_WEATHER_NOW + cityId).let {
                callback?.invoke(it)
            }
        }
    }

    //  获取所有城市的天气
    fun getWeathers() {
        launch {
            cities.value?.let {
                for (city in it) {
                    fetchWeather(city)
                }
            }
        }
    }

    fun fetchWeather(city: CityEntity, callback: ((WeatherBean?) -> Unit)? = null) {
        // 实时天气
        launchSilent {
            val url = String.format(WEATHER_URL, city.latitude, city.longitude)
            val result = HttpUtils.get<WeatherBean>(url)
            if (result != null) {
                result.updateTime = System.currentTimeMillis()

                val todayStr = TimeUtils.millis2String(System.currentTimeMillis(), "MM月dd日")
                val todayKey = "$CACHE_WEATHER_DAY${city.cityId}_$todayStr"
                val yesterdayStr = DateUtil.getYesterday()
                //  需要添加昨日天气
                var needAddDay = true
                /// 保存今日
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
                        //  添加昨天天气
                        result.dailies.add(0, it)
                    }
                }
                Log.e("TAG", "fetchWeather: ${result.dailies.size}")
                //  保存数据
                AppRepo.getInstance().saveCache(CACHE_WEATHER_NOW + city.cityId, result)

                mWeatherMap[city.cityId] = result
                weatherMap.postValue(mWeatherMap)
                callback?.invoke(result)
            } else {
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
                weatherMap.postValue(mWeatherMap)
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

    fun getLocation() {
        loadState.postValue(LoadState.Start("正在获取位置..."))
        val client = lostApiClient ?: createLostApiClient()
        if (client.isConnected()) {
            requestSingleUpdateFallback(client)
            return
        }
        try {
            client.connect()
        } catch (securityException: SecurityException) {
            loadState.postValue(LoadState.Error("定位权限不足，请重试"))
            loadState.postValue(LoadState.Finish)
        } catch (e: Exception) {
            loadState.postValue(LoadState.Error("获取定位失败,请重试"))
            loadState.postValue(LoadState.Finish)
        }
    }

    private fun createLostApiClient(): LostApiClient {
        val client = LostApiClient.Builder(app)
            .addConnectionCallbacks(object : LostApiClient.ConnectionCallbacks {
                override fun onConnected() {
                    requestSingleUpdateFallback(lostApiClient ?: return)
                }

                override fun onConnectionSuspended() {
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
                loadState.postValue(LoadState.Error("获取定位失败,请重试"))
                loadState.postValue(LoadState.Finish)
            }
        }

        try {
            val lastKnown = LocationServices.FusedLocationApi.getLastLocation(client)
            if (lastKnown != null) {
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
            loadState.postValue(LoadState.Error("定位权限不足，请重试"))
            loadState.postValue(LoadState.Finish)
        } catch (e: Exception) {
            clearLocationListener()
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
        val city = location2CityEntity(location)
        curLocation.postValue(city)
        launchSilent {
            AppRepo.getInstance().addCity(city)
            lasLocation = System.currentTimeMillis()
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

    private fun location2CityEntity(location: Location): CityEntity {
        val cityEntity = CityEntity()
        var cityName = ""
        var district = ""
        var poiName = ""
        var cityCode = ""

        try {
            val geocoder = Geocoder(app, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()
            cityName = address?.locality ?: address?.subAdminArea ?: ""
            district = address?.subLocality ?: address?.subAdminArea ?: ""
            poiName = address?.featureName ?: ""
            cityCode = address?.postalCode ?: ""
        } catch (e: Exception) {
            // ignore geocoder errors and keep fallback values
        }

        val fallbackName = if (cityName.isBlank()) "当前位置" else cityName
        cityEntity.cityId = LOCATION_ID
        cityEntity.cityName = fallbackName
        cityEntity.cityCode = cityCode
        cityEntity.shortName = fallbackName
        cityEntity.mergerName = listOf(district, poiName).filter { it.isNotBlank() }.joinToString(" ")
        cityEntity.latitude = location.latitude.toString()
        cityEntity.longitude = location.longitude.toString()
        if (cityEntity.mergerName.isBlank()) {
            cityEntity.mergerName = "${cityEntity.latitude}, ${cityEntity.longitude}"
        }
        cityEntity.setLocal()
        return cityEntity
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
                if (currentSecond == 1000 * 60 * 15) {
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
        timer?.cancel()
    }

}