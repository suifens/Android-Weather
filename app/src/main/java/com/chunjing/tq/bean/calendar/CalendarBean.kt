package com.chunjing.tq.bean.calendar

import com.blankj.utilcode.util.TimeUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

/**
 * com.chunjing.tq.bean.calendar
 */
data class CalendarBean<T>(
    val error_code: Int,
    val reason: String,
    val result: Result<T>?
)

data class Result<T>(
    val data: T
)

data class DayDetail(
    val animalsYear: String,
    val avoid: String,
    val date: String,
    val desc: String,
    val holiday: String,
    val lunar: String,
    val lunarYear: String,
    val suit: String,
    val weekday: String,
    @SerializedName("year-month")
    val yearMonth: String
) : Serializable
{

    @JvmName("getWeekday1")
    fun getWeekday(): String {
        when (weekday) {
            "星期一" -> return "周一"
            "星期二" -> return "周二"
            "星期三" -> return "周三"
            "星期四" -> return "周四"
            "星期五" -> return "周五"
            "星期六" -> return "周六"
            "星期日" -> return "周日"
        }
        return weekday
    }

    @JvmName("getSuit1")
    fun getSuit(): String {
        return if (suit.length > 8) {
            suit.substring(0, 8)
        } else suit
    }
}

data class HolidayBean(
    val year: String,
    val holiday: String,
    val holiday_array: List<Holiday>,
)

data class Holiday(
    val name: String,
    val festival: String,
    val desc: String,
    val rest: String,
    val list_num: Int,
    val list: List<HolidayDay>?
) : Comparable<Holiday>
{
    /**
     * 获取假期时长，status 为 1时 为放假
     */
    fun getHoliday(): Int {
        var count = 0
        list?.let {
            for (day in it) {
                if (day.status == "1") {
                    count++
                }
            }
        }
        return count
    }

    fun getTimeSpan(): String? {
        return if (list != null && list.isNotEmpty()) {
            if (list.size > 1) {
                val first: String = getFirstDay()!!.date
                val last: String = getLastDay()!!.date
                val firstYear = getYear(first)
                val lastYear = getYear(last)
                if (firstYear == lastYear) {
                    getMonthDay(first) + "-" + getMonthDay(last)
                } else {
                    getDate(first) + "-" + getDate(last)
                }
            } else {
                getMonthDay(list[0].date)
            }
        } else ""
    }

    private fun getFirstDay(): HolidayDay? {
        for (i in list!!.indices) {
            val day: HolidayDay = list[i]
            if (day.status == "1") {
                return day
            }
        }
        return null
    }

    private fun getLastDay(): HolidayDay? {
        for (i in list!!.indices.reversed()) {
            val day: HolidayDay = list[i]
            if (day.status == "1") {
                return day
            }
        }
        return null
    }


    fun getDate(): Date {
        val time = TimeUtils.string2Date(festival, "yyyy-MM-dd")
        println("compare $festival time $time")
        return time
    }

    fun getDay(): String? {
        return getMonthDay(festival)
    }

    private fun getYear(time: String): String {
        val strings = time.split("-").toTypedArray()
        return if (strings.isNotEmpty()) {
            strings[0]
        } else ""
    }

    private fun getMonthDay(time: String): String {
        val strings = time.split("-").toTypedArray()
        return if (strings.isNotEmpty()) {
            strings[1] + "月" + strings[2] + "日"
        } else ""
    }

    private fun getDate(time: String): String {
        val strings = time.split("-").toTypedArray()
        return if (strings.isNotEmpty()) {
            strings[0] + "年" + strings[1] + "月" + strings[2] + "日"
        } else ""
    }

    override fun compareTo(other: Holiday): Int {
        val date1 = this.getDate()
        val date2 = other.getDate()
        //  按照升序排列
        return if (date1.after(date2)) {
            1
        } else {
            -1
        }
//        return this.festival.compareTo(other.festival)
    }
}

data class HolidayDay(
    val date: String,
    val status: String
)
