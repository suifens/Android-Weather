package com.chunjing.tq.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.bean.juhe.JuheWeather
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.db.WeatherRepository
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ui.activity.SplashActivity
import com.chunjing.tq.ui.activity.WIDGET_TYPE
import com.chunjing.tq.ui.activity.WidgetSettingActivity
import com.chunjing.tq.ui.fragment.vm.CACHE_JUHE_WEATHER
import com.chunjing.tq.utils.AqiHelper
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.WeatherUtils.getIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 桌面小部件 RemoteViews 渲染（A 方案）。
 *
 * - Provider.onUpdate 只读缓存重绘，不拉网
 * - 网络刷新由 [WidgetUpdateWorker] / [WeatherRepository.fetchAndCache] 成功后再调用
 */
object WidgetRenderer {

    private const val TAG = "WidgetRenderer"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 解析小组件展示城市：优先定位城，否则列表第一个。
     */
    suspend fun resolveWidgetCity(): CityEntity? {
        val cities = AppRepo.getInstance().getCities()
        if (cities.isEmpty()) return null
        return cities.firstOrNull { it.isLocal() } ?: cities[0]
    }

    suspend fun loadAqi(cityName: String): Int {
        val cityCode = ContentUtil.getLifeCityName(cityName) ?: return 0
        val juhe = AppRepo.getInstance().getCache<JuheWeather?>(CACHE_JUHE_WEATHER + cityCode)
        return juhe?.aqi ?: 0
    }

    /**
     * 用本地缓存重绘所有已安装小部件（不拉网）。
     * 无数据时保持桌面现状。
     */
    suspend fun updateWidgetsIfNecessary(context: Context) {
        if (!WidgetWorkScheduler.hasAnyWidget(context)) return
        val city = resolveWidgetCity() ?: return
        val weather = WeatherRepository.getInstance().getCachedWeather(city.cityId) ?: return
        val aqi = loadAqi(city.cityName)
        val displayName = city.mergerName.ifBlank { city.cityName }

        val manager = AppWidgetManager.getInstance(context)
        val smallIds = manager.getAppWidgetIds(ComponentName(context, WeatherWidgetSmall::class.java))
        val doubleIds = manager.getAppWidgetIds(ComponentName(context, WeatherWidgetDouble::class.java))

        if (smallIds.isNotEmpty()) {
            renderSmall(context, displayName, weather, aqi)
        }
        if (doubleIds.isNotEmpty()) {
            renderDouble(context, displayName, weather, aqi)
        }
    }

