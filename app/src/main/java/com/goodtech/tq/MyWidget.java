package com.goodtech.tq;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.goodtech.tq.alarm.JAlarmReceiver;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;

public class MyWidget extends AppWidgetProvider {
    String TAG = "MyWidget：";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i(TAG, "接受广播");
        Bundle extras = intent.getExtras();
        boolean update = extras.getBoolean("WidgetUpdate");
        if (update) {
            context.stopService(new Intent(context, WidgetService.class));
            context.startService(new Intent(context, WidgetService.class));
        }
    }

    /**
     * 第一个widget被添加调用
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i(TAG, "widget  onEnabled 状态");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, WidgetService.class));
        } else  {
            context.startService(new Intent(context, WidgetService.class));
        }
    }

    /**
     * widget被添加 || 更新时调用
     *
     * @param context
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i(TAG, "widget  onUpdate 状态");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (Settings.canDrawOverlays(context)) {
                context.startForegroundService(new Intent(context, WidgetService.class));
            } else {
                context.startService(new Intent(context, WidgetService.class));
            }
        } else  {
            context.startService(new Intent(context, WidgetService.class));
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
        Log.i(TAG, "widget  onDisabled 状态");
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
        Log.i(TAG, "widget  onDeleted 状态");

    }
}
