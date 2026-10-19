package com.chunjing.tq.widget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.MyApp
import com.chunjing.tq.R
import com.chunjing.tq.bean.CityCode
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.bean.juhe.JuheAlarmBean
import com.chunjing.tq.bean.juhe.JuheBean
import com.chunjing.tq.bean.juhe.JuheWeather
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.LocationRepository
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
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * 小组件天气刷新 Worker（A 方案）。
 * - 有效拉网间隔 30 分钟
 * - 拉网前同步刷新定位，再拉天气重绘
 */
class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "WidgetUpdateWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val STALE_MS = 30 * 60 * 1000L
        private const val LOCATION_TIMEOUT_MS = 20_000L
        private const val CHANNEL_ID = "widget_weather_sync"
        private const val FOREGROUND_NOTIFY_ID = 10042
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = buildForegroundInfo()

    private fun buildForegroundInfo(): ForegroundInfo {
        val context = applicationContext
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "天气小组件更新",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("正在更新天气")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var flags = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            val fine = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (fine || coarse) {
                flags = flags or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            }
            flags
        } else {
            0
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(FOREGROUND_NOTIFY_ID, notification, type)
        } else {
            ForegroundInfo(FOREGROUND_NOTIFY_ID, notification)
        }
    }

    override suspend fun doWork(): Result {
        val context = applicationContext
        WidgetWorkScheduler.armNextAlarm(context)
        try {
            setForeground(getForegroundInfo())
        } catch (e: Exception) {
            Log.e(TAG, "升前台失败，继续尝试刷新", e)
        }
        WidgetWorkScheduler.clearLegacyWeatherNotifications(context)
        return try {
            refresh(context)
        } finally {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(FOREGROUND_NOTIFY_ID)
        }
    }

    private suspend fun refresh(context: Context): Result {
        if (!WidgetWorkScheduler.hasAnyWidget(context)) {
            Log.d(TAG, "无 widget，跳过")
            return Result.success()
        }

        var city = WidgetRenderer.resolveWidgetCity()
        val cached = city?.let { WeatherRepository.getInstance().getCachedWeather(it.cityId) }
        val needNetwork = cached == null || cached.needReload()

        if (needNetwork) {
            refreshLocationIfPossible()?.let { city = it }
        }

        city = city ?: WidgetRenderer.resolveWidgetCity()
        if (city == null || city!!.latitude.isBlank() || city!!.longitude.isBlank()) {
            Log.d(TAG, "无可用城市/经纬度，跳过")
            return Result.success()
        }
        val cityNow = city!!

        val repo = WeatherRepository.getInstance()
        val latestCached = repo.getCachedWeather(cityNow.cityId)
        if (latestCached != null) {
            WidgetRenderer.updateWidgetsIfNecessary(context)
        }

        if (latestCached != null && !latestCached.needReload()) {
            Log.d(TAG, "缓存未满 30 分钟，跳过拉网")
            return Result.success()
        }

        val fetched = try {
            repo.fetchAndCache(cityNow)
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

        refreshJuheAqi(cityNow)
        WidgetRenderer.updateWidgetsIfNecessary(context)
        checkAlarmIfNeeded(context, cityNow)
        return Result.success()
    }

    private fun WeatherBean.needReload(): Boolean {
        if (updateTime <= 0L) return true
        return System.currentTimeMillis() - updateTime >= STALE_MS
    }

    /**
     * 有定位权限时刷新定位城并写入 DB；失败返回 null，沿用旧城市。
     */
    private suspend fun refreshLocationIfPossible(): CityEntity? {
        val context = applicationContext
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            Log.d(TAG, "无定位权限，跳过定位刷新")
            return null
        }

        val locationRepo = LocationRepository.getInstance()
        val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                locationRepo.requestLocation(object : LocationRepository.Callbacks {
                    override fun onStart() {}

                    override fun onLocation(location: Location) {
                        if (cont.isActive) cont.resume(location)
                    }

                    override fun onError(message: String) {
                        Log.d(TAG, "定位失败: $message")
                        if (cont.isActive) cont.resume(null)
                    }

                    override fun onFinish() {}
                })
            }
        }

        if (location == null) {
            locationRepo.endLocationRequest()
            return null
        }

        return try {
            withContext(Dispatchers.IO) {
                val city = locationRepo.location2CityEntity(location)
                val resolvedCode = locationRepo.resolveLocationCityCode(city)
                if (resolvedCode.isNotBlank()) {
                    city.cityCode = resolvedCode
                }
                AppRepo.getInstance().addCity(city)
                Log.d(TAG, "定位已更新: ${city.mergerName}")
                city
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存定位城失败", e)
            null
        } finally {
            locationRepo.endLocationRequest()
        }
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
            val alarm = list.firstOrNull { city.cityName.contains(it.district) } ?: list[0]
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
