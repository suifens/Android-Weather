package com.chunjing.tq.ui.activity.vm

import androidx.lifecycle.MutableLiveData
import com.chunjing.tq.db.CityRepository
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ui.base.BaseViewModel

class CityManagerViewModel : BaseViewModel() {

    val cities = MutableLiveData<List<CityEntity>>()

    fun getCities() {
        launch {
            val results = CityRepository.getInstance().getAdditionalCities()
            cities.postValue(results)
        }
    }

    suspend fun removeCities(cityIds: List<String>) {
        val repo = CityRepository.getInstance()
        cityIds.forEach { repo.removeCity(it) }
    }

    suspend fun updateCitiesOrder(ordered: List<CityEntity>) {
        CityRepository.getInstance().updateCitiesOrder(ordered)
    }
}