    /** 任意线程可调用。 */
    fun updateWidgetsIfNecessaryAsync(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                updateWidgetsIfNecessary(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "异步重绘小部件失败", e)
            }
        }
    }

    fun renderCachedForWidgetAsync(context: Context, widgetClass: Class<*>) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                renderCachedForWidget(appContext, widgetClass)
            } catch (e: Exception) {
                Log.e(TAG, "缓存渲染失败", e)
            }
        }
    }

    suspend fun renderCachedForWidget(context: Context, widgetClass: Class<*>) {
        val city = resolveWidgetCity() ?: return
        val weather = WeatherRepository.getInstance().getCachedWeather(city.cityId) ?: return
        val aqi = loadAqi(city.cityName)
        val displayName = city.mergerName.ifBlank { city.cityName }
        when (widgetClass) {
            WeatherWidgetSmall::class.java -> renderSmall(context, displayName, weather, aqi)
            WeatherWidgetDouble::class.java -> renderDouble(context, displayName, weather, aqi)
        }
    }

    fun renderSmall(context: Context, cityName: String, now: WeatherBean?, aqi: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_small)
        applyWidgetClock(views)
        val location = if (cityName.contains("-")) cityName.split("-")[1] else cityName
        views.setTextViewText(R.id.tvLocation, location)
        views.setImageViewResource(R.id.locationImgView, R.drawable.ic_loc_w)

        now?.let {
            val observation = it.observation
            views.setTextViewText(R.id.tvWeather, observation.getWxcPhrase())
            views.setTextViewText(R.id.tvTemp, "${observation.metric.temp}°")
            views.setTextViewText(R.id.tvParse, it.getPhrase())
            views.setImageViewResource(R.id.ivWeather, getIcon(observation.wxIcon))
        }

        if (aqi > 0) {
            views.setViewVisibility(R.id.qualityImgV, View.VISIBLE)
            views.setImageViewResource(R.id.qualityImgV, AqiHelper.getQualityRes(aqi))
        } else {
            views.setViewVisibility(R.id.qualityImgV, View.GONE)
        }

        applyBackgroundVisibility(views, R.id.ivBackground)
        views.setOnClickPendingIntent(R.id.widgetBtn, buildClickIntent(context))
        pushWidget(context, WeatherWidgetSmall::class.java, views)
    }

    fun renderDouble(context: Context, cityName: String, now: WeatherBean?, aqi: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_double)
        applyWidgetClock(views)
        val location = if (cityName.contains("-")) cityName.split("-")[1] else cityName
        views.setTextViewText(R.id.dtvLocation, location)
        views.setImageViewResource(R.id.dLocationImgView, R.drawable.ic_loc_w)

        now?.let {
            val observation = it.observation
            views.setTextViewText(R.id.dtvWeather, observation.getWxcPhrase())
            views.setTextViewText(R.id.dtvTemp, "${observation.metric.temp}°")
            views.setTextViewText(R.id.dtvParse, it.getPhrase())
            views.setImageViewResource(R.id.divWeather, getIcon(observation.wxIcon))
            views.setImageViewResource(R.id.umbrellaImgV, R.drawable.ic_umbrella)
            views.setImageViewResource(R.id.choseImgV, R.drawable.ic_chose_r)

            if (it.hourlies.size > 2) {
                val firstHour = it.hourlies[0]
                val secondHour = it.hourlies[1]
                val showRain = listOf(firstHour, secondHour).any { h ->
                    val p = h.getPhrasesChar()
                    p.contains("雨") || p.contains("雪") || p.contains("阴")
                }
                views.setTextViewText(
                    R.id.rainTip,
                    if (showRain) "未来两小时可能有雨" else "未来两小时不会下雨"
                )
            }
        }

        if (aqi > 0) {
            views.setViewVisibility(R.id.dQualityImgV, View.VISIBLE)
            views.setImageViewResource(R.id.dQualityImgV, AqiHelper.getQualityRes(aqi))
        } else {
            views.setViewVisibility(R.id.dQualityImgV, View.GONE)
        }

        applyBackgroundVisibility(views, R.id.divBackground)
        views.setOnClickPendingIntent(R.id.dWidgetBtn, buildClickIntent(context))
        pushWidget(context, WeatherWidgetDouble::class.java, views)
    }


    /**
     * 配置 TextClock：`9:41 周一 (07月12)`。
     * 由桌面进程按分钟自刷新，不依赖天气 Worker。
     */
    private fun applyWidgetClock(views: RemoteViews) {
        val format12 = "h:mm E (MM'月'dd)"
        val format24 = "H:mm E (MM'月'dd)"
        views.setCharSequence(R.id.widget_time_clock, "setFormat12Hour", format12)
        views.setCharSequence(R.id.widget_time_clock, "setFormat24Hour", format24)
    }

    private fun applyBackgroundVisibility(views: RemoteViews, backgroundId: Int) {
        val widgetTypeStr = SpUtils.instance.getString(
            WIDGET_TYPE,
            WidgetSettingActivity.WidgetType.SingleLine1.toString()
        )
        val type = try {
            WidgetSettingActivity.WidgetType.valueOf(widgetTypeStr)
        } catch (_: Exception) {
            WidgetSettingActivity.WidgetType.SingleLine1
        }
        when (type) {
            WidgetSettingActivity.WidgetType.SingleLine1,
            WidgetSettingActivity.WidgetType.DoubleLine1 ->
                views.setViewVisibility(backgroundId, View.VISIBLE)
            else -> views.setViewVisibility(backgroundId, View.GONE)
        }
    }

    private fun buildClickIntent(context: Context): PendingIntent {
        val intent = Intent(context, SplashActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    private fun pushWidget(context: Context, widgetClass: Class<*>, views: RemoteViews) {
        val componentName = ComponentName(context, widgetClass)
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, views)
    }
}
