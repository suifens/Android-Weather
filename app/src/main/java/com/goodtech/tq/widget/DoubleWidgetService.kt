package com.goodtech.tq.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.*
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.goodtech.tq.R
import com.goodtech.tq.activity.SplashActivity
import com.goodtech.tq.alarm.JAlarmReceiver
import com.goodtech.tq.helpers.AqiHelper
import com.goodtech.tq.helpers.LocationSpHelper
import com.goodtech.tq.helpers.WeatherSpHelper
import com.goodtech.tq.httpClient.ErrorCode
import com.goodtech.tq.httpClient.WeatherHttpHelper
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.*
import kotlinx.coroutines.*
import java.util.*

const val Notify_Id_Double = 999

class DoubleWidgetService : LifecycleService() {

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
        LogUtils.e("onCreate: ---------------------")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForeground(Notify_Id_Double, NotificationUtil.createNotification(this, Notify_Id_Double))
//        }

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

    val callback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            LogUtils.d("network available。。。。")
            updateRemote()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            LogUtils.d("network unavailable。。。。")
            intervalJob?.cancel()
            intervalJob = null
        }
    }

    val netWorkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val activeNetworkInfo = connManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isAvailable) {
                LogUtils.d("network available。。。。")
                updateRemote()
            } else {
                LogUtils.d("network unavailable。。。。")
                intervalJob?.cancel()
                intervalJob = null
            }
        }
    }

    private var intervalJob: Job? = null

    private fun updateRemote() {
        if (intervalJob != null) {
            return
        }
        intervalJob = lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
            LogUtils.e("WidgetService: 异常...")
        }) {
            while (isActive) {
                LogUtils.d("intervalJob run")
                updateRemoteOnce()

                val curDay = TimeUtils.timeToDay(System.currentTimeMillis())
                if (SpUtils.getInstance().getString("alarmDay", "") != curDay) {
                    JAlarmReceiver.fetchAlarm(this@DoubleWidgetService)
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
                    if (success) {
                        updateWidget(this@DoubleWidgetService)
                    }
                }
            }
        }
    }

    private fun updateWidget(context: Context) {
        val location = LocationSpHelper.getLocation()
        if (location != null) {
            val weatherModel = WeatherSpHelper.getWeatherModel(location.cid)
            weatherModel?.let {

                NotificationUtil.updateNotification(
                    this@DoubleWidgetService,
                    Notify_Id_Double,
                    location.mergerName,
                    it
                )

                //通过 RemoteViews 加载布局文件
                //通过 setTextView 等方法实现对控件的控制
                updateDouble(location, it)
            }
        }
    }

    @SuppressLint("DefaultLocale", "UnspecifiedImmutableFlag")
    private fun updateDouble(
        cityMode: CityMode,
        model: WeatherModel?
    ) {

        val remoteViews = RemoteViews(packageName, R.layout.widget_layout)

        //  地址

        //  地址
        val current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH")
//        String current2 = TimeUtils.longToString(System.currentTimeMillis(), "MMddHHmm");
//        remoteViews.setTextViewText(R.id.addressTv, String.format("%s -- %s", cityMode.getMergerName(), current2));
        //        String current2 = TimeUtils.longToString(System.currentTimeMillis(), "MMddHHmm");
//        remoteViews.setTextViewText(R.id.addressTv, String.format("%s -- %s", cityMode.getMergerName(), current2));
        remoteViews.setTextViewText(R.id.addressTv, cityMode.mergerName)
        remoteViews.setImageViewResource(R.id.locationImgView, R.drawable.ic_location_blue)

        if (model != null) {
            val today = model.today()
            if (today != null) {
                val currentTime = System.currentTimeMillis()
                val sunSetTime = TimeUtils.switchTime(today.sunSet)
                val day = currentTime < sunSetTime
                val todayPart = if (day) today.dayPart else today.nightPart
                remoteViews.setTextViewText(
                    R.id.tv_temperature_today,
                    String.format("%d/%d°", today.metric.maxTemp, today.metric.minTemp)
                )
                if (todayPart != null) remoteViews.setTextViewText(
                    R.id.tv_weather_today,
                    todayPart.phraseChar
                )
                if (model.aqi > 0) {
                    remoteViews.setViewVisibility(R.id.img_quality_today, View.VISIBLE)
                    remoteViews.setImageViewResource(
                        R.id.img_quality_today,
                        AqiHelper.getQualityRes(model.aqi)
                    )
                } else {
                    remoteViews.setViewVisibility(R.id.img_quality_today, View.GONE)
                }
                val tomorrow = model.tomorrow()
                if (tomorrow != null) {
                    val tomorrowPart = if (day) tomorrow.dayPart else tomorrow.nightPart
                    remoteViews.setTextViewText(
                        R.id.tv_temperature_morn,
                        String.format("%d/%d°", tomorrow.metric.maxTemp, tomorrow.metric.minTemp)
                    )
                    if (tomorrowPart != null) remoteViews.setTextViewText(
                        R.id.tv_weather_morn,
                        tomorrowPart.phraseChar
                    )
                }
            }
            for (hourly in model.hourlies) {
                if (hourly != null) {
                    val dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH")
                    if (dayHour == current) {
                        //
                        remoteViews.setImageViewResource(
                            R.id.iconImgView,
                            ImageUtils.weatherImageRes(hourly.icon_cd)
                        )
                        remoteViews.setTextViewText(R.id.tv_wx_phrase, hourly.phraseChar)
                        if (hourly.metric != null) {
                            remoteViews.setTextViewText(
                                R.id.tv_rh_wrap, String.format(
                                    "%s风 %d级｜ 湿度%d%%", hourly.wdir_cardinal,
                                    WeatherUtils.windGrade(hourly.metric.wspd.toFloat()), hourly.rh
                                )
                            )
                            remoteViews.setTextViewText(
                                R.id.tv_temperature,
                                String.format("%d°", hourly.metric.temp)
                            )
                            return
                        }
                    }
                }
            }
            if (model.observation != null) {
                val observation = model.observation
                val metric = observation.metric
                remoteViews.setTextViewText(
                    R.id.tv_rh_wrap, String.format(
                        "%s风 %d级｜ 湿度%d%%", observation.wdirCardinal,
                        WeatherUtils.windGrade(metric.wspd.toFloat()), observation.rh
                    )
                )
                remoteViews.setImageViewResource(
                    R.id.iconImgView,
                    ImageUtils.weatherImageRes(observation.wxIcon)
                )
                remoteViews.setTextViewText(R.id.tv_temperature, String.format("%d°", metric.temp))
                remoteViews.setTextViewText(R.id.tv_wx_phrase, observation.wxPhrase)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(
                this,
                SplashActivity::class.java
            ), PendingIntent.FLAG_UPDATE_CURRENT
        )
        remoteViews.setOnClickPendingIntent(R.id.widgetBtn, pendingIntent) //点击跳转

        val componentName = ComponentName(this, MyDoubleWidget::class.java)
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, remoteViews)

    }


    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connManager.unregisterNetworkCallback(callback)
        } else {
            unregisterReceiver(netWorkStateReceiver)
        }
        LogUtils.e("onDestroy: ---------------------")
    }
}