package com.goodtech.tq.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 重启后闹钟会丢失，有小组件时重新挂上并补一次刷新。 */
class WidgetBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!WidgetWorkScheduler.hasAnyWidget(context)) return
        WidgetWorkScheduler.schedulePeriodic(context)
        WidgetWorkScheduler.enqueueOneTime(context)
    }
}
