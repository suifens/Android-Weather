package com.chunjing.tq.db.dao

import androidx.room.*
import com.chunjing.tq.db.entity.CacheEntity
import com.chunjing.tq.db.entity.WeatherBgEntity

@Dao
interface WeatherBgDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveWeatherBg(cache: WeatherBgEntity): Long

    @Query("select *from WeatherImg where tempType=:tempType and timeType=:timeType")
    fun getWeatherBg(tempType: String, timeType: String): WeatherBgEntity?

    @Query("select *from WeatherImg")
    fun getAllBg(): List<WeatherBgEntity>

    @Delete
    fun deleteWeatherBg(cache: WeatherBgEntity): Int

    @Query("DELETE FROM WeatherImg")
    fun deleteAllImg(): Int
}