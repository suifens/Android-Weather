package com.chunjing.tq.utils

import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.MyApp
import com.chunjing.tq.bean.CityCode
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ext.REMINDER_WEATHER
import com.goodtech.weatherlib.BaseApp
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Created by niuchong on 2019/4/7.
 */
object ContentUtil {
    //应用设置里的文字
    //    public static String SYS_LANG = "zh";

    /*@JvmField
    var UNIT_CHANGE = false*/

    //  当前时间
    val todayStr: String
        get() {
            val date = TimeUtils.getNowDate()
            return TimeUtils.date2String(date, "yyyyMMdd")
        }

    //  是否同意权限
    var permissionGranted: Boolean
        get() = SpUtils.instance.getBoolean("permissionGranted", false)
        set(value) {
            SpUtils.instance.putBoolean("permissionGranted", value)
        }

    @JvmField
    var CITY_CHANGE = false

    @JvmField
    var visibleHeight = 0

    @JvmField
    var screenHeight = 0

    @JvmField
    var travelCity: CityEntity? = null

    @JvmStatic
    fun getCityCodes(context: Context?): ArrayList<CityCode> {
        val cityModes: ArrayList<CityCode> = ArrayList()
        val cityJson: String = Utils.getJson("cityCode.json", context)
        val jsonElement = Gson().fromJson(cityJson, JsonObject::class.java)
        if (jsonElement != null) {
            val jsonArray = Gson().fromJson(jsonElement["citys"], JsonArray::class.java)
            val list: ArrayList<CityCode> = Gson().fromJson(
                jsonArray,
                object : TypeToken<ArrayList<CityCode?>?>() {}.type
            )
            cityModes.addAll(list)
        }
        return cityModes
    }

    /**
     * 获取城市code
     */
    @JvmStatic
    fun getCityCode(cityName: String) : String? {

        if (!SpUtils.instance.getBoolean(REMINDER_WEATHER, true)) {
            //  不添加提醒
            return null
        }

        var cityCode: String? = null
        val list: ArrayList<CityCode> = getCityCodes(BaseApp.context)
        for (cityCodeMode in list) {
            if (cityCodeMode.city_name.contains(cityName)) {
                cityCode = cityCodeMode.city_code
                break
            }
        }

        return cityCode
    }

    @JvmStatic
    fun getLifeCityName(cityName: String?): String? {
        var temp = cityName
        if (!TextUtils.isEmpty(cityName)) {
            if (cityName!!.endsWith("市")) {
                temp = cityName.replace("市", "")
            }
            if (cityName.endsWith("县")) {
                temp = cityName.replace("县", "")
            }
            if (cityName.endsWith("区")) {
                temp = cityName.replace("区", "")
            }
        }
        return temp
    }

    fun getVideoDir(): String {
        val file: File = MyApp.instance().externalCacheDir!!.absoluteFile
        val appDir = File(file, "video")
        if (!FileUtils.createOrExistsDir(appDir)) {
            val mkdirs = appDir.mkdirs()
            Log.e("TAG", "------ onResponse: $mkdirs" )
        }
        return appDir.absolutePath
    }
}