package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.databinding.ItemPeripheralBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.goodtech.weatherlib.utils.WeatherUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * com.chunjing.tq.adapter
 */
@SuppressLint("NotifyDataSetChanged")
class PeripheralAdapter(var data: List<CityEntity>) : RecyclerView.Adapter<PeripheralAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPeripheralBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    private val weatherMap = mutableMapOf<String, WeatherBean>()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data[position].let {
            holder.binding.cityNameTv.text = it.cityName

            val weather = weatherMap[it.cityId]
            weather?.let {
                updateWeather(holder, weather)
            }

            val bgRes = when (position%5) {
                0 -> R.drawable.bg_radius_fff8f2
                1 -> R.drawable.bg_radius_fff3f3
                2 -> R.drawable.bg_radius_edf4fd
                3 -> R.drawable.bg_radius_f3fae3
                4 -> R.drawable.bg_radius_f2f1ff
                else -> R.drawable.bg_radius_edf4fd
            }
            holder.binding.bgView.setBackgroundResource(bgRes)
        }
    }

    fun updateWeatherMap(map: Map<String, WeatherBean>) {
        weatherMap.clear()
        weatherMap.putAll(map)
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeather(holder: ViewHolder, weather: WeatherBean) {
        CoroutineScope(Dispatchers.Main).launch {
            val observation = weather.observation
            holder.binding.tempTv.text = "${observation.metric.maxTemp}/${observation.metric.minTemp}°"
            holder.binding.iconImgV.setImageResource(WeatherUtils.getIcon(observation.wxIcon))
            holder.binding.phraseTv.text = observation.getWxcPhrase()
            holder.binding.descTv.text = "${observation.wdirCardinal}风" +
                    "${WeatherUtils.windGrade(observation.metric.wspd)}级 | 湿度${observation.rh}%"
        }
    }

    override fun getItemCount() = data.size

    class ViewHolder(val binding: ItemPeripheralBinding) : RecyclerView.ViewHolder(binding.root)
}