package com.chunjing.tq.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chunjing.tq.widget.WidgetWorkScheduler

/**
 * 历史双城开机广播兼容：同样走 WorkManager，不再启动前台 Service。
 */
class BootCompleteReceiverDouble : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!WidgetWorkScheduler.hasAnyWidget(context)) return
        WidgetWorkScheduler.schedulePeriodic(context)
        WidgetWorkScheduler.enqueueOneTime(context)
    }
}
