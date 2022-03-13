package com.goodtech.tq.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.goodtech.tq.app.BaseApp;

import java.util.Calendar;

/**
 * Created by loonggg on 2016/3/21.
 */
public class AlarmManagerUtil {
    public static final String ALARM_ACTION = "com.goodtech.tq.alarm";

    public static void setAlarmTime(Context context, long timeInMillis, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, intent.getIntExtra("id", 0),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int interval = (int) intent.getLongExtra("intervalMillis", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setWindow(AlarmManager.RTC_WAKEUP, timeInMillis, interval, sender);
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static void cancelAlarm(Context context, String action, int id) {
        Intent intent = new Intent(action);
        PendingIntent pi = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try {
            am.cancel(pi);
            Log.e("TAG", "Alarm is Canceled.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "Alarm is not Canceled: " + e.toString());
        }
        am.cancel(pi);
    }

    /**
     */
    @SuppressLint({"MissingPermission", "UnspecifiedImmutableFlag"})
    public static void setAlarm(Context context, int hourOfDay) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get
                (Calendar.DAY_OF_MONTH), hourOfDay, 0, 10);
        long intervalMillis = 12 * 3600 * 1000;

        int requestCode = calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DAY_OF_MONTH);
        AlarmManagerUtil.cancelAlarm(context, ALARM_ACTION, requestCode);

        Intent intent = new Intent(ALARM_ACTION);
        intent.setPackage(BaseApp.getInstance().getPackageName());
        intent.putExtra("intervalMillis", intervalMillis);

        PendingIntent sender = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            sender = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            sender = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        long startTime = calMethod(0, calendar.getTimeInMillis());
        am.setExact(AlarmManager.RTC_WAKEUP, startTime, sender);
    }


    /**
     * @param weekflag 传入的是周几
     * @param dateTime 传入的是时间戳（设置当天的年月日+从选择框拿来的时分秒）
     * @return 返回起始闹钟时间的时间戳
     */
    private static long calMethod(int weekflag, long dateTime) {
        long time = 0;
        //weekflag == 0表示是按天为周期性的时间间隔或者是一次行的，weekfalg非0时表示每周几的闹钟并以周为时间间隔
        if (weekflag != 0) {
            Calendar c = Calendar.getInstance();
            int week = c.get(Calendar.DAY_OF_WEEK);
            if (1 == week) {
                week = 7;
            } else if (2 == week) {
                week = 1;
            } else if (3 == week) {
                week = 2;
            } else if (4 == week) {
                week = 3;
            } else if (5 == week) {
                week = 4;
            } else if (6 == week) {
                week = 5;
            } else if (7 == week) {
                week = 6;
            }

            if (weekflag == week) {
                if (dateTime > System.currentTimeMillis()) {
                    time = dateTime;
                } else {
                    time = dateTime + 7 * 24 * 3600 * 1000;
                }
            } else if (weekflag > week) {
                time = dateTime + (weekflag - week) * 24 * 3600 * 1000;
            } else if (weekflag < week) {
                time = dateTime + (weekflag - week + 7) * 24 * 3600 * 1000;
            }
        } else {
            if (dateTime > System.currentTimeMillis()) {
                time = dateTime;
            } else {
                time = dateTime + 10 * 1000; //12 * 3600 * 1000;
            }
        }
        return time;
    }


}
