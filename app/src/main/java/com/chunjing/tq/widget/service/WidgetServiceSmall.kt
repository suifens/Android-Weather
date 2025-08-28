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
import com.chunjing.tq.ui.activity.WidgetSettingActivity
import com.chunjing.tq.ui.fragment.vm.CACHE_WEATHER_NOW
import com.chunjing.tq.utils.AqiHelper
import com.chunjing.tq.widget.WeatherWidgetSmall
import com.goodtech.weatherlib.utils.SpUtils
import com.goodtech.weatherlib.utils.WeatherUtils.getIcon

class WidgetServiceSmall : BaseWidgetService() {

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override suspend fun updateWidget(
        cityId: String,
        cityName: String,
        now: WeatherBean?,
        aqi: Int
    ) {
        //LogUtils.d("updateWidget............. ${now != null}")

        val views = RemoteViews(packageName, R.layout.widget_layout_small)
        val location = if (cityName.contains("-")) cityName.split("-")[1] else cityName
        views.setTextViewText(R.id.tvLocation, location)
        views.setImageViewResource(R.id.locationImgView, R.drawable.ic_loc_w)

        now?.let {
            AppRepo.getInstance()
                .saveCache(CACHE_WEATHER_NOW + cityId, it)

            val observation = it.observation
            views.setTextViewText(R.id.tvWeather, observation.getWxcPhrase())
            views.setTextViewText(R.id.tvTemp, "${observation.metric.temp}°")
            views.setTextViewText(R.id.tvParse, it.getPhrase())
            views.setImageViewResource(R.id.ivWeather, getIcon(observation.wxIcon))

        }
        if (aqi > 0) {
            views.setViewVisibility(R.id.qualityImgV, View.VISIBLE)
            views.setImageViewResource(
                R.id.qualityImgV,
                AqiHelper.getQualityRes(aqi)
            )
        } else {
            views.setViewVisibility(R.id.qualityImgV, View.GONE)
        }

        val widgetTypeStr = SpUtils.instance.getString(
            WIDGET_TYPE,
            WidgetSettingActivity.WidgetType.SingleLine1.toString()
        )
        when (WidgetSettingActivity.WidgetType.valueOf(widgetTypeStr)) {
            WidgetSettingActivity.WidgetType.SingleLine1, WidgetSettingActivity.WidgetType.DoubleLine1 ->
                views.setViewVisibility(R.id.ivBackground, View.VISIBLE)
            else -> views.setViewVisibility(R.id.ivBackground, View.GONE)
        }

        initEvent(views)

        val componentName = ComponentName(this, WeatherWidgetSmall::class.java)
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
        views.setOnClickPendingIntent(R.id.widgetBtn, weatherPI)
    }
}