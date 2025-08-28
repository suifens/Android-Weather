package com.chunjing.tq.bean.juhe

import java.io.Serializable

/**
 * com.chunjing.tq.bean.juhe
 * 聚合城市天气
 */
data class JuheWeather(
    val city: String,
    val realtime: Realtime,
    //  更新时间
    var updateTime: Long
) : Serializable {
    val aqi: Int
        get() = realtime.aqi.toInt()
}

data class Realtime(
    val aqi: String,
    val direct: String,
    val humidity: String,
    val info: String,
    val power: String,
    val temperature: String,
    val wid: String
) : Serializable
