package com.goodtech.tq.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 30 分钟闹钟到期：立刻入队刷新，并预约下一次。
 */
class WidgetRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != WidgetWorkScheduler.ACTION_REFRESH) return
        if (!WidgetWorkScheduler.hasAnyWidget(context)) {
            WidgetWorkScheduler.cancelAlarm(context)
            return
        }
        Log.d(TAG, "闹钟触发，开始刷新天气与定位")
        WidgetWorkScheduler.enqueueOneTime(context)
        WidgetWorkScheduler.armNextAlarm(context)
    }

    companion object {
        private const val TAG = "WidgetRefreshReceiver"
    }
}
