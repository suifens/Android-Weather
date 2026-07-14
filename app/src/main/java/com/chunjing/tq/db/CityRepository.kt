package com.chunjing.tq.db

import com.chunjing.tq.db.dao.CityDao
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.goodtech.weatherlib.BaseApp

/**
 * 用户城市列表的读写与排序；搜索字典仍走 [AppRepo.searchCity]。
 */
class CityRepository private constructor(
    private val cityDao: CityDao = AppDatabase.getInstance(BaseApp.context).cityDao()
) {

    suspend fun addCity(city: CityEntity) {
        assignSortOrderIfNeeded(city)
        cityDao.addCity(city)
    }

    suspend fun removeCity(cityId: String) {
        cityDao.removeCity(cityId)
    }

    suspend fun removeAllCity() {
        cityDao.removeAllCity()
    }

    suspend fun removeAllCityWithout(cityId: String) {
        cityDao.removeAllCityWithout(cityId)
    }

    suspend fun getCity(cityId: String): CityEntity? = cityDao.getCity(cityId)

    suspend fun getCities(): List<CityEntity> = cityDao.getCities()

    suspend fun getAdditionalCities(): List<CityEntity> = cityDao.getCitiesWithoutLocation()

    /**
     * 按当前拖拽顺序写入 sortOrder（定位城不在列表内，保持 0）。
     */
    suspend fun updateCitiesOrder(cities: List<CityEntity>) {
        cityDao.updateCitiesOrder(cities)
    }

    suspend fun removeLocal() {
        cityDao.removeCity(LOCATION_ID)
    }

    private fun assignSortOrderIfNeeded(city: CityEntity) {
        val existing = if (!city.isLocal()) cityDao.getCity(city.cityId) else null
        city.sortOrder = CityListLogic.assignSortOrder(
            cityId = city.cityId,
            isLocal = city.isLocal(),
            existingOrder = existing?.sortOrder,
            maxSortOrder = cityDao.getMaxSortOrder()
        )
    }

    companion object {
        @Volatile
        private var instance: CityRepository? = null

        @JvmStatic
        fun getInstance(): CityRepository =
            instance ?: synchronized(this) {
                instance ?: CityRepository().also { instance = it }
            }
    }
}
