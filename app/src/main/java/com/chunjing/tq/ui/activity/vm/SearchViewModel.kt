package com.chunjing.tq.ui.activity.vm

import androidx.lifecycle.MutableLiveData
import com.chunjing.tq.R
import com.chunjing.tq.bean.MessageEvent
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseViewModel
import com.goodtech.weatherlib.BaseApp
import org.greenrobot.eventbus.EventBus

class SearchViewModel : BaseViewModel() {

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
            val stringArray = BaseApp.context.resources.getStringArray(R.array.top_city)
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
            try {
                val existing = AppRepo.getInstance().getCity(it.cityId)
                if (existing == null) {
                    mainViewModel.addCityAndRefreshCities(it)
                    mainViewModel.awaitFetchWeather(it)
                    EventBus.getDefault().post(MessageEvent(cityChanged = true))
                }
            } catch (_: Exception) {
                // 写库失败时仍关闭添加页 loading，避免卡死
            } finally {
                addFinish.postValue(it.cityId)
                EventBus.getDefault().post(MessageEvent(selectedCityId = it.cityId))
            }
        }
    }

    fun updateLocation(city: CityEntity) {
        launchSilent {
            try {
                mainViewModel.addCityAndRefreshCities(city)
                mainViewModel.awaitFetchWeather(city)
                EventBus.getDefault().post(MessageEvent(cityChanged = true, selectedCityId = city.cityId))
            } catch (_: Exception) {
            }
        }
    }
}