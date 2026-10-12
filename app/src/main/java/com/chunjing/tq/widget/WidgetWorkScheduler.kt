package com.chunjing.tq.widget

import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chunjing.tq.widget.service.Notify_Id
import com.chunjing.tq.widget.service.WidgetServiceDouble
import com.chunjing.tq.widget.service.WidgetServiceSmall
import java.util.concurrent.TimeUnit

/**
 * 桌面小部件网络刷新任务调度（取代常驻前台 Service）。
 */
object WidgetWorkScheduler {

    private const val PERIODIC_WORK_NAME = "simple_widget_weather_periodic"
    private const val ONE_TIME_WORK_NAME = "simple_widget_weather_one_time"
    private const val INTERVAL_MINUTES = 30L
    private const val BACKOFF_DELAY_SECONDS = 30L

    private fun constraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    /** 停掉旧版前台 Service，并清掉历史常驻通知。 */
    fun stopLegacyServices(context: Context) {
        runCatching {
            context.stopService(Intent(context, WidgetServiceSmall::class.java))
            context.stopService(Intent(context, WidgetServiceDouble::class.java))
        }
        clearLegacyWeatherNotifications(context)
    }

    fun schedulePeriodic(context: Context) {
        stopLegacyServices(context)
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

    fun enqueueOneTime(context: Context) {
        stopLegacyServices(context)
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

    fun cancelIfNoWidgets(context: Context) {
        if (hasAnyWidget(context)) return
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
        stopLegacyServices(context)
    }

    fun hasAnyWidget(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        val smallIds = manager.getAppWidgetIds(ComponentName(context, WeatherWidgetSmall::class.java))
        val doubleIds = manager.getAppWidgetIds(ComponentName(context, WeatherWidgetDouble::class.java))
        return smallIds.isNotEmpty() || doubleIds.isNotEmpty()
    }

    fun clearLegacyWeatherNotifications(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        nm.cancel(Notify_Id)
    }
}
