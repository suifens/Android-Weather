package com.chunjing.tq.widget.service

import android.content.*
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.MyApp
import com.chunjing.tq.R
import com.chunjing.tq.bean.CityCode
import com.chunjing.tq.bean.Daily
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.bean.juhe.JuheAlarmBean
import com.chunjing.tq.bean.juhe.JuheBean
import com.chunjing.tq.bean.juhe.JuheWeather
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ext.JUHE_ALARM
import com.chunjing.tq.ext.JUHE_QUERY
import com.chunjing.tq.ext.REMINDER_WEATHER
import com.chunjing.tq.ext.WEATHER_URL
import com.chunjing.tq.jpush.JPushHelper
import com.chunjing.tq.ui.fragment.vm.CACHE_JUHE_WEATHER
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_DAY
import com.chunjing.tq.utils.ContentUtil
import com.chunjing.tq.utils.NotificationUtil
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.util.*

const val Notify_Id = 999

open class BaseWidgetService : LifecycleService() {

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
        //LogUtils.e("onCreate: ---------------------")
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

    val callback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            //LogUtils.d("network available。。。。")
            updateRemote()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            //LogUtils.d("network unavailable。。。。")
            intervalJob?.cancel()
            intervalJob = null
        }
    }

    private val netWorkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val activeNetworkInfo = connManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isAvailable) {
                //LogUtils.d("network available。。。。")
                updateRemote()
            } else {
                //LogUtils.d("network unavailable。。。。")
                intervalJob?.cancel()
                intervalJob = null
            }
        }
    }

    protected var intervalJob: Job? = null

    protected fun updateRemote() {
        if (intervalJob != null) {
            return
        }
        intervalJob = lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
            //LogUtils.e("WidgetService: 异常...")
        }) {
            while (isActive) {
                //LogUtils.d("intervalJob run")
                updateRemoteOnce()
                delay(1800_000)
            }
        }
    }

    private suspend fun updateRemoteOnce() {
        val cities = AppRepo.getInstance().getCities()
        println("+++++++++updateRemoteOnce size = ${cities.size}")
        var cityEntity: CityEntity?
        if (cities.isNotEmpty()) {
            cityEntity = cities[0]
            var localName: String? = null
            var cityId = cities[0].cityId
            var cityName = cities[0].cityName
            var latitude = cities[0].latitude
            var longitude = cities[0].longitude
            cities.forEach {
                if (it.isLocal()) {
                    cityEntity = it
                    localName = it.mergerName
                    cityId = it.cityId
                    cityName = it.cityName
                    latitude = it.latitude
                    longitude = it.longitude
                    return@forEach
                }
            }
            val url = String.format(WEATHER_URL, latitude, longitude)
            val result = HttpUtils.get<WeatherBean>(url)
            result?.let {
                val now = it
                /// 保存今日
                val todayStr = TimeUtils.millis2String(System.currentTimeMillis(), "MM月dd日")
                val todayKey = "$CACHE_WEATHER_DAY${cityEntity!!.cityId}_$todayStr"
                for (daily in result.dailies) {
                    if (daily.time == todayStr) {
                        val todayDaily = AppRepo.getInstance().getCache<Daily?>(todayKey)
                        if (todayDaily == null) {
                            AppRepo.getInstance().saveCache(
                                "$CACHE_WEATHER_DAY${cityEntity!!.cityId}_$todayStr",
                                daily
                            )
                        }
                        break
                    }
                }

                NotificationUtil.updateNotification(
                    this@BaseWidgetService,
                    Notify_Id,
                    localName ?: cityName,
                    now
                )

                val curDay = DateUtil.timeToDay(System.currentTimeMillis())
                val key = "alarmDay-$cityId"
                if (SpUtils.instance.getString(key, "") != curDay) {
                    fetchAlarmData(cityEntity!!)
                }

                val cityCode = ContentUtil.getLifeCityName(cityName)
                if (!TextUtils.isEmpty(cityCode)) {
                    val queryUrl = String.format(JUHE_QUERY, cityCode)
                    val queryResult = HttpUtils.get<JuheBean<JuheWeather>>(queryUrl)
                    queryResult?.result.let { weather ->
                        if (weather != null) {
                            weather.updateTime = System.currentTimeMillis()
                            AppRepo.getInstance().saveCache(CACHE_JUHE_WEATHER + cityCode, weather)
                            updateWidget(cityId, localName ?: cityName, now, weather.aqi)
                        } else {
                            updateWidget(cityId, localName ?: cityName, now)
                        }
                    }
                } else {
                    updateWidget(cityId, localName ?: cityName, now)
                }
            }
        }
    }

    /**
     * 预警获取
     */
    private suspend fun fetchAlarmData(city: CityEntity) {
        val cityCode = getCityCode(city.cityName)
        if (cityCode != null && cityCode.isNotEmpty()) {
            val url = String.format(JUHE_ALARM, cityCode)
            val result = HttpUtils.get<JuheBean<List<JuheAlarmBean>>>(url)
            result?.result?.let {
                checkAlarmModels(MyApp.instance(), city, it)
            }
        }
    }

    private fun getCityCodes(context: Context?): ArrayList<CityCode> {
        val cityModes: ArrayList<CityCode> = ArrayList()
        val cityJson: String = Utils.getJson("cityCode.json", context)
        val jsonElement = Gson().fromJson(cityJson, JsonObject::class.java)
        if (jsonElement != null) {
            val jsonArray = Gson().fromJson(jsonElement["citys"], JsonArray::class.java)
            val list: ArrayList<CityCode> = Gson().fromJson(
                jsonArray,
                object : TypeToken<ArrayList<CityCode?>?>() {}.type
            )
            cityModes.addAll(list)
        }
        return cityModes
    }

    /**
     * 获取城市code
     */
    private fun getCityCode(cityName: String): String? {

        if (!SpUtils.instance.getBoolean(REMINDER_WEATHER, true)) {
            //  不添加提醒
            return null
        }

        var cityCode: String? = null
        val list: ArrayList<CityCode> = getCityCodes(MyApp.instance())
        for (cityCodeMode in list) {
            if (cityCodeMode.city_name.contains(cityName)) {
                cityCode = cityCodeMode.city_code
                break
            }
        }

        return cityCode
    }

    private fun checkAlarmModels(
        context: Context,
        cityMode: CityEntity,
        list: List<JuheAlarmBean>
    ) {
        if (list.isNotEmpty()) {
            var alarmModel: JuheAlarmBean? = null
            for (temp in list) {
                if (cityMode.cityName.contains(temp.district)) {
                    alarmModel = temp
                    break
                }
            }
            if (alarmModel == null) {
                alarmModel = list[0]
            }
            //  建立推送
            val curDay: String = TimeUtils.millis2String(System.currentTimeMillis())
            val key = "alarmDay-" + cityMode.cityId
            if (SpUtils.instance.getString(key, "") !== curDay) {
                val timestamp = TimeUtils.string2Millis(alarmModel.time, "yyyy-MM-dd HH:mm")
                SpUtils.instance.putString(
                    key,
                    TimeUtils.millis2String(timestamp, "dd")
                )
                JPushHelper.buildLocalNotification(
                    context.applicationContext,
                    context.getString(R.string.app_name), alarmModel.title
                )
            }
        }
    }

    /**
     * Update widget
     */
    open suspend fun updateWidget(
        cityId: String,
        cityName: String,
        now: WeatherBean?,
        aqi: Int = 0
    ) {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connManager.unregisterNetworkCallback(callback)
        } else {
            unregisterReceiver(netWorkStateReceiver)
        }
        //LogUtils.e("onDestroy: ---------------------")
    }
}