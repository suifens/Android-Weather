package com.goodtech.tq.modules.weather.holder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.databinding.WeatherItemObservationBinding
import com.goodtech.tq.models.Daily
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.TimeUtils

class GTObservationHolder(val binding: WeatherItemObservationBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun getBinding(context: Context, parent: ViewGroup): WeatherItemObservationBinding {
            return WeatherItemObservationBinding.inflate(LayoutInflater.from(context), parent, false)
        }
    }

    @SuppressLint("DefaultLocale")
    fun setData(model: WeatherModel?) {
        if (model != null) {
            val observation = model.observation ?: return
            val metric = observation.metric
            val current = System.currentTimeMillis()
            val currentStr = TimeUtils.longToString(current, "yyyy-MM-dd")
            var minTemp: String? = null
            var maxTemp: String? = null
            if (model.dailies != null) {
                var daily: Daily? = null
                for (i in model.dailies.indices) {
                    val temp = model.dailies[i]
                    if (temp != null && temp.fcst_valid_local.contains(currentStr)) {
                        daily = temp
                        break
                    }
                }
                if (daily != null) {
                    val sunrise = TimeUtils.timeToHHmm(TimeUtils.switchTime(daily.sunRise))
                    val sunset = TimeUtils.timeToHHmm(TimeUtils.switchTime(daily.sunSet))
                    binding.tvTimeSunrise.text = String.format("日出%s", sunrise)
                    binding.tvTimeSunset.text = String.format("日落%s", sunset)
                    minTemp = String.format("%d°", daily.metric.minTemp)
                    maxTemp = String.format("%d°", daily.metric.maxTemp)
                }
            }
            if (minTemp == null) {
                minTemp = String.format("%d°", metric.minTemp)
                maxTemp = String.format("%d°", metric.maxTemp)
            }
            binding.tvMinTemp.text = minTemp
            binding.tvMaxTemp.text = maxTemp
            binding.layoutWindSpeed.setValue(String.format("%d", metric.wspd))
            binding.layoutRh.setValue(String.format("%d%%", observation.rh))
            binding.layoutDewpt.setValue(String.format("%d°", metric.dewpt))
            binding.layoutPressure.setValue(String.format("%.1f", metric.pressure))
            binding.layoutUvIndex.setValue(String.format("%d", observation.uvIndex))
            binding.layoutVisibility.setValue(String.format("%.2f", metric.vis))
        }
    }

}