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
 * A 方案：此处只负责调度周期任务 + 读缓存重绘，不拉网。
 * 网络刷新由 [WidgetUpdateWorker] / App 内天气请求完成后统一触发。
 */
class MyDoubleWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    /**
     * 第一个 widget 被添加：注册周期任务，并立即触发一次拉网（可能尚无缓存）。
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled")
        SpUtils.getInstance().putBoolean(Constants.WIDGET_DOUBLE, true)
        WidgetWorkScheduler.schedulePeriodic(context)
        WidgetWorkScheduler.enqueueOneTime(context)
    }

    /**
     * 系统要求重绘时：只读缓存画 UI，避免与周期 Worker 重复拉网。
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate")
        WidgetWorkScheduler.schedulePeriodic(context)
        renderCachedAsync(context)
    }

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
