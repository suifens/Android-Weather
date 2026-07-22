package com.goodtech.tq.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import com.goodtech.tq.utils.Constants
import com.goodtech.tq.utils.SpUtils

/**
 * 双城大尺寸桌面小部件 Provider。
 *
 * 由 [WidgetWorkScheduler] 调度 [WidgetUpdateWorker] 周期刷新，
 * 不再依赖常驻前台 Service。
 */
class MyDoubleWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    /**
     * 第一个 widget 被添加：注册周期刷新任务并立即触发一次。
     * 同时记录双城 widget 已启用标记，供其他模块判断。
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled")
        SpUtils.getInstance().putBoolean(Constants.WIDGET_DOUBLE, true)
        WidgetWorkScheduler.schedulePeriodic(context)
        WidgetWorkScheduler.enqueueOneTime(context)
    }

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
                WidgetRenderer.renderCachedForWidget(context, MyDoubleWidget::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "缓存渲染失败", e)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    /**
     * 最后一个双城 widget 被删除。
     * 仅当单城 widget 也不存在时，才取消共享的周期刷新任务。
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled")
        WidgetWorkScheduler.cancelIfNoWidgets(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "onDeleted")
        SpUtils.getInstance().remove(Constants.WIDGET_DOUBLE)
    }

    companion object {
        private const val TAG = "MyDoubleWidget"
    }
}
