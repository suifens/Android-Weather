package com.chunjing.tq.ui.activity.vm

import androidx.lifecycle.MutableLiveData
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.ui.base.BaseViewModel
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_NOW

class CityManagerViewModel : BaseViewModel() {

    val cities = MutableLiveData<List<CityEntity>>()

    fun getCities() {
        launch {
            val results = AppRepo.getInstance().getAdditionalCities()
            cities.postValue(results)
        }
    }

    fun removeCity(cityId: String) {
        launchSilent {
            AppRepo.getInstance().removeCity(cityId)
        }
    }

    fun updateCities(it: List<CityEntity>) {
        launchSilent {
            AppRepo.getInstance().removeAllCityWithout(LOCATION_ID)
            it.forEach {
                AppRepo.getInstance().addCity(it)
            }
        }
    }
}