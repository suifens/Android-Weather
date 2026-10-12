package com.chunjing.tq.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking

/**
 * 单行桌面小部件 Provider（A 方案：只调度 + 读缓存，不拉网）。
 */
class WeatherWidgetSmall : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled")
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
        renderCachedAsync(context)
    }

    private fun renderCachedAsync(context: Context) {
        val pendingResult = goAsync()
        Thread {
            try {
                runBlocking {
                    WidgetRenderer.renderCachedForWidget(context, WeatherWidgetSmall::class.java)
                }
            } catch (e: Exception) {
                Log.e(TAG, "缓存渲染失败", e)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled")
        WidgetWorkScheduler.cancelIfNoWidgets(context)
    }

    companion object {
        private const val TAG = "WeatherWidgetSmall"
    }
}
