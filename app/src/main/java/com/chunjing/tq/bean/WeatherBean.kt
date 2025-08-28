package com.chunjing.tq.bean

import com.blankj.utilcode.util.TimeUtils
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.WeatherUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * com.chunjing.tq.bean
 */
data class WeatherBean(

    val conditionsshort: Conditionsshort,
    val fcstdaily10short: Fcstdaily10short,
    val fcsthourly24short: Fcsthourly24short,
    //  更新时间
    var updateTime: Long

) : Serializable
{
    val observation: Observation
        get() = conditionsshort.observation

    //  24小时天气
    val hourlies: List<Hourly>
        get() = fcsthourly24short.forecasts

    //  10天天气
    val dailies: ArrayList<Daily>
        get() = fcstdaily10short.forecasts

    val metric: Metric
        get() = observation.metric

    //  获取天气图标
    fun getIconCd(): Int {
        return observation.wxIcon
    }

    //  获取当前的天气
    fun getCurrentTemp(): Int {
        return observation.metric.temp
    }

    fun getPhrase(): String {
        return "${observation.wdirCardinal}风" +
                "${WeatherUtils.windGrade(observation.metric.wspd)}级 | 湿度${observation.rh}%"
    }

    fun today(): Daily? {
        if (dailies.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            val currentLoc: String = TimeUtils.millis2String(currentTime, "yyyy-MM-dd")
            for (i in dailies.indices) {
                val daily = dailies[i]
                val dayLocal = daily.fcst_valid_local
                if (dayLocal.contains(currentLoc)) {
                    return daily
                }
            }
        }
        return null
    }

    fun tomorrow(): Daily? {
        if (dailies.isNotEmpty()) {
            val time = System.currentTimeMillis() + 1000 * 3600 * 24
            val timeLoc: String = TimeUtils.millis2String(time, "yyyy-MM-dd")
            for (i in dailies.indices) {
                val daily = dailies[i]
                val dayLocal = daily.fcst_valid_local
                if (dayLocal.contains(timeLoc)) {
                    return daily
                }
            }
        }
        return null
    }

    /**
     * 是否是白天
     */
    fun timeType(): com.goodtech.weatherlib.ext.BgTimeType {
        today()?.let {
            val current = System.currentTimeMillis()
            val sunRise = DateUtil.switchTime(it.sunRise)
            val sunSet = DateUtil.switchTime(it.sunSet)

            val halfHour = 30 * 60 * 1000
            if (current in (sunRise - halfHour) until (sunRise + halfHour)) {
                return com.goodtech.weatherlib.ext.BgTimeType.SunRise
            }
            if (current in (sunSet - halfHour) until (sunSet + halfHour)) {
                return com.goodtech.weatherlib.ext.BgTimeType.SunSet
            }
            if (current in (sunRise + 1) until sunSet) {
                return com.goodtech.weatherlib.ext.BgTimeType.Day
            }
        }
        return com.goodtech.weatherlib.ext.BgTimeType.Night
    }

    fun isDay(): Boolean {
        today()?.let {
            val current = System.currentTimeMillis()
            val sunRise = DateUtil.switchTime(it.sunRise)
            val sunSet = DateUtil.switchTime(it.sunSet)

            return current in (sunRise + 1) until sunSet
        }
        return false
    }
}

data class Conditionsshort(
    val observation: Observation
) : Serializable

data class Fcstdaily10short(
    val forecasts: ArrayList<Daily>
) : Serializable

data class Fcsthourly24short(
    val forecasts: List<Hourly>
) : Serializable

data class Observation(
    @SerializedName("valid_time_gmt")
    val validTime: Long = 0,

    @SerializedName("metric")
    val metric: Metric,

    @SerializedName("obs_id")
    val obsId: String = "",

    @SerializedName("obs_name")
    val obsName: String = "",

    @SerializedName("pressure_desc")
    val pressureDesc: String = "",

    @SerializedName("pressure_tend")
    val pressureTend: Int = 0,
    val rh: Int = 0, //  湿度

    //  紫外线
    @SerializedName("uv_desc")
    val uvDesc: String = "",

    @SerializedName("uv_index")
    val uvIndex: Int = 0,//  紫外线指数

    @SerializedName("wdir")
    val wdir: Int = 0,

    //  "东北偏东"
    @SerializedName("wdir_cardinal")
    val wdirCardinal: String = "",

    @SerializedName("wx_icon")
    val wxIcon: Int = 0,

    @SerializedName("wx_phrase")
    val wxPhrase: String = ""

) : Serializable {
    fun getWxcPhrase(): String {
        return if (wxIcon == 26) return "阴天"
        else wxPhrase
    }
}

data class Metric(
    val gust: Int = 0,
    @SerializedName("dewpt")
    val dewpt: Int = 0, //  露点

    @SerializedName("feels_like")
    val feelsLike: Int = 0,

    @SerializedName("max_temp")
    val maxTemp: Int = 0, //  最高温

    @SerializedName("min_temp")
    val minTemp: Int = 0, //  最低温

    @SerializedName("precip_total")
    val precipTotal: Float = 0f,

    @SerializedName("pressure")
    val pressure: Float = 0f, //  气压

    @SerializedName("temp")
    val temp: Int = 0, //  温度

    @SerializedName("vis")
    val vis: Float = 0f, //  能见度

    @SerializedName("wspd")
    val wspd: Int = 0, //  风速 km/h
) : Serializable

