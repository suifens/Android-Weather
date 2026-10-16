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
import java.util.concurrent.TimeUnit

/**
 * 桌面小部件网络刷新任务的统一调度入口。
 *
 * - [schedulePeriodic]：注册 30 分钟周期任务，仅在联网时执行
 * - Worker 内有效拉网间隔也为 30 分钟，并在拉网前同步刷新定位
 * - [enqueueOneTime]：立即拉网刷新（首次添加 widget、城市列表变更等）
 * - Provider.onUpdate 不再走这里拉网，只读缓存；样式变更走 [WidgetRenderer.updateWidgetsIfNecessaryAsync]
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
     * 适用于首次添加 widget、切换城市等需要立刻拉网的场景。
     * 仅改样式请用 [WidgetRenderer.updateWidgetsIfNecessaryAsync]。
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
