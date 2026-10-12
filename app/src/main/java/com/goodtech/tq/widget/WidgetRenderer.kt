package com.goodtech.tq.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.goodtech.tq.R
import com.goodtech.tq.activity.SplashActivity
import com.goodtech.tq.helpers.AqiHelper
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.Daily
import com.goodtech.tq.models.Observation
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.others.widget.WidgetType
import com.goodtech.tq.utils.Constants
import com.goodtech.tq.helpers.LocationSpHelper
import com.goodtech.tq.helpers.WeatherSpHelper
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.utils.TimeUtils
import com.goodtech.tq.utils.WeatherUtils
import java.util.concurrent.Executors

/**
 * 桌面小部件 RemoteViews 构建与更新的纯函数集合。
 *
 * 把原 [WidgetService.updateSmall] / [DoubleWidgetService.updateDouble] 中
 * 与 Service 生命周期耦合的 RemoteViews 渲染逻辑抽离出来，
 * 使其可在 Worker / Receiver 等任意无生命周期上下文中复用。
 *
 * 刷新约定（对齐 Breezy 思路的 A 方案）：
 * - Provider.onUpdate 只读缓存重绘，不拉网
 * - 网络刷新由 [WidgetUpdateWorker] / App 内 fetch 完成后再调 [updateWidgetsIfNecessary]
 */
object WidgetRenderer {

    private const val TAG = "WidgetRenderer"

