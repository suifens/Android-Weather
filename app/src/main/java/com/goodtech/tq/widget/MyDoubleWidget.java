package com.goodtech.tq.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;

public class MyDoubleWidget extends AppWidgetProvider {
    String TAG = "MyDoubleWidget：--------------";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e(TAG, "接受广播");
    }

    /**
     * 第一个widget被添加调用
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.e(TAG, "widget  onEnabled 状态");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Settings.canDrawOverlays(context)) {
            context.startForegroundService(new Intent(context, DoubleWidgetService.class));
        } else  {
            context.startService(new Intent(context, DoubleWidgetService.class));
        }
        SpUtils.getInstance().putBoolean(Constants.WIDGET_DOUBLE, true);
    }

    /**
     * widget被添加 || 更新时调用
     *
     * @param context
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.e(TAG, "widget  onUpdate 状态");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Settings.canDrawOverlays(context)) {
            context.startForegroundService(new Intent(context, DoubleWidgetService.class));
        } else  {
            context.startService(new Intent(context, DoubleWidgetService.class));
        }
    }

    /**
     * 最后一个widget被删除时调用
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.e(TAG, "widget  onDisabled 状态");
        context.stopService(new Intent(context, DoubleWidgetService.class));
    }

    /**
     * widget被删除时调用
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.e(TAG, "widget  onDeleted 状态");
        SpUtils.getInstance().remove(Constants.WIDGET_DOUBLE);
    }
}
