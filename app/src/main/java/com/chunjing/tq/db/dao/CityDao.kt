package com.chunjing.tq.db.dao

import androidx.room.*
import com.chunjing.tq.db.entity.CityEntity

@Dao
interface CityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCity(city: CityEntity): Long

    @Query("select * from city ORDER BY sortOrder ASC")
    fun getCities(): List<CityEntity>

    @Query("select * from city where cityId != '100000' ORDER BY sortOrder ASC")
    fun getCitiesWithoutLocation(): List<CityEntity>

    @Query("select * from city where cityId = :cityId limit 1")
    fun getCity(cityId: String): CityEntity?

    @Query("SELECT IFNULL(MAX(sortOrder), 0) FROM city")
    fun getMaxSortOrder(): Int

    @Query("UPDATE city SET sortOrder = :sortOrder WHERE cityId = :cityId")
    fun updateSortOrder(cityId: String, sortOrder: Int)

    @Transaction
    fun updateCitiesOrder(cities: List<CityEntity>) {
        cities.forEachIndexed { index, city ->
            val order = if (city.cityId == "100000") 0 else index + 1
            city.sortOrder = order
            updateSortOrder(city.cityId, order)
        }
    }

    @Query("delete from city where cityId=:id")
    fun removeCity(id: String)

    @Query("delete from city")
    fun removeAllCity()

    @Query("delete from city where cityId != :cityId")
    fun removeAllCityWithout(cityId: String)

    @Query("select * from city where mergerName like '%' || :name || '%' order by cityId")
    fun searchCities(name: String): List<CityEntity>

    @Query("select * from city where cityId = :cityId limit 1")
    fun searchCity(cityId: String): CityEntity?

    @Query("select * from city where cityCode = :cityCode order by cityId asc limit 0,10")
    fun searchCitiesWithCode(cityCode: String): List<CityEntity>
}
