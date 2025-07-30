package com.goodtech.tq.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils
import com.gengee.insaitlib.ui.base.BaseViewModel
import com.goodtech.tq.helpers.WeatherSpHelper
import com.goodtech.tq.httpClient.ApiCallback
import com.goodtech.tq.httpClient.ErrorCode
import com.goodtech.tq.httpClient.WeatherHttpHelper
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel

/**
 * 周边天气ViewModel
 */
class PeripheralViewModel(private val app: Application) : BaseViewModel(app) {

    val cities = MutableLiveData<List<CityMode>>()
    private val weatherMap = mutableMapOf<String, WeatherModel>()
    val weatherLiveData = MutableLiveData<Map<String, WeatherModel>>()

    fun setupCities(list: List<CityMode>) {
        launchSilent {
            cities.postValue(list)
        }
    }

    fun getWeatherData() {
        launchSilent {
            cities.value?.let {
                for ((index, city) in it.withIndex()) {
                    val weather = WeatherSpHelper.getWeatherModel(city.poiId)
                    if (weather != null) {
//                        if (TimeUtils.getTimeSpanByNow(weather.expireTime, TimeConstants.HOUR) != 0L) {
                        if (weather.expireTime < System.currentTimeMillis()) {
                            fetchWeatherData(city, index)
                        } else {
                            weatherMap[city.poiId] = weather
                            weatherLiveData.postValue(weatherMap)
                        }
                    } else {
                        fetchWeatherData(city, index)
                    }
                }
            }
        }
    }

    private fun fetchWeatherData(city: CityMode, index: Int) {
        WeatherHttpHelper.getInstance().getWeather(city, object : ApiCallback {
            override fun onResponse(success: Boolean, result: WeatherModel?, errCode: ErrorCode?) {
                result?.let { weather ->
                    weatherMap[city.poiId] = weather
                    weatherLiveData.postValue(weatherMap)
                }
            }

        })
    }

}