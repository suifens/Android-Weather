package com.chunjing.tq.utils

import android.text.TextUtils
import com.blankj.utilcode.util.TimeUtils
import com.goodtech.weatherlib.utils.SpUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * com.chunjing.tq.utils
 */
object WeatherSpUtils {

    @JvmStatic
    fun saveWeather(jsonObject: JSONObject?, cid: Int) {
        val key = String.format("weather_%d", cid)
        val timeKey = String.format("weather_%d_update", cid)
        if (jsonObject != null) {
            SpUtils.instance.putString(key, jsonObject.toString())
            SpUtils.instance.putLong(timeKey, System.currentTimeMillis())
        }
    }

    @JvmStatic
    fun getLastUpdate(cid: Int): Long {
        val timeKey = String.format("weather_%d_update", cid)
        return SpUtils.instance.getLong(timeKey, 0L)
    }

    /**
     * 获取当前定位
     */
//    fun getWeatherModel(cid: Int): WeatherModel? {
//        val weatherJson = getWeatherJson(cid)
//        val weatherModel: WeatherModel = WeatherHttpHelper.parseWeatherJson(weatherJson, cid)
//        if (weatherModel != null && !TextUtils.isEmpty(getJuheAqi(cid))) {
//            val airModel: JuheAirModel =
//                Gson().fromJson(getJuheAqi(cid), object : TypeToken<JuheAirModel?>() {}.type)
//            if (airModel != null) {
//                weatherModel.aqi = airModel.getAqi().toInt()
//            }
//        }
//        if (weatherModel != null && !TextUtils.isEmpty(getCityLife(cid))) {
//            val lifeModel: JuheLifeModel =
//                Gson().fromJson(getCityLife(cid), object : TypeToken<JuheLifeModel?>() {}.type)
//            if (lifeModel != null) {
//                weatherModel.lifeModel = lifeModel
//            }
//        }
//        return weatherModel
//    }

    fun getWeatherJson(cid: Int): JSONObject? {
        val key = String.format("weather_%d", cid)
        val json: String = SpUtils.instance.getString(key, "")
        if (!TextUtils.isEmpty(json)) {
            try {
                return JSONObject(json)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun saveJuheAqi(jsonObject: String?, cid: Int) {
        val key = String.format("aqi_%d", cid)
        val timeKey = String.format("aqi_%d_update", cid)
        if (jsonObject != null) {
            SpUtils.instance.putString(key, jsonObject)
            SpUtils.instance.putLong(timeKey, System.currentTimeMillis())
        }
    }

    fun getJuheAqi(cid: Int): String? {
        val key = String.format("aqi_%d", cid)
        return SpUtils.instance.getString(key, "")
    }

    fun saveCityLife(jsonObject: String?, cid: Int) {
        val key = String.format("%s_life_%d", ContentUtil.todayStr, cid)
        val timeKey = String.format("life_%d_update", cid)
        if (jsonObject != null) {
            SpUtils.instance.putString(key, jsonObject)
        }
    }

    fun getCityLife(cid: Int): String? {
        val key = String.format("%s_life_%d", ContentUtil.todayStr, cid)
        return SpUtils.instance.getString(key, "")
    }

    fun deleteWeatherModel(cid: Int) {
        val key = String.format("weather_%d", cid)
        SpUtils.instance.remove(key)
    }
}