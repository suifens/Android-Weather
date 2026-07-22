package com.goodtech.tq.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 单城小尺寸桌面小部件 Provider。
 *
 * 由 [WidgetWorkScheduler] 调度 [WidgetUpdateWorker] 周期刷新，
 * 不再依赖常驻前台 Service。
 */
class MyWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    /**
     * 第一个 widget 被添加：注册周期刷新任务并立即触发一次。
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled")
        WidgetWorkScheduler.schedulePeriodic(context)
        WidgetWorkScheduler.enqueueOneTime(context)
    }

    /**
     * widget 被添加或系统周期更新时调用。
     * KEEP 策略保证周期任务不会被重复注册；立即刷新保证添加后即时显示缓存数据。
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate")
        WidgetWorkScheduler.schedulePeriodic(context)
        // 系统周期 onUpdate 用节流版，避免与周期任务短时间内重复执行
        WidgetWorkScheduler.enqueueOneTimeThrottled(context)
        // 用缓存立即渲染一次，避免 Worker 执行延迟期间首屏空白
        renderCachedAsync(context)
    }

    /**
     * 通过 goAsync 在后台线程用缓存立即渲染 widget，
     * 避免 Worker 执行延迟（通常 1-3 秒）期间显示空白 initialLayout。
     */
    private fun renderCachedAsync(context: Context) {
        val pendingResult = goAsync()
        Thread {
            try {
                WidgetRenderer.renderCachedForWidget(context, MyWidget::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "缓存渲染失败", e)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    /**
     * 最后一个单城 widget 被删除。
     * 仅当双城 widget 也不存在时，才取消共享的周期刷新任务。
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled")
        WidgetWorkScheduler.cancelIfNoWidgets(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "onDeleted")
    }

    companion object {
        private const val TAG = "MyWidget"
    }
}