data class Hourly(
    @SerializedName("num")
    val num: Int = 0,
    @SerializedName("day_ind")
    val dayInd: String = "",

    @SerializedName("dow")
    val dow: String = "",

    @SerializedName("metric")
    val metric: Metric,

    @SerializedName("fcst_valid")
    var fcst_valid: Long = 0,

    @SerializedName("fcst_valid_local")
    val fcst_valid_local: String = "",

    @SerializedName("icon_cd")
    var icon_cd: Int = 0,

    @SerializedName("icon_extd")
    val icon_extd: Int = 0,

    @SerializedName("phrase_32char")
    var phraseChar: String = "",

    @SerializedName("pop")
    val pop: Int = 0,

    @SerializedName("precip_type")
    val precip_type: String = "",

    //  相对湿度
    @SerializedName("rh")
    val rh: Int = 0,

    //  紫外线
    @SerializedName("uv_desc")
    val uv_desc: String = "",

    @SerializedName("uv_index")
    val uv_index: Int = 0,

    //  风向
    @SerializedName("wdir")
    val wdir: Int = 0,

    @SerializedName("wdir_cardinal")
    val wdir_cardinal: String = "",

    val sunrise: Boolean = false,
    val sunset: Boolean = false

) : Serializable {
    //  时间
    val time: String
        get() {
            val timestamp = fcst_valid * 1000
            val hour = TimeUtils.millis2String(timestamp, "HH").toInt()
//            val curTime = TimeUtils.millis2String(System.currentTimeMillis(), "HH").toInt()
//            return if (curTime == hour) {
//                "现在"
//            } else {
                return "${hour}时"
//            }
        }

    val temp: Int
        get() = metric.temp

    fun getPhrasesChar(): String {
        return if (icon_cd == 26) return "阴天"
        else phraseChar
    }
}

data class Daily(
    @SerializedName("num")
    val num: Int = 0,

    @SerializedName("dow")
    val dow: String = "",

    @SerializedName("fcst_valid")
    val fcst_valid: Long = 0,

    @SerializedName("fcst_valid_local")
    val fcst_valid_local: String = "",

    @SerializedName("metric")
    val metric: Metric? = null,

    @SerializedName("moonrise")
    val moonRise: String = "",

    @SerializedName("moonset")
    val moonSet: String = "",

    @SerializedName("moon_phase")
    val moon_phase: String = "",

    @SerializedName("moon_phase_code")
    val moon_phase_code: String = "",

    @SerializedName("sunrise")
    val sunRise: String = "",

    @SerializedName("sunset")
    val sunSet: String = "",

    @SerializedName("day")
    val dayPart: Daypart? = null,

    @SerializedName("night")
    val nightPart: Daypart? = null,

) : Serializable {

    val weatherPart: Daypart?
        get() {
            if (dayPart != null) return dayPart
            if (nightPart != null) return nightPart
            return null
        }

    val minTemp: Int
        get() {
            metric?.let { return metric.minTemp }
            return 0
        }

    val maxTemp: Int
        get() {
            metric?.let { return metric.maxTemp }
            return 0
        }

    val time: String
        get() = TimeUtils.millis2String(fcst_valid * 1000, "MM月dd日")
}

data class Daypart(
    /**
     * day 1 ; night 2
     */
    @SerializedName("num")
    val num: Int = 0,

    @SerializedName("daypart_name")
    val name: String = "",

//  周日晚间、周日白天
    @SerializedName("long_daypart_name")
    val longName: String = "",

//  时间 1590318000
    @SerializedName("fcst_valid")
    val fcst_valid: Long = 0,

//  29
    @SerializedName("icon_cd")
    val iconCd: Int = 0,

    @SerializedName("icon_extd")
    val iconExtd: Int = 0,

//  局部多云
    @SerializedName("phrase_32char")
    val phraseChar: String = "",

//  "rain"
    @SerializedName("precip_type")
    val precipType: String = "",

//  20
    @SerializedName("pop")
    val pop: Int = 0,

//  湿度 87
    @SerializedName("rh")
    val rh: Int = 0,

//  紫外线
//  "低"
    @SerializedName("uv_desc")
    val uvDesc: String = "",

//  0
    @SerializedName("uv_index")
    val uvInde: Int = 0,

//  69
    @SerializedName("wdir")
    val wdir: Int = 0,

//  "东北偏东"
    @SerializedName("wdir_cardinal")
    val wdirCardinal: String = "",

    @SerializedName("metric")
    val metric: Metric,

) : Serializable {
    // 5 风速
    val wspd: Int
        get() = metric.wspd

    val temp: Int
        get() = metric.temp

    fun getPhrasesChar(): String {
        return if (iconCd == 26) return "阴天"
        else phraseChar
    }
}

