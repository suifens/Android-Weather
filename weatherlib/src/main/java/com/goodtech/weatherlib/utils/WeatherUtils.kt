package com.goodtech.weatherlib.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.goodtech.weatherlib.R
import com.goodtech.weatherlib.ext.BgTimeType

/**
 * com.goodtech.weatherlib.utils
 */
@SuppressLint("UseCompatLoadingForDrawables")
object WeatherUtils {

    //  使用类型
    fun getUsingType(isHome: Boolean): String {
        return if (isHome) "首页" else "详细页"
    }

    //  时间类型
    fun getTimeType(isDay: Boolean): String {
        return if (isDay) "白天" else "晚上"
    }

    //  时间类型
    fun getTimeType(timeType: BgTimeType): String {
        return when (timeType) {
            BgTimeType.SunRise -> "日出"
            BgTimeType.Day -> "白天"
            BgTimeType.SunSet -> "日落"
            BgTimeType.Night -> "晚上"
        }
    }

    //  天气类型
    fun getTempType(wxIcon: Int): String {
        return when (wxIcon) {
            32, 34, 36,
            31, 33 -> "晴"
            28, 30,
            27, 29 -> "多云"
            37,
            45 -> "阵雨"
            11, 8, 9 -> "小雨"
            5, 6, 7, 10, 35, 39 ->"中雨"
            12, 40 ->"大雨"
            3, 4, 38,
            47 ->"雷阵雨"
            13, 14, 16, 17, 18, 41, 42, 43, 46 ->"下雪"
            0, 26 ->"阴天"
            else -> "特殊天气"
        }
    }

    @JvmStatic
    fun getTempIcon(context: Context, icon_cd: Int): Drawable? {
        val icon = getIcon(icon_cd)
        return AppCompatResources.getDrawable(context, icon)
    }

    @JvmStatic
    fun getIcon(icon_cd: Int): Int {
        return when (icon_cd) {
            0 -> R.drawable.icon0
            1 -> R.drawable.icon1
            2 -> R.drawable.icon2
            3 -> R.drawable.icon3
            4 -> R.drawable.icon4
            5 -> R.drawable.icon5
            6 -> R.drawable.icon6
            7 -> R.drawable.icon7
            8 -> R.drawable.icon8
            9 -> R.drawable.icon9
            10 -> R.drawable.icon10
            11 -> R.drawable.icon11
            12 -> R.drawable.icon12
            13 -> R.drawable.icon13
            14 -> R.drawable.icon14
            15 -> R.drawable.icon15
            16 -> R.drawable.icon16
            17 -> R.drawable.icon17
            18 -> R.drawable.icon18
            19 -> R.drawable.icon19
            20 -> R.drawable.icon20
            21 -> R.drawable.icon21
            22 -> R.drawable.icon22
            23 -> R.drawable.icon23
            24 -> R.drawable.icon24
            25 -> R.drawable.icon25
            26 -> R.drawable.icon26
            27 -> R.drawable.icon27
            28 -> R.drawable.icon28
            29 -> R.drawable.icon29
            30 -> R.drawable.icon30
            31 -> R.drawable.icon31
            32 -> R.drawable.icon32
            33 -> R.drawable.icon33
            34 -> R.drawable.icon34
            35 -> R.drawable.icon35
            36 -> R.drawable.icon36
            37 -> R.drawable.icon37
            38 -> R.drawable.icon38
            39 -> R.drawable.icon39
            40 -> R.drawable.icon40
            41 -> R.drawable.icon41
            42 -> R.drawable.icon42
            43 -> R.drawable.icon43
            44 -> R.drawable.icon44
            45 -> R.drawable.icon45
            46 -> R.drawable.icon46
            else -> R.drawable.icon47
        }
    }

    /**
     * 风速等级
     */
    fun windGrade(windSpeed: Int): Int {
        return if (windSpeed <= 0.72) {
            0
        } else if (windSpeed <= 5.4) {
            1
        } else if (windSpeed <= 11.9) {
            2
        } else if (windSpeed <= 19.4) {
            3
        } else if (windSpeed <= 28.4) {
            4
        } else if (windSpeed <= 38.5) {
            5
        } else if (windSpeed <= 49.7) {
            6
        } else if (windSpeed <= 61.6) {
            7
        } else if (windSpeed <= 74.5) {
            8
        } else if (windSpeed <= 87.8) {
            9
        } else if (windSpeed <= 102.2) {
            10
        } else if (windSpeed <= 117.4) {
            11
        } else if (windSpeed <= 132.8) {
            12
        } else {
            13
        }
    }

}