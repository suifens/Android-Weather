package com.goodtech.weatherlib.utils

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * com.goodtech.weatherlib.utils
 */
@SuppressLint("SimpleDateFormat")
object CalendarUtil {

    fun switchTime(time: String): Long {
        var timestamp: Long = 0
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") //注意格式化的表达式
        try {
            val formatTime = format.parse(time)
            val date = formatTime.toString()
            //将西方形式的日期字符串转换成java.util.Date对象
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
            val datetime = sdf.parse(date) as Date
            //再转换成自己想要显示的格式
            timestamp = dateToLong(datetime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (time.contains("+0600")) {
            timestamp += (2 * 60 * 60 * 1000).toLong()
        } else if (time.contains("+0700")) {
            timestamp += (60 * 60 * 1000).toLong()
        }
        return timestamp
    }

    /**
     * @param strTime    要转换的string类型的时间，
     * @param formatType 要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
     * @return Date
     */
    fun stringToDate(strTime: String, formatType: String): Date? {
        val formatter = SimpleDateFormat(formatType)
        var date: Date? = null
        try {
            date = formatter.parse(strTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date
    }

    fun longWithDate(strTime: String, formatType: String): Long {
        val date = stringToDate(strTime, formatType)
        return date?.time ?: 0
    }

    fun dateToLong(date: Date): Long {
        return date.time
    }

    fun timeToHH(timeMills: Long): String {
        val format = SimpleDateFormat("HH")
        //        if (timeStr.equals("00")) {
//            return "24";
//        }
        return format.format(timeMills)
    }

    fun timeToHHmm(timeMills: Long): String {
        val format = SimpleDateFormat("HH:mm")
        return format.format(timeMills)
    }

    fun getNowTime(): String {
        return timeToHHmm(System.currentTimeMillis())
    }

    fun timeToDay(strTime: String, formatType: String): String {
        val timeMills = longWithDate(strTime, formatType)
        val dateFormat = SimpleDateFormat("dd")
        return dateFormat.format(timeMills)
    }

    fun timeToDay(timeMills: Long): String {
        val dateFormat = SimpleDateFormat("dd")
        return dateFormat.format(timeMills)
    }

    fun longToString(timeMills: Long, pattern: String): String {
        val format = SimpleDateFormat(pattern)
        return format.format(timeMills)
    }

    fun timeToString(timeMills: Long, pattern: String): String {
        val dateFormat = SimpleDateFormat(pattern)
        return dateFormat.format(timeMills)
    }

    /**
     * 之后是否还有假期
     */
    fun afterHoliday(timeMills: Long): Boolean {
        val month = longToString(timeMills, "MM").toInt()
        return if (month == 10) {
            val day = longToString(timeMills, "dd").toInt()
            day > 7
        } else {
            month > 10
        }
    }

    /**
     * 获取年份
     */
    fun getYear(timeMills: Long): Int {
        val year = longToString(timeMills, "yyyy")
        return year.toInt()
    }

    fun getMonth(timeMills: Long): Int {
        val year = longToString(timeMills, "MM")
        return year.toInt()
    }

    fun getDay(timeMills: Long): Int {
        val year = longToString(timeMills, "dd")
        return year.toInt()
    }

    fun getYearWeek(date: Date): Int {
        val cal =
            Calendar.getInstance() //这一句必须要设置，否则美国认为第一天是周日，而我国认为是周一，对计算当期日期是第几周会有错误
        val weekYear = cal[Calendar.YEAR] //获得当前的年
        cal[weekYear, 0] = 1 // 每周从周一开始
        cal.time = date
        return cal[Calendar.WEEK_OF_YEAR]
    }

    /**
     * 获取某天的00点
     */
    fun getZoneTime(timeMillis: Long): Long {
        val day = longToString(timeMillis, "yyyy-MM-dd")
        return longWithDate(day, "yyyy-MM-dd")
    }

    /**
     * 是否是今天
     */
    fun isCurrentDay(timeMillis: Long): Boolean {
        val zoneTime = getZoneTime(timeMillis)
        val curZoneTime = getZoneTime(System.currentTimeMillis())
        return zoneTime == curZoneTime
    }

    /**
     * 是否为白天时间 [4:00 ~ 18:00)
     */
    fun isDaytime(timeMillis: Long): Boolean {
        val zoneTime = getZoneTime(timeMillis)
        val startTime = zoneTime + 4 * getHourMillis()
        val endTime = zoneTime + 17 * getHourMillis()
        return timeMillis in startTime until endTime
    }

    fun getHourMillis(): Long {
        return 60 * 60 * 1000
    }

    fun getWeek(time: String): String {
        var week = ""
        val format = SimpleDateFormat("yyyy-MM-dd")
        val c = Calendar.getInstance()
        try {
            c.time = format.parse(time)!!
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val wek = c[Calendar.DAY_OF_WEEK]
        if (wek == 1) {
            week += "周日"
        }
        if (wek == 2) {
            week += "周一"
        }
        if (wek == 3) {
            week += "周二"
        }
        if (wek == 4) {
            week += "周三"
        }
        if (wek == 5) {
            week += "周四"
        }
        if (wek == 6) {
            week += "周五"
        }
        if (wek == 7) {
            week += "周六"
        }
        return week
    }

}