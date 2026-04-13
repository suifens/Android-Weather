package com.chunjing.tq.ui.activity.vm

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseViewModel
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_NOW

/**
 * 周边天气ViewModel
 */
class PeripheralViewModel : BaseViewModel() {

    val cities = MutableLiveData<List<CityEntity>>()
    private val weatherMap = mutableMapOf<String, WeatherBean>()
    val weatherLiveData = MutableLiveData<Map<String, WeatherBean>>()

    fun setupCities(list: List<CityEntity>) {
        launchSilent {
            cities.postValue(list)
        }
    }

    fun getWeatherData() {
        launchSilent {
            cities.value?.let {
                for ((index, city) in it.withIndex()) {
                    val weather = AppRepo.getInstance().getCache<WeatherBean?>(CACHE_WEATHER_NOW + city.cityId)
                    if (weather != null) {
                        if (!TimeUtils.isToday(weather.updateTime)
                            || TimeUtils.getTimeSpanByNow(weather.updateTime, TimeConstants.HOUR) != 0L) {
                            fetchWeatherData(city, index)
                        } else {
                            weatherMap[city.cityId] = weather
                            weatherLiveData.postValue(weatherMap)
                        }
                    } else {
                        fetchWeatherData(city, index)
                    }
                }
            }
        }
    }

    private fun fetchWeatherData(city: CityEntity, index: Int) {
        mainViewModel.fetchWeather(city) { result ->
            result?.let { weather ->
                weather.updateTime = System.currentTimeMillis()
                weatherMap[city.cityId] = weather
                weatherLiveData.postValue(weatherMap)
            }
        }
    }

}