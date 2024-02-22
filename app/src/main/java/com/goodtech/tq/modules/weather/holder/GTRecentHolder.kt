package com.goodtech.tq.modules.weather.holder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.databinding.WeatherItemRecentBinding
import com.goodtech.tq.helpers.AqiHelper
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.TimeUtils

class GTRecentHolder(val binding: WeatherItemRecentBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun getBinding(context: Context, parent: ViewGroup): WeatherItemRecentBinding {
            return WeatherItemRecentBinding.inflate(LayoutInflater.from(context), parent, false)
        }
    }

    @SuppressLint("DefaultLocale")
    fun setData(weatherModel: WeatherModel?) {
        if (weatherModel != null) {
            val observation = weatherModel.observation
            val metric = observation.metric
            val today = weatherModel.today()
            if (today != null) {
                binding.tvNotice.text = String.format(
                    "今天：当前%s，最高气温%d°，最低气温%d°", observation.getWxPhrase(),
                    today.metric.maxTemp, today.metric.minTemp
                )
            } else {
                binding.tvNotice.text = String.format(
                    "今天：当前%s，最高气温%d°，最低气温%d°", observation.getWxPhrase(),
                    metric.maxTemp, metric.minTemp
                )
            }
            if (today != null) {
                val currentTime = System.currentTimeMillis()
                val sunSetTime = TimeUtils.switchTime(today.sunSet)
                val day = currentTime < sunSetTime
                val todayPart = if (day) today.dayPart else today.nightPart
                binding.tvTemperatureToday.text = String.format(
                    "%d°/%d°",
                    today.metric.maxTemp,
                    today.metric.minTemp
                )
                if (todayPart != null) {
                    if (todayPart.getPhraseChar().isNotEmpty()) {
                        binding.tvWeatherToday.text = todayPart.getPhraseChar()
                    } else {
                        binding.tvWeatherToday.text = observation.getWxPhrase()
                    }
                }
                if (weatherModel.aqi > 0) {
                    binding.tvQualityToday.visibility = View.VISIBLE
                    binding.tvQualityToday.text = AqiHelper.getQuality(weatherModel.aqi)
                    binding.tvQualityToday.setBackgroundResource(AqiHelper.getBgColorResId(weatherModel.aqi))
                } else {
                    binding.tvQualityToday.visibility = View.GONE
                }
                val tomorrow = weatherModel.tomorrow()
                if (tomorrow != null) {
                    val tomorrowPart = if (day) tomorrow.dayPart else tomorrow.nightPart
                    binding.tvTemperatureMorn.text = String.format(
                        "%d°/%d°",
                        tomorrow.metric.maxTemp,
                        tomorrow.metric.minTemp
                    )
                    if (tomorrowPart != null) binding.tvWeatherMorn.text = tomorrowPart.getPhraseChar()
                }
            }
        }
    }

}