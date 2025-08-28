package com.chunjing.tq.bean.calendar

/**
 * com.chunjing.tq.bean.calendar
 */
data class AlmanacBean<T>(
    val error_code: Int,
    val reason: String,
    val result: T?
)

/**
 * 老黄历、日历
 */
data class DayAlmanac(
    val baiji: String,
    val chongsha: String,
    val id: String,
    val ji: String,
    val jishen: String,
    val wuxing: String,
    val xiongshen: String,
    val yangli: String,
    val yi: String,
    val yinli: String
)

/**
 * 老黄历、时辰
 */
data class HourAlmanac (
    val des: String,
    val hours: String,
    val ji: String,
    val yangli: String,
    val yi: String
) {
    var time: String = ""
    fun getHourString() : String {
        val times = hours.split("-")
        val startTime = if (times[0].toInt() >= 10) times[0] else "0${times[0]}"
        val endTime = if (times[1].toInt() >= 10) times[1] else "0${times[1]}"
        return "$startTime:00-$endTime:00"
    }
}


