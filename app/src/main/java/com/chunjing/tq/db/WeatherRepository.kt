package com.chunjing.tq.db

import android.util.Log
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.bean.Daily
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ext.WEATHER_URL
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_DAY
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_NOW
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.utils.DateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 天气 HTTP 拉取、Room 缓存与内存快照；UI 层通过 [MainViewModel] 订阅更新事件。
 */
class WeatherRepository private constructor(
    private val appRepo: AppRepo = AppRepo.getInstance()
) {

    private val memoryCache = HashMap<String, WeatherBean>()

    fun getMemoryWeather(cityId: String): WeatherBean? = synchronized(memoryCache) {
        memoryCache[cityId]
    }

    fun getMemorySnapshot(): Map<String, WeatherBean> = synchronized(memoryCache) {
        HashMap(memoryCache)
    }

    fun putMemory(cityId: String, weather: WeatherBean) {
        synchronized(memoryCache) {
            memoryCache[cityId] = weather
        }
    }

    suspend fun getCachedWeather(cityId: String): WeatherBean? =
        withContext(Dispatchers.IO) {
            appRepo.getCache(CACHE_WEATHER_NOW + cityId)
        }

    /**
     * 网络拉取并落库；成功后写入内存缓存后返回。
     */
    suspend fun fetchAndCache(city: CityEntity): WeatherBean? {
        if (city.latitude.isBlank() || city.longitude.isBlank()) {
            Log.e(TAG, "fetchAndCache: blank lat/lon cityId=${city.cityId}")
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
                    val todayDaily = appRepo.getCache<Daily?>(todayKey)
                    if (todayDaily == null) {
                        appRepo.saveCache("$CACHE_WEATHER_DAY${city.cityId}_$todayStr", daily)
                    }
                    break
                }
            }
            if (needAddDay) {
                val yesterdayKey = "$CACHE_WEATHER_DAY${city.cityId}_$yesterdayStr"
                appRepo.getCache<Daily?>(yesterdayKey)?.let {
                    result.dailies.add(0, it)
                }
            }
            appRepo.saveCache(CACHE_WEATHER_NOW + city.cityId, result)
        }

        putMemory(city.cityId, result)
        return result
    }

    /**
     * 将未进内存的城市天气从 Room 预载到内存。
     */
    suspend fun preloadFromCache(cityIds: List<String>): List<Pair<String, WeatherBean>> {
        val loaded = ArrayList<Pair<String, WeatherBean>>()
        withContext(Dispatchers.IO) {
            for (cityId in cityIds) {
                if (getMemoryWeather(cityId) != null) continue
                getCachedWeather(cityId)?.let {
                    putMemory(cityId, it)
                    loaded.add(cityId to it)
                }
            }
        }
        return loaded
    }

    companion object {
        private const val TAG = "WeatherRepository"

        @Volatile
        private var instance: WeatherRepository? = null

        @JvmStatic
        fun getInstance(): WeatherRepository =
            instance ?: synchronized(this) {
                instance ?: WeatherRepository().also { instance = it }
            }
    }
}
