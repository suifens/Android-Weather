package com.goodtech.tq.widget

import android.Manifest
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.goodtech.tq.alarm.JAlarmReceiver
import com.goodtech.tq.helpers.LocationSpHelper
import com.goodtech.tq.helpers.WeatherSpHelper
import com.goodtech.tq.httpClient.ApiCallback
import com.goodtech.tq.httpClient.ErrorCode
import com.goodtech.tq.httpClient.WeatherHttpHelper
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * 桌面小部件天气刷新 Worker（A 方案）。
 *
 * - 周期约 30 分钟；有效拉网间隔也是 30 分钟
 * - 拉天气前先尝试刷新定位（有权限时），再拉网重绘
 */
class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "WidgetUpdateWorker"
        private const val REQUEST_TIMEOUT_MS = 30_000L
        private const val LOCATION_TIMEOUT_MS = 15_000L
        private const val MAX_RETRY_ATTEMPTS = 3
        /** 有效拉网间隔：30 分钟 */
        private const val STALE_MS = 30 * 60 * 1000L
        /** 最近定位可复用时长 */
        private const val LAST_KNOWN_MAX_AGE_MS = 5 * 60 * 1000L
        private const val CHANNEL_ID = "widget_weather_sync"
        private const val FOREGROUND_NOTIFY_ID = 10042
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = buildForegroundInfo()

    private fun buildForegroundInfo(): ForegroundInfo {
        val context = applicationContext
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "天气小组件更新",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(context.getString(com.goodtech.tq.R.string.app_name))
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

    private data class WidgetIds(val smallIds: IntArray, val doubleIds: IntArray) {
        fun hasAny(): Boolean = smallIds.isNotEmpty() || doubleIds.isNotEmpty()
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
        val widgetIds = queryWidgetIds(context)
        if (!widgetIds.hasAny()) {
            Log.d(TAG, "无 widget 安装，跳过本次刷新")
            return Result.success()
        }

        // 与天气同节奏：即将拉网时先刷新定位（失败则沿用缓存定位）
        val lastUpdateHint = LocationSpHelper.getLocation()?.poiId?.let {
            WeatherSpHelper.getLastUpdate(it)
        } ?: 0L
        val needNetwork = lastUpdateHint <= 0L ||
            System.currentTimeMillis() - lastUpdateHint >= STALE_MS

        if (needNetwork) {
            refreshLocationIfPossible(context)
        }

        val location = LocationSpHelper.getLocation()
        if (location == null) {
            Log.d(TAG, "无定位城市，跳过刷新")
            return Result.success()
        }
        if (location.lat.isNullOrEmpty() || location.lon.isNullOrEmpty()) {
            Log.d(TAG, "定位城市缺少经纬度，跳过刷新")
            return Result.success()
        }

        val cachedModel = WeatherSpHelper.getWeatherModel(location.poiId)
        if (cachedModel != null) {
            WidgetRenderer.updateWidgetsIfNecessary(context)
        }

        val lastUpdate = WeatherSpHelper.getLastUpdate(location.poiId)
        if (lastUpdate > 0L && System.currentTimeMillis() - lastUpdate < STALE_MS) {
            Log.d(TAG, "缓存未满 30 分钟，跳过网络请求")
            return Result.success()
        }

        val fetchSuccess = try {
            fetchWeatherSync(location)
        } catch (e: Exception) {
            Log.e(TAG, "拉取天气异常", e)
            false
        }

        if (fetchSuccess) {
            WidgetRenderer.updateWidgetsIfNecessary(context)
            checkAlarmIfNeeded(context)
            return Result.success()
        }

        return if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
            Log.d(TAG, "拉取失败，准备第 ${runAttemptCount + 1} 次重试")
            Result.retry()
        } else {
            Log.d(TAG, "已达最大重试次数，停止重试")
            Result.success()
        }
    }

    private fun queryWidgetIds(context: Context): WidgetIds {
        val manager = AppWidgetManager.getInstance(context)
        val smallIds = manager.getAppWidgetIds(ComponentName(context, MyWidget::class.java))
        val doubleIds = manager.getAppWidgetIds(ComponentName(context, MyDoubleWidget::class.java))
        return WidgetIds(smallIds, doubleIds)
    }

    /**
     * 后台静默定位：有权限则更新 [LocationSpHelper]，无弹窗。
     * 失败不影响后续用缓存城市拉天气。
     */
    private suspend fun refreshLocationIfPossible(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            Log.d(TAG, "无定位权限，跳过定位刷新")
            return false
        }

        val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            requestLocationOnce(context)
        } ?: return false

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    ?.firstOrNull()
                LocationSpHelper.saveWithLocation(location, address)
                Log.d(TAG, "定位已更新: ${location.latitude},${location.longitude}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "保存定位失败", e)
                false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestLocationOnce(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
            !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        ) {
            return null
        }

        val last = sequenceOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).mapNotNull { provider ->
            runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }

        if (last != null && System.currentTimeMillis() - last.time <= LAST_KNOWN_MAX_AGE_MS) {
            return last
        }

        return suspendCancellableCoroutine { cont ->
            val mainHandler = Handler(Looper.getMainLooper())
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    runCatching {
                        lm.removeUpdates(this)
                    }
                    if (cont.isActive) cont.resume(location)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            cont.invokeOnCancellation {
                runCatching { lm.removeUpdates(listener) }
            }

            mainHandler.post {
                try {
                    var requested = false
                    if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        lm.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 0L, 0f, listener, Looper.getMainLooper()
                        )
                        requested = true
                    }
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        lm.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 0L, 0f, listener, Looper.getMainLooper()
                        )
                        requested = true
                    }
                    if (!requested) {
                        if (cont.isActive) cont.resume(last)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "请求定位失败", e)
                    if (cont.isActive) cont.resume(last)
                }
            }
        }
    }

    private suspend fun fetchWeatherSync(cityMode: CityMode): Boolean {
        if (cityMode.lat.isNullOrEmpty() || cityMode.lon.isNullOrEmpty()) {
            return false
        }

        val result = withTimeoutOrNull(REQUEST_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val helper = WeatherHttpHelper.getInstance()
                helper.getBaseUrl {
                    helper.fetchWeather(cityMode, object : ApiCallback {
                        override fun onResponse(
                            success: Boolean,
                            weather: WeatherModel?,
                            errCode: ErrorCode?
                        ) {
                            if (cont.isActive) cont.resume(success)
                        }
                    })
                }
                cont.invokeOnCancellation { }
            }
        }
        return result ?: false
    }

    private fun checkAlarmIfNeeded(context: Context) {
        val curDay = TimeUtils.timeToDay(System.currentTimeMillis())
        if (SpUtils.getInstance().getString("alarmDay", "") != curDay) {
            JAlarmReceiver.fetchAlarm(context)
        }
    }
}