    private val backgroundExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "widget-render").apply { isDaemon = true }
    }

    /**
     * 读取用户在设置页选择的 [WidgetType]，判断是否为透明样式。
     * SingleLine2 / DoubleLine2 为透明样式。
     */
    private fun isTransparentStyle(): Boolean {
        val typeStr = SpUtils.getInstance()
            .getString(Constants.WIDGET_TYPE, WidgetType.SingleLine1.toString())
        return try {
            val type = WidgetType.valueOf(typeStr)
            type == WidgetType.SingleLine2 || type == WidgetType.DoubleLine2
        } catch (e: IllegalArgumentException) {
            // 配置异常时回退为非透明
            false
        }
    }

    /**
     * 根据透明样式设置背景 ImageView。
     * 透明样式下隐藏背景，让桌面壁纸透出；非透明使用默认半透明黑底以保证文字可读性。
     */
    private fun applyBackground(remoteViews: RemoteViews) {
        if (isTransparentStyle()) {
            remoteViews.setViewVisibility(R.id.backgroundView, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.backgroundView, View.VISIBLE)
            remoteViews.setImageViewResource(
                R.id.backgroundView, R.drawable.bg_radius12_black_10
            )
        }
    }

    /**
     * 构建点击跳转 [SplashActivity] 的 PendingIntent，统一处理 immutable 兼容。
     */
    private fun buildClickIntent(context: Context): PendingIntent {
        val intent = Intent(context, SplashActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    /**
     * 用本地缓存重绘所有已安装的天气小部件（不拉网）。
     * 失败或无数据时保持桌面现状，不覆盖为空白。
     *
     * 调用方须在后台线程（Worker / goAsync / [updateWidgetsIfNecessaryAsync]）。
     */
    @JvmStatic
    fun updateWidgetsIfNecessary(context: Context) {
        if (!WidgetWorkScheduler.hasAnyWidget(context)) {
            return
        }
        val location = LocationSpHelper.getLocation() ?: return
        val model = WeatherSpHelper.getWeatherModel(location.poiId) ?: return

        val manager = AppWidgetManager.getInstance(context)
        val smallIds = manager.getAppWidgetIds(ComponentName(context, MyWidget::class.java))
        val doubleIds = manager.getAppWidgetIds(ComponentName(context, MyDoubleWidget::class.java))

        if (smallIds.isNotEmpty()) {
            renderSmall(context, location, model)
        }
        if (doubleIds.isNotEmpty()) {
            renderDouble(context, location, model)
        }
    }

    /**
     * 任意线程可调用：内部切到后台线程执行 [updateWidgetsIfNecessary]。
     * 供主线程回调（如天气拉取成功、设置页改样式）使用。
     */
    @JvmStatic
    fun updateWidgetsIfNecessaryAsync(context: Context) {
        val appContext = context.applicationContext
        backgroundExecutor.execute {
            try {
                updateWidgetsIfNecessary(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "异步重绘小部件失败", e)
            }
        }
    }

    /**
     * 用 MMKV 缓存立即绘制指定类型的 widget（不拉网）。
     * 供 [android.appwidget.AppWidgetProvider.onUpdate] 在后台线程调用。
     */
    fun renderCachedForWidget(context: Context, widgetClass: Class<*>) {
        val location = LocationSpHelper.getLocation() ?: return
        val model = WeatherSpHelper.getWeatherModel(location.poiId) ?: return
        when (widgetClass) {
            MyWidget::class.java -> renderSmall(context, location, model)
            MyDoubleWidget::class.java -> renderDouble(context, location, model)
        }
    }

    /**
     * 用缓存渲染小尺寸 widget（[R.layout.widget_layout_small]）并推送到系统。
     *
     * @param context 任意 Context
     * @param cityMode 当前城市
     * @param model 天气数据，可为 null（仅渲染城市名等基础信息）
     */
    fun renderSmall(context: Context, cityMode: CityMode, model: WeatherModel?) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout_small)

        applyBackground(remoteViews)

        remoteViews.setTextViewText(R.id.sAddressTv, cityMode.mergerName)
        remoteViews.setImageViewResource(R.id.locationImgView, R.drawable.ic_location_blue)

        if (model != null) {
            if (model.aqi > 0) {
                remoteViews.setViewVisibility(R.id.img_quality, View.VISIBLE)
                remoteViews.setImageViewResource(
                    R.id.img_quality, AqiHelper.getQualityRes(model.aqi)
                )
            } else {
                remoteViews.setViewVisibility(R.id.img_quality, View.GONE)
            }

            val current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH")
            var hadSetTemp = false
            model.hourlies?.let { hourlies ->
                for (hourly in hourlies) {
                    if (hourly == null) continue
                    val dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH")
                    if (dayHour == current) {
                        remoteViews.setImageViewResource(
                            R.id.sIconImgView, WeatherUtils.weatherImageRes(hourly.icon_cd)
                        )
                        remoteViews.setTextViewText(R.id.sTv_wx_phrase, hourly.getPhraseChar())
                        hourly.metric?.let { metric ->
                            remoteViews.setTextViewText(
                                R.id.sTv_rh_wrap,
                                String.format(
                                    "%s风 %d级｜ 湿度%d%%",
                                    hourly.wdir_cardinal,
                                    WeatherUtils.windGrade(metric.wspd.toFloat()),
                                    hourly.rh
                                )
                            )
                            remoteViews.setTextViewText(
                                R.id.sTv_temperature, String.format("%d°", metric.temp)
                            )
                            hadSetTemp = true
                        }
                        break
                    }
                }
            }

            if (!hadSetTemp && model.observation != null) {
                fillFromObservationSmall(remoteViews, model.observation)
            }
        }

        remoteViews.setOnClickPendingIntent(R.id.widgetBtn, buildClickIntent(context))
        pushWidget(context, MyWidget::class.java, remoteViews)
    }

    private fun fillFromObservationSmall(remoteViews: RemoteViews, observation: Observation) {
        val metric = observation.metric ?: return
        remoteViews.setTextViewText(
            R.id.sTv_rh_wrap,
            String.format(
                "%s风 %d级｜ 湿度%d%%",
                observation.wdirCardinal,
                WeatherUtils.windGrade(metric.wspd.toFloat()),
                observation.rh
            )
        )
        remoteViews.setImageViewResource(
            R.id.sIconImgView, WeatherUtils.weatherImageRes(observation.wxIcon)
        )
        remoteViews.setTextViewText(R.id.sTv_temperature, String.format("%d°", metric.temp))
        remoteViews.setTextViewText(R.id.sTv_wx_phrase, observation.getWxPhrase())
    }

    /**
     * 渲染双城 widget（[R.layout.widget_layout]）并推送到系统。
     */
    fun renderDouble(context: Context, cityMode: CityMode, model: WeatherModel?) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)

        applyBackground(remoteViews)

        remoteViews.setTextViewText(R.id.addressTv, cityMode.mergerName)
        remoteViews.setImageViewResource(R.id.locationImgView, R.drawable.ic_location_blue)

        if (model != null) {
            val today = model.today()
            if (today != null) {
                val currentTime = System.currentTimeMillis()
                val sunSetTime = TimeUtils.switchTime(today.sunSet)
                val isDay = currentTime < sunSetTime
                val todayPart = if (isDay) today.dayPart else today.nightPart

                remoteViews.setTextViewText(
                    R.id.tv_temperature_today,
                    String.format("%d/%d°", today.metric.maxTemp, today.metric.minTemp)
                )
                if (todayPart != null) {
                    val phraseChar = if (todayPart.getPhraseChar().isNotEmpty()) {
                        todayPart.getPhraseChar()
                    } else {
                        model.observation?.getWxPhrase().orEmpty()
                    }
                    remoteViews.setTextViewText(R.id.tv_weather_today, phraseChar)
                }

                if (model.aqi > 0) {
                    remoteViews.setViewVisibility(R.id.img_quality_today, View.VISIBLE)
                    remoteViews.setImageViewResource(
                        R.id.img_quality_today, AqiHelper.getQualityRes(model.aqi)
                    )
                } else {
                    remoteViews.setViewVisibility(R.id.img_quality_today, View.GONE)
                }

                renderTomorrow(remoteViews, model.tomorrow(), isDay)
            }

            val current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH")
            var hadSetTemp = false
            model.hourlies?.let { hourlies ->
                for (hourly in hourlies) {
                    if (hourly == null) continue
                    val dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH")
                    if (dayHour == current) {
                        remoteViews.setImageViewResource(
                            R.id.iconImgView, WeatherUtils.weatherImageRes(hourly.icon_cd)
                        )
                        remoteViews.setTextViewText(R.id.tv_wx_phrase, hourly.getPhraseChar())
                        hourly.metric?.let { metric ->
                            remoteViews.setTextViewText(
                                R.id.tv_rh_wrap,
                                String.format(
                                    "%s风 %d级｜ 湿度%d%%",
                                    hourly.wdir_cardinal,
                                    WeatherUtils.windGrade(metric.wspd.toFloat()),
                                    hourly.rh
                                )
                            )
                            remoteViews.setTextViewText(
                                R.id.tv_temperature, String.format("%d°", metric.temp)
                            )
                            hadSetTemp = true
                        }
                        break
                    }
                }
            }

            if (!hadSetTemp && model.observation != null) {
                fillFromObservationDouble(remoteViews, model.observation)
            }
        }

        remoteViews.setOnClickPendingIntent(R.id.widgetBtn, buildClickIntent(context))
        pushWidget(context, MyDoubleWidget::class.java, remoteViews)
    }

    private fun renderTomorrow(remoteViews: RemoteViews, tomorrow: Daily?, isDay: Boolean) {
        if (tomorrow == null) return
        val tomorrowPart = if (isDay) tomorrow.dayPart else tomorrow.nightPart
        remoteViews.setTextViewText(
            R.id.tv_temperature_morn,
            String.format("%d/%d°", tomorrow.metric.maxTemp, tomorrow.metric.minTemp)
        )
        if (tomorrowPart != null) {
            remoteViews.setTextViewText(R.id.tv_weather_morn, tomorrowPart.getPhraseChar())
        }
    }

    private fun fillFromObservationDouble(remoteViews: RemoteViews, observation: Observation) {
        val metric = observation.metric ?: return
        remoteViews.setTextViewText(
            R.id.tv_rh_wrap,
            String.format(
                "%s风 %d级｜ 湿度%d%%",
                observation.wdirCardinal,
                WeatherUtils.windGrade(metric.wspd.toFloat()),
                observation.rh
            )
        )
        remoteViews.setImageViewResource(
            R.id.iconImgView, WeatherUtils.weatherImageRes(observation.wxIcon)
        )
        remoteViews.setTextViewText(R.id.tv_temperature, String.format("%d°", metric.temp))
        remoteViews.setTextViewText(R.id.tv_wx_phrase, observation.getWxPhrase())
    }

    /**
     * 用 ComponentName 方式更新所有已安装的同类 widget。
     * 与原 Service 实现保持一致：通过 [AppWidgetManager.updateAppWidget] 推送。
     */
    private fun pushWidget(
        context: Context,
        widgetClass: Class<*>,
        remoteViews: RemoteViews
    ) {
        val componentName = ComponentName(context, widgetClass)
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews)
    }
}
