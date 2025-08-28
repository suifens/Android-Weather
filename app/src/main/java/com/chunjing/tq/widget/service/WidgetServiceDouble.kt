package com.chunjing.tq.widget.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import com.blankj.utilcode.util.LogUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.db.AppRepo
import com.chunjing.tq.ui.activity.SplashActivity
import com.chunjing.tq.ui.activity.WIDGET_TYPE
import com.chunjing.tq.ui.activity.WidgetSettingActivity.WidgetType
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_NOW
import com.chunjing.tq.utils.AqiHelper
import com.chunjing.tq.widget.WeatherWidgetDouble
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.WeatherUtils.getIcon

class WidgetServiceDouble : BaseWidgetService() {

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override suspend fun updateWidget(cityId: String, cityName: String, now: WeatherBean?, aqi: Int) {
        //LogUtils.d("updateDoubleWidget.............")

        val views = RemoteViews(packageName, R.layout.widget_layout_double)
        val location = if (cityName.contains("-")) cityName.split("-")[1] else cityName
        views.setTextViewText(R.id.dtvLocation, location)
        views.setImageViewResource(R.id.dLocationImgView, R.drawable.ic_loc_w)

        now?.let {
            AppRepo.getInstance()
                .saveCache(CACHE_WEATHER_NOW + cityId, it)

            val observation = it.observation
            views.setTextViewText(R.id.dtvWeather, observation.getWxcPhrase())
            views.setTextViewText(R.id.dtvTemp, "${observation.metric.temp}°")
            views.setTextViewText(R.id.dtvParse, it.getPhrase())
            views.setImageViewResource(R.id.divWeather, getIcon(observation.wxIcon))
            views.setImageViewResource(R.id.umbrellaImgV, R.drawable.ic_umbrella)
            views.setImageViewResource(R.id.choseImgV, R.drawable.ic_chose_r)

            var showRain = false
            if (it.hourlies.size > 2) {
                val firstHour = it.hourlies[0]
                val secondHour = it.hourlies[1]
                if (firstHour.getPhrasesChar().contains("雨")
                    || firstHour.getPhrasesChar().contains("雪")
                    || firstHour.getPhrasesChar().contains("阴")
                    || secondHour.getPhrasesChar().contains("雨")
                    || secondHour.getPhrasesChar().contains("雪")
                    || secondHour.getPhrasesChar().contains("阴")) {
                    showRain = true
                }
                val rainTip = if (showRain) "未来两小时可能有雨" else "未来两小时不会下雨"
                views.setTextViewText(R.id.rainTip, rainTip)
            }
        }
        if (aqi > 0) {
            views.setViewVisibility(R.id.dQualityImgV, View.VISIBLE)
            views.setImageViewResource(
                R.id.dQualityImgV,
                AqiHelper.getQualityRes(aqi)
            )
        } else {
            views.setViewVisibility(R.id.dQualityImgV, View.GONE)
        }

        val widgetTypeStr = SpUtils.instance.getString(WIDGET_TYPE, WidgetType.SingleLine1.toString())
        when (WidgetType.valueOf(widgetTypeStr)) {
            WidgetType.SingleLine1, WidgetType.DoubleLine1 ->
                views.setViewVisibility(R.id.divBackground, View.VISIBLE)
            else -> views.setViewVisibility(R.id.divBackground, View.GONE)
        }

        initEvent(views)

        val componentName = ComponentName(this, WeatherWidgetDouble::class.java)
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, views);
    }

    /**
     * 点击事件相关
     */
    private fun initEvent(views: RemoteViews) {
        // 风云
        val weatherIntent = Intent(this, SplashActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT

        val weatherPI =
            PendingIntent.getActivity(this, 0, weatherIntent, flags)
        views.setOnClickPendingIntent(R.id.dWidgetBtn, weatherPI)
    }
}