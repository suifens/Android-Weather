package com.goodtech.tq.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.goodtech.tq.app.App;

public class MyWidget extends AppWidgetProvider {
    String TAG = "MyWidget：--------------";

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
        App.instance.startService(context);
    }

    /**
     * widget被添加 || 更新时调用
     *
     * @param context
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.e(TAG, "widget  onUpdate 状态");
        App.instance.startService(context);
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
        context.stopService(new Intent(context, WidgetService.class));
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

    }
}
