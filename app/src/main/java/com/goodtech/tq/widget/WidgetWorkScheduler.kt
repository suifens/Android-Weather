package com.goodtech.tq.widget

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

/**
 * 桌面小部件网络刷新任务的统一调度入口。
 *
 * WorkManager 周期任务在息屏/省电下常被推迟数小时，因此主调度改为 AlarmManager：
 * 每 30 分钟唤醒进程并立刻入队一次刷新（不附加网络约束，避免一直 BLOCKED）。
 */
object WidgetWorkScheduler {

    private const val TAG = "WidgetWorkScheduler"
    const val ACTION_REFRESH = "com.goodtech.tq.widget.ACTION_REFRESH"
    private const val PERIODIC_WORK_NAME = "widget_weather_periodic"
    private const val ONE_TIME_WORK_NAME = "widget_weather_one_time"
    private const val ALARM_REQUEST_CODE = 0x5747
    const val INTERVAL_MS = 30 * 60 * 1000L

    /** 历史天气通知 ID（迁移前前台 Service 使用），升级后需主动清除。 */
    private const val LEGACY_NOTIFY_ID_SMALL = 999
    private const val LEGACY_NOTIFY_ID_DOUBLE = 998

    /**
     * 有小组件时设定下一次 30 分钟闹钟，并取消旧的周期 Work（避免和闹钟重复、且周期任务易被推迟）。
     */
    fun schedulePeriodic(context: Context) {
        val app = context.applicationContext
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

    /**
     * 立刻入队。加急执行，配额用完时退回普通任务。
     * 任务内部会升成前台同步，避免被系统杀掉。
     */
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

    /**
     * 仅当桌面上已无任何天气小部件时，才取消周期任务。
     * 避免删除一种 widget 时误停另一种仍在使用的刷新任务。
     */
    fun cancelIfNoWidgets(context: Context) {
        if (hasAnyWidget(context)) {
            return
        }
        WorkManager.getInstance(context).cancelUniqueWork(ONE_TIME_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
        cancelAlarm(context)
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
