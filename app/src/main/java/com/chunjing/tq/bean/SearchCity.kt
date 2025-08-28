package com.chunjing.tq.bean

import com.chunjing.tq.db.entity.CityEntity

data class SearchCity(
    val code: String,
    val location: List<CityEntity>
)

data class TopCity(
    val code: String,
    val topCityList: ArrayList<CityEntity>
)