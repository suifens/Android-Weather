package com.chunjing.tq.bean

import com.chunjing.tq.db.entity.WeatherBgEntity
import java.io.Serializable

/**
 * com.chunjing.tq.bean
 */
data class WeatherBgBean(
    val endTime: String,
    val updateTime: String,
    val imgList: List<WeatherBgEntity>,
    val startTime: String
) : Serializable