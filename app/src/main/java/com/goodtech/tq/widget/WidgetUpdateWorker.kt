package com.goodtech.tq.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * 桌面小部件天气刷新 Worker。
 *
 * 取代原 [WidgetService] / [DoubleWidgetService] 的常驻前台 Service + while 循环方案：
 * - 由 [WidgetWorkScheduler] 以周期任务（约 30 分钟）+ 网络约束调度
 * - 单次执行：拉天气 → 渲染所有已安装的 widget → 检查预警
 * - 无 widget 安装时直接返回，避免空跑
 * - 不再更新通知栏天气，仅刷新桌面 RemoteViews
 */
class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "WidgetUpdateWorker"
        /** 单次天气请求超时，避免回调不触发时协程永久挂起。 */
        private const val REQUEST_TIMEOUT_MS = 30_000L
        /** 网络失败时最大重试次数，超过后不再重试。 */
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    /** 一次 doWork 内缓存的 widget 安装情况，避免重复查询 AppWidgetManager。 */
    private data class WidgetIds(val smallIds: IntArray, val doubleIds: IntArray) {
        fun hasAny(): Boolean = smallIds.isNotEmpty() || doubleIds.isNotEmpty()
    }

    override suspend fun doWork(): Result {
        val context = applicationContext

        // 清除迁移前残留的天气通知栏
        WidgetWorkScheduler.clearLegacyWeatherNotifications(context)

        // 一次性查询所有已安装 widget，后续复用，避免重复 IPC
        val widgetIds = queryWidgetIds(context)
        if (!widgetIds.hasAny()) {
            Log.d(TAG, "无 widget 安装，跳过本次刷新")
            return Result.success()
        }

        val location = LocationSpHelper.getLocation()
        if (location == null) {
            Log.d(TAG, "无定位城市，跳过刷新")
            return Result.success()
        }

        // lat/lon 缺失属于配置问题，重试无意义，直接成功结束
        if (location.lat.isNullOrEmpty() || location.lon.isNullOrEmpty()) {
            Log.d(TAG, "定位城市缺少经纬度，跳过刷新")
            return Result.success()
        }

        // 先读缓存，渲染一次保证 widget 不会停留在 initialLayout
        val cachedModel = WeatherSpHelper.getWeatherModel(location.poiId)
        if (cachedModel != null) {
            renderWidgets(context, location, widgetIds, cachedModel)
        }

        // 数据未过期（< 1 小时且同小时）时跳过网络请求，节省流量与电量
        if (cachedModel != null && !cachedModel.needReload()) {
            Log.d(TAG, "缓存数据未过期，跳过网络请求")
            return Result.success()
        }

        // 拉取最新天气
        val fetchSuccess = try {
            fetchWeatherSync(location)
        } catch (e: Exception) {
            Log.e(TAG, "拉取天气异常", e)
            false
        }

        // 拉取成功后用新数据再渲染一次
        if (fetchSuccess) {
            WeatherSpHelper.getWeatherModel(location.poiId)?.let {
                renderWidgets(context, location, widgetIds, it)
            }
            checkAlarmIfNeeded(context)
            return Result.success()
        }

        // 拉取失败（网络问题等）：限制重试次数，避免无限重试
        return if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
            Log.d(TAG, "拉取失败，准备第 ${runAttemptCount + 1} 次重试")
            Result.retry()
        } else {
            Log.d(TAG, "已达最大重试次数，停止重试")
            Result.success()
        }
    }

    /** 一次性查询两种 widget 的安装情况。 */
    private fun queryWidgetIds(context: Context): WidgetIds {
        val manager = AppWidgetManager.getInstance(context)
        val smallIds = manager.getAppWidgetIds(ComponentName(context, MyWidget::class.java))
        val doubleIds = manager.getAppWidgetIds(ComponentName(context, MyDoubleWidget::class.java))
        return WidgetIds(smallIds, doubleIds)
    }

    /**
     * 用传入的 [model] 渲染所有已安装的桌面小部件（不再更新通知栏）。
     */
    private fun renderWidgets(
        context: Context,
        cityMode: CityMode,
        widgetIds: WidgetIds,
        model: WeatherModel
    ) {
        if (widgetIds.smallIds.isNotEmpty()) {
            WidgetRenderer.renderSmall(context, cityMode, model)
        }
        if (widgetIds.doubleIds.isNotEmpty()) {
            WidgetRenderer.renderDouble(context, cityMode, model)
        }
    }

    /**
     * 把 [WeatherHttpHelper.fetchWeather] 的回调式 API 包装成 suspend。
     * 内部先获取 baseUrl，再发请求。
     *
     * 注意：[WeatherHttpHelper.fetchWeather] 在 lat/lon 为空时不会触发回调，
     * 因此用 [withTimeoutOrNull] 兜底，避免协程永久挂起。
     */
    private suspend fun fetchWeatherSync(cityMode: CityMode): Boolean {
        // 前置检查：lat/lon 缺失时 fetchWeather 不会回调，直接返回
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

                cont.invokeOnCancellation { /* OkHttp 请求由其自身管理生命周期 */ }
            }
        }
        return result ?: false
    }

    /** 每天最多拉一次预警，避免重复请求聚合 API。 */
    private fun checkAlarmIfNeeded(context: Context) {
        val curDay = TimeUtils.timeToDay(System.currentTimeMillis())
        if (SpUtils.getInstance().getString("alarmDay", "") != curDay) {
            JAlarmReceiver.fetchAlarm(context)
        }
    }
}
