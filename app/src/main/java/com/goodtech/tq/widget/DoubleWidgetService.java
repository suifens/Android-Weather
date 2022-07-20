package com.goodtech.tq.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.goodtech.tq.R;
import com.goodtech.tq.SplashActivity;
import com.goodtech.tq.alarm.JAlarmReceiver;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.helpers.AqiHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Daily;
import com.goodtech.tq.models.Daypart;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.Metric;
import com.goodtech.tq.models.Observation;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.others.widget.WidgetType;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.WeatherUtils;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Timer;

public class DoubleWidgetService extends Service {

    String  TAG = "WidgetService ";
    private Timer mTimer;
    private SimpleDateFormat mFormat;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // updateWidget(DoubleWidgetService.this);
        WeatherHttpHelper.getInstance().getBaseUrl(() ->
                WeatherHttpHelper.getInstance().fetchWeather(Objects.requireNonNull(LocationSpHelper.getLocation()),
                        (success, weather, errCode) ->
                                updateWidget(DoubleWidgetService.this)));

        checkUpdate();

        String curDay = TimeUtils.timeToDay(System.currentTimeMillis());
        if (!SpUtils.getInstance().getString("alarmDay", "").equals(curDay)) {
            JAlarmReceiver.fetchAlarm(BaseApp.getInstance());
        }
    }

    private void checkUpdate() {
        LocationHelper.getInstance().start(BaseApp.getInstance(), new BDAbstractLocationListener() {
            /**
             * 定位请求回调函数
             * @param location 定位结果
             */
            @Override
            public void onReceiveLocation(BDLocation location) {
                //保存
                LocationSpHelper.saveWithLocation(location);
                LocationHelper.getInstance().stop();

                Log.e(TAG, "onReceiveLocation: --------------------------");

                WeatherHttpHelper.getInstance().getBaseUrl(() ->
                        WeatherHttpHelper.getInstance().fetchWeather(LocationSpHelper.getLocation(),
                                (success, weather, errCode) ->
                                        updateWidget(DoubleWidgetService.this)));
            }

            @Override
            public void onConnectHotSpotMessage(String s, int i) {
                super.onConnectHotSpotMessage(s, i);
            }

            /**
             * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
             *
             * @param locType           当前定位类型
             * @param diagnosticType    诊断类型（1~9）
             * @param diagnosticMessage 具体的诊断信息释义
             */
            @Override
            public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {
                super.onLocDiagnosticMessage(locType, diagnosticType, diagnosticMessage);
            }
        });
    }

    private void updateWidget(Context context) {

        CityMode location = LocationSpHelper.getLocation();
        if (location != null) {
            Log.e(TAG, "updateWidget: -------------" + location.getMergerName());
            WeatherModel weatherModel = WeatherSpHelper.getWeatherModel(location.getCid());
            if (weatherModel != null) {
                updateAppWidget(context, location, weatherModel);
            }
        }
    }

    @SuppressLint({"DefaultLocale", "RemoteViewLayout"})
    private void updateAppWidget(Context context, CityMode cityMode, WeatherModel model) {
        Log.e(TAG, "updateAppWidget: ----------------");
        //通过 RemoteViews 加载布局文件
        //通过 setTextView 等方法实现对控件的控制
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//        long millis = System.currentTimeMillis();
//        String format = mFormat.format(new Date(millis));
//        Log.i(TAG,"millis :" + millis + "\n"
//                + "format: " + format);
//        remoteViews.setTextViewText(R.id.tv_date, "日  期：" + format);
//        remoteViews.setTextViewText(R.id.tv_money, "毫秒值：" + millis);

//        String widgetTypeStr = SpUtils.getInstance().getString(Constants.WIDGET_TYPE, WidgetType.SingleLine1.toString());
//        switch (WidgetType.valueOf(widgetTypeStr)) {
//            case SingleLine1:
//                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);
//                remoteViews.setViewVisibility(R.id.layout_widget_small, View.VISIBLE);
//                remoteViews.setViewVisibility(R.id.backgroundView, View.VISIBLE);
//                updateSmall(context, remoteViews, cityMode, model);
//                break;
//            case DoubleLine1:
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        String widgetTypeStr = SpUtils.getInstance().getString(Constants.WIDGET_TYPE, WidgetType.SingleLine1.toString());
        switch (WidgetType.valueOf(widgetTypeStr)) {
            case SingleLine1:
            case DoubleLine1:
                remoteViews.setViewVisibility(R.id.backgroundView, View.VISIBLE);
                break;
            case SingleLine2:
            case DoubleLine2:
                remoteViews.setViewVisibility(R.id.backgroundView, View.INVISIBLE);
                break;
        }
                updateDouble(context, remoteViews, cityMode, model);
//                break;
//            case SingleLine2:
//                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);
//                remoteViews.setViewVisibility(R.id.layout_widget_small, View.VISIBLE);
//                remoteViews.setViewVisibility(R.id.backgroundView, View.INVISIBLE);
//                updateSmall(context, remoteViews, cityMode, model);
//                break;
//            case DoubleLine2:
//                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//                remoteViews.setViewVisibility(R.id.layout_widget, View.VISIBLE);
//                remoteViews.setViewVisibility(R.id.backgroundView, View.INVISIBLE);
//                updateDouble(context, remoteViews, cityMode, model);
//                break;
//        }
        
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(this, SplashActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widgetBtn, pendingIntent);   //点击跳转

        ComponentName componentName = new ComponentName(this, MyDoubleWidget.class);
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, remoteViews);
    }

    @SuppressLint("DefaultLocale")
    private void updateDouble(Context context, RemoteViews remoteViews, CityMode cityMode, WeatherModel model) {
        //  地址
        String current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH");
//        String current2 = TimeUtils.longToString(System.currentTimeMillis(), "MMddHHmm");
//        remoteViews.setTextViewText(R.id.addressTv, String.format("%s -- %s", cityMode.getMergerName(), current2));
        remoteViews.setTextViewText(R.id.addressTv, cityMode.getMergerName());
        remoteViews.setImageViewResource(R.id.locationImgView, R.drawable.ic_location_blue);

        if (model != null) {

            Daily today = model.today();
            if (today != null) {
                long currentTime = System.currentTimeMillis();
                long sunSetTime = TimeUtils.switchTime(today.sunSet);
                boolean day = currentTime < sunSetTime;

                Daypart todayPart = day ? today.dayPart : today.nightPart;
                remoteViews.setTextViewText(R.id.tv_temperature_today, String.format("%d/%d°", today.metric.maxTemp, today.metric.minTemp));
                if (todayPart != null) remoteViews.setTextViewText(R.id.tv_weather_today,todayPart.phraseChar);

                if (model.aqi > 0) {
                    remoteViews.setViewVisibility(R.id.img_quality_today, View.VISIBLE);
                    remoteViews.setImageViewResource(R.id.img_quality_today, AqiHelper.getQualityRes(model.aqi));
                } else {
                    remoteViews.setViewVisibility(R.id.img_quality_today, View.GONE);
                }

                Daily tomorrow = model.tomorrow();
                if (tomorrow != null) {
                    Daypart tomorrowPart = day ? tomorrow.dayPart : tomorrow.nightPart;
                    remoteViews.setTextViewText(R.id.tv_temperature_morn, String.format("%d/%d°", tomorrow.metric.maxTemp, tomorrow.metric.minTemp));
                    if (tomorrowPart != null) remoteViews.setTextViewText(R.id.tv_weather_morn, tomorrowPart.phraseChar);
                }
            }

            for (Hourly hourly : model.hourlies) {
                if (hourly != null) {
                    String dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH");
                    if (dayHour.equals(current)) {
                        //
                        remoteViews.setImageViewResource(R.id.iconImgView, ImageUtils.weatherImageRes(hourly.icon_cd));
                        remoteViews.setTextViewText(R.id.tv_wx_phrase, hourly.phraseChar);

                        if (hourly.metric != null) {

                            remoteViews.setTextViewText(R.id.tv_rh_wrap, String.format("%s风 %d级｜ 湿度%d%%", hourly.wdir_cardinal,
                                    WeatherUtils.windGrade(hourly.metric.wspd), hourly.rh));
                            remoteViews.setTextViewText(R.id.tv_temperature, String.format("%d°", hourly.metric.temp));
                            return;
                        }
                    }
                }
            }

            if (model.observation != null) {
                Observation observation = model.observation;
                Metric metric = observation.metric;

                remoteViews.setTextViewText(R.id.tv_rh_wrap, String.format("%s风 %d级｜ 湿度%d%%", observation.wdirCardinal,
                        WeatherUtils.windGrade(metric.wspd), observation.rh));
                remoteViews.setImageViewResource(R.id.iconImgView, ImageUtils.weatherImageRes(observation.wxIcon));
                remoteViews.setTextViewText(R.id.tv_temperature, String.format("%d°", metric.temp));
                remoteViews.setTextViewText(R.id.tv_wx_phrase, observation.wxPhrase);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        mTimer.cancel();
//        mTimer = null;
        Log.i(TAG,"onDestory");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

}
