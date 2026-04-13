package com.chunjing.tq.ui.activity.vm

import androidx.lifecycle.MutableLiveData
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseViewModel


class TravelDetailViewModel : BaseViewModel() {

    val currentCity = MutableLiveData<CityEntity>()
    val travelCity = MutableLiveData<CityEntity>()
    var currentWeather = MutableLiveData<WeatherBean>()
    var travelWeather = MutableLiveData<WeatherBean>()

    fun setupCities(currentId: String, travelId: String) {
        launch {
            val curCity = AppRepo.getInstance().getCity(currentId)
            curCity?.let {
                currentCity.postValue(it)
                mainViewModel.getCityWeather(it.cityId)?.let { weather ->
                    currentWeather.postValue(weather)
                }
            }
            val travel = AppRepo.getInstance().searchCityWithId(travelId)
            travel?.let {
                travelCity.postValue(it)
                mainViewModel.getCityWeather(it.cityId)?.let { weather ->
                    travelWeather.postValue(weather)
                }
            }
        }
    }

}