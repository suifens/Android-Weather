package com.chunjing.tq.ui.activity.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.MessageEvent
import com.chunjing.tq.bean.WeatherBgBean
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseViewModel
import com.goodtech.weatherlib.net.HttpUtils
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus

class SearchViewModel(private val app: Application) : BaseViewModel(app) {

    val searchResult = MutableLiveData<List<CityEntity>>()

    val choseCity = MutableLiveData<CityEntity>()

    val topCity = MutableLiveData<List<CityEntity>>()

    val addFinish = MutableLiveData<String>()

    /**
     * 搜索城市
     */
    fun searchCity(keywords: String) {
        launchSilent {
            val cities = AppRepo.getInstance().searchCity(keywords)
            cities.let {
                searchResult.postValue(it)
            }
        }
    }

    /**
     * 获取热门城市
     */
    fun getTopCity() {
        launchSilent {
            val stringArray = app.resources.getStringArray(R.array.top_city)
            val cityList = ArrayList<CityEntity>()
            for (name in stringArray) {
                val cities = AppRepo.getInstance().searchCity(name)
                if (cities.isNotEmpty()) {
                    cityList.add(cities.first())
                }
            }

            topCity.postValue(cityList)
        }
    }

    /**
     * 添加城市
     */
    fun addCity(it: CityEntity) {
        launchSilent {
            val city = AppRepo.getInstance().getCity(it.cityId)
            if (city == null) {
                mainViewModel.addCity(it)
                mainViewModel.fetchWeather(it)
//                mainViewModel.getCitiesCache()
                delay(1000L)
                EventBus.getDefault().post(MessageEvent(cityChanged = true))
            }

            addFinish.postValue(it.cityId)
            EventBus.getDefault().post(MessageEvent(selectedCityId = it.cityId))
        }
    }

    fun updateLocation(city: CityEntity) {
        launchSilent {
            mainViewModel.addCity(city)
            mainViewModel.fetchWeather(city)
//            mainViewModel.getCitiesCache()
            EventBus.getDefault().post(MessageEvent(cityChanged = true, selectedCityId = city.cityId))
        }
    }
}