package com.chunjing.tq.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.chunjing.tq.widget.service.WidgetServiceDouble

class BootCompleteReceiverDouble : BroadcastReceiver() {

    private val BOOT_ACTION_DOUBLE = "android.intent.action.BOOT_COMPLETED_DOUBLE"

    override fun onReceive(context: Context, intent: Intent) {
        if (BOOT_ACTION_DOUBLE == intent.action) {
            //开启Service
            openService(context)
        }
    }

    /***
     * 启动Service的方法
     *
     * @param context
     */
    private fun openService(context: Context) {
        val newIntent = Intent(context, WidgetServiceDouble::class.java)
        //判断当前编译的版本是否高于等于 Android8.0 或 26 以上的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Settings.canDrawOverlays(context)) {
            context.startForegroundService(newIntent)
        } else {
            context.startService(newIntent)
        }
    }
}