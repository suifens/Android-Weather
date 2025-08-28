package com.goodtech.weatherlib.utils

import com.blankj.utilcode.util.TimeUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object DateUtil {

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
            timestamp = datetime.time
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

    @JvmStatic
    fun getNowHour(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    /**
     * 一天的时间戳
     */
    fun dayMillis(): Long {
        return 1000 * 60 * 60 * 24;
    }

    /**
     * HH:mm
     */
    fun getNowTime(): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        // 获取当前时间
        val date = Date(System.currentTimeMillis());
        return simpleDateFormat.format(date)
    }

    fun timeToDay(timeMills: Long): String {
        val dateFormat = SimpleDateFormat("dd")
        return dateFormat.format(timeMills)
    }

    @JvmStatic
    fun getWeek(time: Long): String {
        val timeDay = TimeUtils.millis2String(time, "dd").toInt()
        val today = TimeUtils.millis2String(System.currentTimeMillis(), "dd").toInt()
        return when (timeDay - today) {
            -1 -> "昨天"
            0 -> "今天"
            1 -> "明天"
            else -> TimeUtils.getChineseWeek(time)
        }
    }

    /**
     * 获取昨天月日
     */
    @JvmStatic
    fun getYesterday() : String {
        val yesterday = System.currentTimeMillis() - dayMillis()
        return TimeUtils.millis2String(yesterday, "MM月dd日")
    }

    /**
     * 获取星期
     *
     * @param num 0-6
     * @return 星期
     */
    fun getWeek(num: Int): String {
        var week = " "
        when (num) {
            1 -> week = "周一"
            2 -> week = "周二"
            3 -> week = "周三"
            4 -> week = "周四"
            5 -> week = "周五"
            6 -> week = "周六"
            7 -> week = "周日"
        }
        return week
    }

    fun getWeek(dow: String): String {

        return when (dow) {
            "星期一" ->  "周一"
            "星期二" ->  "周二"
            "星期三" ->  "周三"
            "星期四" ->  "周四"
            "星期五" ->  "周五"
            "星期六" ->  "周六"
            "星期天" ->  "周日"
            else -> "周一"
        }
    }

    fun getSimpleMonth(month: Int): String {
        return when (month) {
            2 -> "Feb."
            3 -> "Mar."
            4 -> "Apr."
            5 -> "May."
            6 -> "Jun."
            7 -> "Jul."
            8 -> "Aug."
            9 -> "Sept."
            10 -> "Oct."
            11 -> "Nov."
            12 -> "Dec."
            else -> "Jan."
        }
    }


}