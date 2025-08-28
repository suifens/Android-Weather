package com.chunjing.tq.ui.fragment.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseViewModel

class WeatherItemViewModel(app: Application) : BaseViewModel(app) {

    val weatherNow = MutableLiveData<WeatherBean>()
    val curCity = MutableLiveData<CityEntity>()
    val curBgEntity = MutableLiveData<WeatherBgEntity>()

    fun loadCache(cityId: String) {
        launchSilent {
            val city = AppRepo.getInstance().getCity(cityId)
            city?.let {
                curCity.postValue(it)
                AppRepo.getInstance().getCache<WeatherBean?>(CACHE_WEATHER_NOW + it.cityId)
                    ?.let { weather ->
                        weatherNow.postValue(weather)
                    }
            }
        }
    }

    fun loadData(cityId: String) {
        // 实时天气
        launchSilent {
            val city = AppRepo.getInstance().getCity(cityId)
            city?.let {
                curCity.postValue(it)
                mainViewModel.fetchWeather(it) { result ->
                    result?.let { weather ->
                        weatherNow.postValue(weather)
                    }
                }
            }
        }
    }

    //  获取背景
    fun getWeatherBgEntity(weather: WeatherBean) {
        launchSilent {
            mainViewModel.getWeatherBg(weather) { entity ->
                entity?.let {
                    curBgEntity.postValue(it)
                }
            }
        }
    }


}