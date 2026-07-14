package com.chunjing.tq.db

import android.util.Log
import com.chunjing.tq.db.dao.CacheDao
import com.chunjing.tq.db.dao.CalendarBgDao
import com.chunjing.tq.db.dao.CityDao
import com.chunjing.tq.db.dao.WeatherBgDao
import com.chunjing.tq.db.entity.CacheEntity
import com.chunjing.tq.db.entity.CalendarBgEntity
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.goodtech.weatherlib.BaseApp

const val TIME_HOUR = 60 * 60
const val TIME_DAY = TIME_HOUR * 24

class AppRepo {

    private val weatherBgDao: WeatherBgDao = AppDatabase.getInstance(BaseApp.context).weatherBgDao()
    private val calendarBgDao: CalendarBgDao = AppDatabase.getInstance(BaseApp.context).calendarBgDao()

    private val cacheDao: CacheDao = AppDatabase.getInstance(BaseApp.context).cacheDao()

    private val cityRepository: CityRepository = CityRepository.getInstance()
    //  搜索
    private val searchDao: CityDao = CityDatabase.getInstance(BaseApp.context).cityDao()

    suspend fun addWeatherBg(bg: WeatherBgEntity) {
        val code = weatherBgDao.saveWeatherBg(bg)
        Log.e("TAG", "addWeatherBg: $code")
    }

    /**
     * 获取天气背景
     * @param tempType 天气类型
     * @param timeType 时间类型
     */
    suspend fun getWeatherBg(tempType: String, timeType: String): WeatherBgEntity? {
        return weatherBgDao.getWeatherBg(tempType, timeType)
    }

    suspend fun getAllBgWeathers(): List<WeatherBgEntity> {
        return weatherBgDao.getAllBg()
    }

    suspend fun removeWeatherBg() {

    }

    suspend fun addCalendarBg(bg: CalendarBgEntity) {
        val code = calendarBgDao.saveCalendarBg(bg)
        Log.e("TAG", "addCalendarBg: $code")
    }

    /**
     * 获取日历背景
     */
    suspend fun getCalendarBg(holidayTime: String): CalendarBgEntity? {
        return calendarBgDao.getCalendarBg(holidayTime)
    }

    // 删除前定位城市
    suspend fun removeLocal() {
        cityRepository.removeLocal()
    }

    suspend fun addCity(city: CityEntity) {
        cityRepository.addCity(city)
    }

    suspend fun removeCity(cityId: String) {
        cityRepository.removeCity(cityId)
    }

    suspend fun removeAllCity() {
        cityRepository.removeAllCity()
    }

    suspend fun removeAllCityWithout(cityId: String) {
        cityRepository.removeAllCityWithout(cityId)
    }

    suspend fun updateCitiesOrder(cities: List<CityEntity>) {
        cityRepository.updateCitiesOrder(cities)
    }

    suspend fun getCity(cityId: String): CityEntity? {
        return cityRepository.getCity(cityId)
    }

    /**
     * 获取城市列表
     */
    suspend fun getCities(): List<CityEntity> {
        return cityRepository.getCities()
    }

    /**
     * 获取除定位外的城市列表
     */
    suspend fun getAdditionalCities(): List<CityEntity> {
        return cityRepository.getAdditionalCities()
    }

    suspend fun searchCity(name: String): List<CityEntity> {
        return searchDao.searchCities(name)
    }

    suspend fun searchCityWithId(cityId: String): CityEntity? {
        return searchDao.searchCity(cityId)
    }

    /**
     *  通过cityCode获取城市
     */
    suspend fun searchCities(cityCode: String): List<CityEntity> {
        return searchDao.searchCitiesWithCode(cityCode)
    }

    suspend fun deleteCache(key: String) {
        val cache = CacheEntity()
        cache.key = key
        cacheDao.deleteCache(cache)
    }

    suspend fun <T> saveCache(key: String, body: T) {
        saveCache(key, body, 0)
    }

    suspend fun <T> saveCache(key: String, body: T, saveTime: Int) {
        val cache = CacheEntity()
        cache.key = key
        cache.data = CacheCodec.encode(body as Any)
        if (saveTime == 0) {
            cache.dead_line = 0
        } else {
            cache.dead_line = System.currentTimeMillis() / 1000 + saveTime
        }
        cacheDao.saveCache(cache)
    }

    suspend fun <T> getCache(key: String): T? {
        val cache: CacheEntity? = cacheDao.getCache(key)
        val data = cache?.data ?: return null
        if (cache.dead_line != 0L && cache.dead_line <= System.currentTimeMillis() / 1000) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return CacheCodec.decode(data) as T?
    }

    companion object {
        private const val TAG = "AppRepo"

        @Volatile
        private var instance: AppRepo? = null

        @JvmStatic
        fun getInstance() =
            instance ?: synchronized(this) {
                instance
                    ?: AppRepo()
                        .also { instance = it }
            }
    }
}
