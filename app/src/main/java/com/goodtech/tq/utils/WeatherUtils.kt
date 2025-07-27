package com.goodtech.tq.utils

import com.goodtech.tq.R
import com.goodtech.tq.httpClient.ErrorCode

/**
 * com.goodtech.tq.utils
 */
object WeatherUtils {

    /**
     * 风速等级
     */
    @JvmStatic
    fun windGrade(windSpeed: Float): Int {
        return when {
            windSpeed <= 0.72f -> 0
            windSpeed <= 5.4f -> 1
            windSpeed <= 11.9f -> 2
            windSpeed <= 19.4f -> 3
            windSpeed <= 28.4f -> 4
            windSpeed <= 38.5f -> 5
            windSpeed <= 49.7f -> 6
            windSpeed <= 61.6f -> 7
            windSpeed <= 74.5f -> 8
            windSpeed <= 87.8f -> 9
            windSpeed <= 102.2f -> 10
            windSpeed <= 117.4f -> 11
            windSpeed <= 132.8f -> 12
            else -> 13
        }
    }

    @JvmStatic
    fun getRainfall(weather: String): Int {
        return when {
            weather.contains("暴雨") || weather.contains("暴雪") -> 100
            weather.contains("大雨") || weather.contains("大雪") -> 90
            weather.contains("中雨") || weather.contains("中雪") -> 80
            weather.contains("阵雨") || weather.contains("阵雪") -> 50
            weather.contains("雨") || weather.contains("雪") -> 70
            weather.contains("阴") -> 20
            else -> 0
        }
    }

    @JvmStatic
    fun weatherImageRes(iconCd: Int): Int {
        return when (iconCd) {
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

    @JvmStatic
    fun bgImageRes(iconCd: Int, night: Boolean): Int {
        return when (iconCd) {
            0, 19, 20, 21, 22 -> R.drawable.bg_wumai
            1, 2, 3, 4, 8, 9, 11, 12, 37, 38, 39, 40, 45, 47 -> {
                if (night) R.drawable.bg_yutian_night else R.drawable.bg_yutian
            }
            5, 6, 7, 10, 13, 14, 15, 16, 17, 18, 25, 35, 41, 42, 43, 46 -> {
                if (night) R.drawable.bg_xiaxue_night else R.drawable.bg_xiaxue
            }
            26, 27, 28, 29, 30 -> {
                if (night) R.drawable.bg_yintian_duoyun_night else R.drawable.bg_duoyun
            }
            //  晴
            23, 24, 31, 32, 33, 34, 36 -> {
                if (night) R.drawable.bg_qin_night else R.drawable.bg_qintian
            }
            44 -> {
                if (night) R.drawable.bg_yintian_duoyun_night else R.drawable.bg_yintian
            }
            else -> R.drawable.bg_normal
        }
    }
} 