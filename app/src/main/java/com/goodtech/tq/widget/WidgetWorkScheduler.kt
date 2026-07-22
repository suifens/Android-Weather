package com.goodtech.tq.widget

import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.goodtech.tq.utils.SpUtils
import java.util.concurrent.TimeUnit

/**
 * 桌面小部件刷新任务的统一调度入口。
 *
 * - [schedulePeriodic]：注册 30 分钟周期任务（最小允许间隔），仅在联网时执行
 * - [enqueueOneTime]：立即触发一次刷新（widget 添加/更新、网络恢复、回前台等场景）
 * - [cancelIfNoWidgets]：两种 widget 都已删除时才取消周期任务
 *
 * 使用唯一 workName 保证全局只有一个周期任务在运行，
 * 配合 [ExistingPeriodicWorkPolicy.KEEP] 避免重复调度覆盖已有任务。
 */
object WidgetWorkScheduler {

    /** 周期任务唯一名称。 */
    private const val PERIODIC_WORK_NAME = "widget_weather_periodic"

    /** 一次性立即刷新任务唯一名称。 */
    private const val ONE_TIME_WORK_NAME = "widget_weather_one_time"

    /** 周期刷新间隔，与原 [WidgetService] 内的 delay(1800_000) 保持一致。 */
    private const val INTERVAL_MINUTES = 30L

    /** 失败重试的初始退避延迟（线性增长：30s、60s、90s...）。 */
    private const val BACKOFF_DELAY_SECONDS = 30L

    /** 一次性刷新的节流间隔，避免系统 onUpdate 与周期任务短时间内重复触发。 */
    private const val ONE_TIME_THROTTLE_MS = 60_000L

    /** 记录上次一次性刷新时间的 SP key。 */
    private const val SP_KEY_LAST_ONE_TIME = "widget_last_one_time_ms"

    /** 历史天气通知 ID（迁移前前台 Service 使用），升级后需主动清除。 */
    private const val LEGACY_NOTIFY_ID_SMALL = 999
    private const val LEGACY_NOTIFY_ID_DOUBLE = 998

    private fun constraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    /**
     * 注册或保留周期刷新任务。
     * 已存在周期任务时保持原任务（不重置计时），避免每次 widget onUpdate 都重置周期。
     */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints())
            .setBackoffCriteria(BackoffPolicy.LINEAR, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * 立即触发一次刷新。使用 [ExistingWorkPolicy.REPLACE] 保证最新请求生效。
     * 已有未执行的 one-time 任务会被替换，避免排队堆积。
     * 适用于用户主动操作（添加 widget、切换城市、改样式）等需要立即反馈的场景。
     */
    fun enqueueOneTime(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setConstraints(constraints())
            .setBackoffCriteria(BackoffPolicy.LINEAR, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        SpUtils.getInstance().putLong(SP_KEY_LAST_ONE_TIME, System.currentTimeMillis())
    }

    /**
     * 带节流的立即刷新：距上次刷新不足 [ONE_TIME_THROTTLE_MS] 时跳过。
     * 适用于系统 [android.appwidget.AppWidgetProvider.onUpdate] 等高频回调场景，
     * 避免与周期任务短时间内重复执行（fetchWeather 内部已有 5 分钟节流，
     * 此处节流进一步避免不必要的 Worker 启动开销）。
     */
    fun enqueueOneTimeThrottled(context: Context) {
        val last = SpUtils.getInstance().getLong(SP_KEY_LAST_ONE_TIME, 0L)
        if (System.currentTimeMillis() - last < ONE_TIME_THROTTLE_MS) {
            return
        }
        enqueueOneTime(context)
    }

    /**
     * 仅当桌面上已无任何天气小部件时，才取消周期任务。
     * 避免删除一种 widget 时误停另一种仍在使用的刷新任务。
     */
    fun cancelIfNoWidgets(context: Context) {
        if (hasAnyWidget(context)) {
            return
        }
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
        clearLegacyWeatherNotifications(context)
    }

    /** 是否仍有任意一种天气小部件安装在桌面。 */
    fun hasAnyWidget(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        val smallIds = manager.getAppWidgetIds(ComponentName(context, MyWidget::class.java))
        val doubleIds = manager.getAppWidgetIds(ComponentName(context, MyDoubleWidget::class.java))
        return smallIds.isNotEmpty() || doubleIds.isNotEmpty()
    }

    /**
     * 清除历史天气通知栏（迁移自前台 Service，现已不再展示）。
     * Worker 首次刷新时也会调用，确保升级用户桌面上的旧通知被移除。
     */
    fun clearLegacyWeatherNotifications(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        nm.cancel(LEGACY_NOTIFY_ID_SMALL)
        nm.cancel(LEGACY_NOTIFY_ID_DOUBLE)
    }
}
