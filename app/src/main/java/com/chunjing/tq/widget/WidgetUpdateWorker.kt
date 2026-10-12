package com.chunjing.tq.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.MyApp
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.bean.juhe.JuheAlarmBean
import com.chunjing.tq.bean.juhe.JuheBean
import com.chunjing.tq.bean.juhe.JuheWeather
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.WeatherRepository
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ext.JUHE_ALARM
import com.chunjing.tq.ext.JUHE_QUERY
import com.chunjing.tq.ext.REMINDER_WEATHER
import com.chunjing.tq.jpush.JPushHelper
import com.chunjing.tq.ui.fragment.vm.CACHE_JUHE_WEATHER
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.Utils
import com.chunjing.tq.bean.CityCode
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

/**
 * 小组件天气刷新 Worker（A 方案）。
 * 过期才拉网 → 成功后 [WidgetRenderer.updateWidgetsIfNecessary]；失败不覆盖桌面。
 */
class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "WidgetUpdateWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val STALE_MS = 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        val context = applicationContext
        WidgetWorkScheduler.clearLegacyWeatherNotifications(context)

        if (!WidgetWorkScheduler.hasAnyWidget(context)) {
            Log.d(TAG, "无 widget，跳过")
            return Result.success()
        }

        val city = WidgetRenderer.resolveWidgetCity()
        if (city == null || city.latitude.isBlank() || city.longitude.isBlank()) {
            Log.d(TAG, "无可用城市/经纬度，跳过")
            return Result.success()
        }

        val repo = WeatherRepository.getInstance()
        val cached = repo.getCachedWeather(city.cityId)
        if (cached != null) {
            WidgetRenderer.updateWidgetsIfNecessary(context)
        }

        if (cached != null && !cached.needReload()) {
            Log.d(TAG, "缓存未过期，跳过拉网")
            return Result.success()
        }

        val fetched = try {
            repo.fetchAndCache(city)
        } catch (e: Exception) {
            Log.e(TAG, "拉天气失败", e)
            null
        }

        if (fetched == null) {
            return if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.success()
            }
        }

        refreshJuheAqi(city)
        WidgetRenderer.updateWidgetsIfNecessary(context)
        checkAlarmIfNeeded(context, city)
        return Result.success()
    }

    private fun WeatherBean.needReload(): Boolean {
        if (updateTime <= 0L) return true
        val now = System.currentTimeMillis()
        if (now - updateTime > STALE_MS) return true
        val curHour = TimeUtils.millis2String(now, "HH")
        val lastHour = TimeUtils.millis2String(updateTime, "HH")
        return curHour != lastHour
    }

    private suspend fun refreshJuheAqi(city: CityEntity) {
        val cityCode = ContentUtil.getLifeCityName(city.cityName) ?: return
        if (cityCode.isEmpty()) return
        try {
            val url = String.format(JUHE_QUERY, cityCode)
            val queryResult = HttpUtils.get<JuheBean<JuheWeather>>(url)
            queryResult?.result?.let { weather ->
                weather.updateTime = System.currentTimeMillis()
                AppRepo.getInstance().saveCache(CACHE_JUHE_WEATHER + cityCode, weather)
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新聚合 AQI 失败", e)
        }
    }

    private suspend fun checkAlarmIfNeeded(context: Context, city: CityEntity) {
        if (!SpUtils.instance.getBoolean(REMINDER_WEATHER, true)) return
        val curDay = DateUtil.timeToDay(System.currentTimeMillis())
        val key = "alarmDay-${city.cityId}"
        if (SpUtils.instance.getString(key, "") == curDay) return

        val cityCode = resolveAlarmCityCode(city.cityName) ?: return
        try {
            val url = String.format(JUHE_ALARM, cityCode)
            val result = HttpUtils.get<JuheBean<List<JuheAlarmBean>>>(url)
            val list = result?.result ?: return
            if (list.isEmpty()) return
            var alarm = list.firstOrNull { city.cityName.contains(it.district) } ?: list[0]
            val timestamp = TimeUtils.string2Millis(alarm.time, "yyyy-MM-dd HH:mm")
            SpUtils.instance.putString(key, TimeUtils.millis2String(timestamp, "dd"))
            JPushHelper.buildLocalNotification(
                context.applicationContext,
                context.getString(R.string.app_name),
                alarm.title
            )
        } catch (e: Exception) {
            Log.e(TAG, "预警检查失败", e)
        }
    }

    private fun resolveAlarmCityCode(cityName: String): String? {
        val list = loadCityCodes(MyApp.instance())
        return list.firstOrNull { it.city_name.contains(cityName) }?.city_code
    }

    private fun loadCityCodes(context: Context): ArrayList<CityCode> {
        val cityModes = ArrayList<CityCode>()
        val cityJson = Utils.getJson("cityCode.json", context)
        val jsonElement = Gson().fromJson(cityJson, JsonObject::class.java) ?: return cityModes
        val jsonArray = Gson().fromJson(jsonElement["citys"], JsonArray::class.java)
        val list: ArrayList<CityCode> = Gson().fromJson(
            jsonArray,
            object : TypeToken<ArrayList<CityCode?>?>() {}.type
        )
        cityModes.addAll(list)
        return cityModes
    }
}
