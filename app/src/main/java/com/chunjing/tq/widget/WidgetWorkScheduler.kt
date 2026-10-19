package com.chunjing.tq.widget

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.chunjing.tq.widget.service.Notify_Id
import com.chunjing.tq.widget.service.WidgetServiceDouble
import com.chunjing.tq.widget.service.WidgetServiceSmall

/**
 * 桌面小部件刷新调度。
 * WorkManager 周期任务在息屏下常被推迟，主路径改为 AlarmManager 每 30 分钟唤醒。
 */
object WidgetWorkScheduler {

    private const val TAG = "WidgetWorkScheduler"
    const val ACTION_REFRESH = "com.chunjing.tq.widget.ACTION_REFRESH"
    private const val PERIODIC_WORK_NAME = "simple_widget_weather_periodic"
    private const val ONE_TIME_WORK_NAME = "simple_widget_weather_one_time"
    private const val ALARM_REQUEST_CODE = 0x5748
    const val INTERVAL_MS = 30 * 60 * 1000L

    fun stopLegacyServices(context: Context) {
        runCatching {
            context.stopService(Intent(context, WidgetServiceSmall::class.java))
            context.stopService(Intent(context, WidgetServiceDouble::class.java))
        }
        clearLegacyWeatherNotifications(context)
    }

    fun schedulePeriodic(context: Context) {
        val app = context.applicationContext
        stopLegacyServices(app)
        if (!hasAnyWidget(app)) {
            cancelAlarm(app)
            WorkManager.getInstance(app).cancelUniqueWork(PERIODIC_WORK_NAME)
            return
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            30, TimeUnit.MINUTES,
            10, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(app).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        armNextAlarm(app)
    }

    /** 立刻入队。加急执行，配额用完时退回普通任务。 */
    fun enqueueOneTime(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun armNextAlarm(context: Context) {
        val app = context.applicationContext
        if (!hasAnyWidget(app)) {
            cancelAlarm(app)
            return
        }
        val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + INTERVAL_MS
        val pi = refreshPendingIntent(app)
        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
            Log.d(TAG, "已设定 ${INTERVAL_MS / 60000} 分钟后刷新")
        } catch (e: SecurityException) {
            Log.e(TAG, "精确闹钟被拒绝，改用不精确闹钟", e)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        }
    }

    fun cancelAlarm(context: Context) {
        val app = context.applicationContext
        val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(refreshPendingIntent(app))
    }

    private fun refreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetRefreshReceiver::class.java).setAction(ACTION_REFRESH)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)
    }

    fun cancelIfNoWidgets(context: Context) {
        if (hasAnyWidget(context)) return
        WorkManager.getInstance(context).cancelUniqueWork(ONE_TIME_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
        cancelAlarm(context)
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
