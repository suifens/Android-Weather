package com.chunjing.tq.ui.fragment.vm

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.bean.*
import com.chunjing.tq.bean.juhe.JuheAlarmBean
import com.chunjing.tq.bean.juhe.JuheBean
import com.chunjing.tq.bean.juhe.JuheWeather
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.ext.*
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseViewModel
import com.goodtech.weatherlib.BaseApp
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

const val CACHE_WEATHER_NOW = "now_"
const val CACHE_WEATHER_DAY = "day_"
const val CACHE_ALARM_NOW = "alarm_"
const val CACHE_LIFE = "life_"
const val CACHE_JUHE_WEATHER = "juhe_weather_"

class WeatherViewModel : BaseViewModel() {

    val weatherNow = MutableLiveData<WeatherBean>()
    val curCity = MutableLiveData<CityEntity>()
    val curBgEntity = MutableLiveData<WeatherBgEntity>()
    val lifeLiveData = MutableLiveData<LifeEntity>()
    val warnings = MutableLiveData<JuheAlarmBean>()
    val todayAqi = MutableLiveData<Int>()
    val nearbyCities = MutableLiveData<ArrayList<CityEntity>>()

    val calendarBgPath = MutableLiveData<String?>()

    @SuppressLint("NullSafeMutableLiveData")
    fun loadCacheDisplayOnly(cityId: String) {
        launch {
            loadCacheInternal(cityId, fetchNetwork = false)
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun loadCache(cityId: String) {
        launch {
            loadCacheInternal(cityId, fetchNetwork = true)
        }
    }

    private suspend fun loadCacheInternal(cityId: String, fetchNetwork: Boolean) {
        val city = AppRepo.getInstance().getCity(cityId)
        if (city == null && cityId == LOCATION_ID) {
            mainViewModel.curLocation.value?.let { loc ->
                if (loc.isLocal()) {
                    if (fetchNetwork) {
                        refreshWithCity(loc)
                    } else {
                        curCity.postValue(loc)
                        AppRepo.getInstance().getCache<WeatherBean?>(CACHE_WEATHER_NOW + loc.cityId)?.let {
                            weatherNow.postValue(it)
                        }
                    }
                    return
                }
            }
        }
        city?.let {
            curCity.postValue(it)
            if (fetchNetwork) {
                getPeripheralCities()
            }
            val weather = AppRepo.getInstance().getCache<WeatherBean?>(CACHE_WEATHER_NOW + city.cityId)
            if (weather != null) {
                weatherNow.postValue(weather)
                if (fetchNetwork && (!TimeUtils.isToday(weather.updateTime)
                        || TimeUtils.getTimeSpanByNow(weather.updateTime, TimeConstants.HOUR) != 0L)
                ) {
                    fetchWeatherData(city)
                }
            } else if (fetchNetwork) {
                fetchWeatherData(city)
            }

            if (!fetchNetwork) {
                return
            }

            val cityCode = getCityCode(city.cityName)
            if (cityCode != null && cityCode.isNotEmpty()) {
                val alarm = AppRepo.getInstance().getCache<JuheAlarmBean?>(CACHE_ALARM_NOW + cityCode)
                if (alarm != null) {
                    warnings.postValue(alarm)
                    if (!TimeUtils.isToday(alarm.updateTime)) {
                        fetchAlarmData(city)
                    }
                } else {
                    fetchAlarmData(city)
                }
            }

            getLifeCityName(city.cityName)?.let { cityName ->
                val life = AppRepo.getInstance().getCache<LifeEntity?>(CACHE_LIFE + cityName)
                if (life != null) {
                    lifeLiveData.postValue(life)
                    if (!TimeUtils.isToday(life.updateTime)) {
                        fetchLifeData(city)
                    }
                } else {
                    fetchLifeData(city)
                }

                val jWeather = AppRepo.getInstance().getCache<JuheWeather?>(CACHE_JUHE_WEATHER + cityName)
                if (jWeather != null) {
                    todayAqi.postValue(jWeather.aqi)
                    if (!TimeUtils.isToday(jWeather.updateTime)) {
                        fetchQueryData(city)
                    }
                } else {
                    fetchQueryData(city)
                }
            }
        }
    }

    fun loadLocationCache() {
        mainViewModel.loadWeather(LOCATION_ID) { weather ->
            weather?.let {
                weatherNow.postValue(it)
            }
        }
    }

    fun loadData(cityId: String) {
        launchSilent {
            val city = AppRepo.getInstance().getCity(cityId)
            city?.let {
                curCity.postValue(it)
                // 实时天气
                fetchWeatherData(it)
                // 预警
                fetchAlarmData(it)
                //  生活
                fetchLifeData(it)
                //  获取天气实况
                fetchQueryData(it)
            }
        }
    }

    fun refreshWithCity(city: CityEntity) {
        curCity.postValue(city)
        fetchWeatherData(city)
        fetchAlarmData(city)
        fetchLifeData(city)
        fetchQueryData(city)
    }

    /** 与 [com.chunjing.tq.ui.activity.vm.MainViewModel.weatherMap] 同步，避免仅更新全局缓存时本页不刷新 */
    fun applyMainWeather(w: WeatherBean, cityId: String) {
        weatherNow.postValue(w)
        getWeatherBgEntity(w, cityId, false)
    }

    //  获取背景
    fun getWeatherBgEntity(weather: WeatherBean, cityId: String, isItem: Boolean) {
        launchSilent {
            mainViewModel.resolveWeatherBg(weather)?.let { entity ->
                curBgEntity.postValue(entity)
                if (!isItem) {
                    mainViewModel.setBgEntity(cityId, entity)
                }
            }
        }
    }

    /// 获取日历背景
    fun getCalendarBgEntity() {
        launchSilent {
            val time = TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd")
            mainViewModel.getCalendarBg(time) {
                calendarBgPath.postValue(it)
            }
        }
    }
    
    /**
     * 实时天气获取
     */
    private fun fetchWeatherData(city: CityEntity) {
        launch {
            mainViewModel.fetchWeather(city) { result ->
                result?.let { weather ->
                    weather.updateTime = System.currentTimeMillis()
                    weatherNow.postValue(weather)
                }
            }
        }
    }

    /**
     * 预警获取
     */
    private fun fetchAlarmData(city: CityEntity) {
        launchSilent {
            val cityCode = getCityCode(city.cityName)
            if (cityCode != null && cityCode.isNotEmpty()) {
                val url = String.format(JUHE_ALARM, cityCode)
                val result = HttpUtils.get<JuheBean<List<JuheAlarmBean>>>(url)
                result?.result?.let {
                    var alarmBean: JuheAlarmBean? = null
                    if (it.isNotEmpty()) {
                        for (i in it.indices) {
                            val alarm = it[i]
                            if (alarm.district.isEmpty()) {
                                alarmBean = alarm
                                break
                            }
                        }
                        if (alarmBean == null) {
                            alarmBean = it.first()
                        }
                    }
                    alarmBean?.let { alarm ->
                        alarm.updateTime = System.currentTimeMillis()
                        AppRepo.getInstance().saveCache(CACHE_ALARM_NOW + cityCode, alarm)
                        warnings.postValue(alarm)
                    }
                }
            }
        }
    }

    /**
     * 获取生活数据
     */
    private fun fetchLifeData(city: CityEntity) {
        launchSilent {
            val cityName = getLifeCityName(city.cityName)
            if (TextUtils.isEmpty(cityName)) {
                return@launchSilent
            }

            val url = String.format(JUHE_LIFE, cityName)
            val result = HttpUtils.get<JuheBean<LifeResult>>(url)
            result?.result?.life?.let { entity ->
                entity.updateTime = System.currentTimeMillis()
                AppRepo.getInstance().saveCache(CACHE_LIFE + cityName, entity)
                lifeLiveData.postValue(entity)
            }
        }
    }

    /**
     * 获取天气实况
     */
    private fun fetchQueryData(city: CityEntity) {
        launchSilent {
            val cityName = getLifeCityName(city.cityName)
            if (TextUtils.isEmpty(cityName)) {
                return@launchSilent
            }

            val url = String.format(JUHE_QUERY, cityName)
            val result = HttpUtils.get<JuheBean<JuheWeather>>(url)
            result?.result?.let { weather ->
                weather.updateTime = System.currentTimeMillis()
                AppRepo.getInstance().saveCache(CACHE_JUHE_WEATHER + cityName, weather)
                todayAqi.postValue(weather.aqi)
            }
        }
    }

    private fun getCityCodes(context: Context?): ArrayList<CityCode> {
        val cityModes: ArrayList<CityCode> = ArrayList()
        val cityJson: String = Utils.getJson("cityCode.json", context)
        val jsonElement = Gson().fromJson(cityJson, JsonObject::class.java)
        if (jsonElement != null) {
            val jsonArray = Gson().fromJson(jsonElement["citys"], JsonArray::class.java)
            val list: ArrayList<CityCode> = Gson().fromJson(
                jsonArray,
                object : TypeToken<ArrayList<CityCode?>?>() {}.type
            )
            cityModes.addAll(list)
        }
        return cityModes
    }

    /**
     * 获取城市code
     */
    private fun getCityCode(cityName: String) : String? {

        if (!SpUtils.instance.getBoolean(REMINDER_WEATHER, true)) {
            //  不添加提醒
            return null
        }

        var cityCode: String? = null
        val list: ArrayList<CityCode> = getCityCodes(BaseApp.context)
        for (cityCodeMode in list) {
            if (cityCodeMode.city_name.contains(cityName)) {
                cityCode = cityCodeMode.city_code
                break
            }
        }

        return cityCode
    }

    /**
     *  获取周边城市
     */
    private fun getPeripheralCities() {
        launchSilent {
            curCity.value?.let {
                val result = AppRepo.getInstance().searchCities(it.cityCode) as ArrayList
                nearbyCities.postValue(result)
            }
        }
    }

    private fun getLifeCityName(cityName: String?): String? {
        var temp = cityName
        if (!TextUtils.isEmpty(cityName)) {
            if (cityName!!.endsWith("市")) {
                temp = cityName.replace("市", "")
            } else if (cityName.endsWith("县")) {
                temp = cityName.replace("县", "")
            } else if (cityName.endsWith("区")) {
                temp = cityName.replace("区", "")
            }
        }
        return temp
    }

}