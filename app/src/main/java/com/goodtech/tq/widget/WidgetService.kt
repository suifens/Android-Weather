package com.goodtech.tq.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.goodtech.tq.R
import com.goodtech.tq.activity.SplashActivity
import com.goodtech.tq.alarm.JAlarmReceiver
import com.goodtech.tq.app.App
import com.goodtech.tq.helpers.AqiHelper
import com.goodtech.tq.helpers.LocationSpHelper
import com.goodtech.tq.helpers.WeatherSpHelper
import com.goodtech.tq.httpClient.ErrorCode
import com.goodtech.tq.httpClient.WeatherHttpHelper
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.*
import kotlinx.coroutines.*

const val Notify_Id = 999

class WidgetService : LifecycleService() {

    lateinit var connManager: ConnectivityManager

    /**
     * 防止Service首次启动时执行onStartCommand()中的updateRemoteOnce()
     */
    private var isFirst = true

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isFirst = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Notify_Id,
                NotificationUtil.createNotification(this, Notify_Id),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(
                Notify_Id,
                NotificationUtil.createNotification(this, Notify_Id)
            )
        }

        connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connManager.registerDefaultNetworkCallback(callback)
        } else {
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            registerReceiver(netWorkStateReceiver, intentFilter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isFirst) {
            isFirst = false
        } else {
            lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }) {
                updateRemoteOnce()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val callback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            updateRemote()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            intervalJob?.cancel()
            intervalJob = null
        }
    }

    private val netWorkStateReceiver = object : BroadcastReceiver() {

        private val BOOT_ACTION_DOUBLE = "android.intent.action.BOOT_COMPLETED_DOUBLE"

        override fun onReceive(context: Context, intent: Intent) {
            if (BOOT_ACTION_DOUBLE == intent.action) {
                //开启Service
                openService(context)
            }

            val activeNetworkInfo = connManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isAvailable) {
                updateRemote()
            } else {
                intervalJob?.cancel()
                intervalJob = null
            }
        }
    }

    /***
     * 启动Service的方法
     *
     * @param context
     */
    private fun openService(context: Context) {
        App.instance.startService(context);
    }

    private var intervalJob: Job? = null

    private fun updateRemote() {
        if (intervalJob != null) {
            return
        }
        intervalJob = lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
        }) {
            while (isActive) {
                updateRemoteOnce()


//        AlarmManagerUtil.setAlarm(getApplicationContext(), 21);
                val curDay = TimeUtils.timeToDay(System.currentTimeMillis())
                if (SpUtils.getInstance().getString("alarmDay", "") != curDay) {
                    JAlarmReceiver.fetchAlarm(this@WidgetService)
                }

                delay(1800_000)
            }
        }
    }

    private suspend fun updateRemoteOnce() {
        val location = LocationSpHelper.getLocation()
        if (location != null) {
            WeatherHttpHelper.getInstance().getBaseUrl {
                WeatherHttpHelper.getInstance().fetchWeather(LocationSpHelper.getLocation())
                { success: Boolean, _: WeatherModel?, _: ErrorCode? ->
                    updateWidget(this@WidgetService)
                }
            }
        }
    }

    private fun updateWidget(context: Context) {
        val location = LocationSpHelper.getLocation()
        if (location != null) {
            val weatherModel = WeatherSpHelper.getWeatherModel(location.poiId)
            weatherModel?.let {

                NotificationUtil.updateNotification(
                    this@WidgetService,
                    Notify_Id,
                    location.mergerName,
                    it
                )

                //通过 RemoteViews 加载布局文件
                //通过 setTextView 等方法实现对控件的控制
                updateSmall(location, it)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateSmall(
        cityMode: CityMode,
        model: WeatherModel?
    ) {

        val remoteViews = RemoteViews(packageName, R.layout.widget_layout_small)

        //  地址
        val current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH")
        //        String current2 = TimeUtils.longToString(System.currentTimeMillis(), "MMddHHmm");
//        remoteViews.setTextViewText(R.id.sAddressTv, String.format("%s -- %s", cityMode.getMergerName(), current2));
        remoteViews.setTextViewText(R.id.sAddressTv, cityMode.mergerName)
        remoteViews.setImageViewResource(R.id.locationImgView, R.drawable.ic_location_blue)
        if (model != null) {
            if (model.aqi > 0) {
                remoteViews.setViewVisibility(R.id.img_quality, View.VISIBLE)
                remoteViews.setImageViewResource(
                    R.id.img_quality,
                    AqiHelper.getQualityRes(model.aqi)
                )
            } else {
                remoteViews.setViewVisibility(R.id.img_quality, View.GONE)
            }

            var hadSetTemp = false
            for (hourly in model.hourlies) {
                if (hourly != null) {
                    val dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH")
                    if (dayHour == current) {
                        //
                        remoteViews.setImageViewResource(
                            R.id.sIconImgView,
                            ImageUtils.weatherImageRes(hourly.icon_cd)
                        )
                        remoteViews.setTextViewText(R.id.sTv_wx_phrase, hourly.getPhraseChar())
                        if (hourly.metric != null) {
                            remoteViews.setTextViewText(
                                R.id.sTv_rh_wrap, String.format(
                                    "%s风 %d级｜ 湿度%d%%", hourly.wdir_cardinal,
                                    WeatherUtils.windGrade(hourly.metric.wspd.toFloat()), hourly.rh
                                )
                            )
                            remoteViews.setTextViewText(
                                R.id.sTv_temperature,
                                String.format("%d°", hourly.metric.temp)
                            )
                            hadSetTemp = true
                            break
                        }
                    }
                }
            }
            if (!hadSetTemp && model.observation != null) {
                val observation = model.observation
                val metric = observation.metric
                remoteViews.setTextViewText(
                    R.id.sTv_rh_wrap, String.format(
                        "%s风 %d级｜ 湿度%d%%", observation.wdirCardinal,
                        WeatherUtils.windGrade(metric.wspd.toFloat()), observation.rh
                    )
                )
                remoteViews.setImageViewResource(
                    R.id.sIconImgView,
                    ImageUtils.weatherImageRes(observation.wxIcon)
                )
                remoteViews.setTextViewText(
                    R.id.sTv_temperature,
                    String.format("%d°", metric.temp)
                )
                remoteViews.setTextViewText(R.id.sTv_wx_phrase, observation.getWxPhrase())
            }
        }

        val intent = Intent(this, SplashActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        remoteViews.setOnClickPendingIntent(R.id.widgetBtn, pendingIntent) //点击跳转

        val componentName = ComponentName(this, MyWidget::class.java)
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, remoteViews)

    }


    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connManager.unregisterNetworkCallback(callback)
        } else {
            unregisterReceiver(netWorkStateReceiver)
        }
    }
}