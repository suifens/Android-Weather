package com.goodtech.tq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.R
import com.goodtech.tq.databinding.ItemPeripheralBinding
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.WeatherUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * com.chunjing.tq.adapter
 */
@SuppressLint("NotifyDataSetChanged")
class PeripheralAdapter(var data: List<CityMode>) : RecyclerView.Adapter<PeripheralAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPeripheralBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    private val weatherMap = mutableMapOf<String, WeatherModel>()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data[position].let {
            holder.binding.cityNameTv.text = it.mergerName

            val weather = weatherMap[it.poiId]
            weather?.let {
                updateWeather(holder, weather)
            }

            val bgRes = when (position%5) {
                0 -> R.color.color_fff8f2
                1 -> R.color.color_fff3f3
                2 -> R.color.color_edf4fd
                3 -> R.color.color_f3fae3
                4 -> R.color.color_f2f1ff
                else -> R.color.color_fff8f2
            }
            holder.binding.bgView.setBackgroundResource(bgRes)
        }
    }

    fun updateWeatherMap(map: Map<String, WeatherModel>) {
        weatherMap.clear()
        weatherMap.putAll(map)
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeather(holder: ViewHolder, weather: WeatherModel) {
        CoroutineScope(Dispatchers.Main).launch {
            val observation = weather.observation
            holder.binding.tempTv.text = "${observation.metric.maxTemp}/${observation.metric.minTemp}°"
            holder.binding.iconImgV.setImageResource(WeatherUtils.weatherImageRes(observation.wxIcon))
            holder.binding.phraseTv.text = observation.wxPhrase
            holder.binding.descTv.text = "${observation.wdirCardinal}风" +
                    "${WeatherUtils.windGrade(observation.metric.wspd.toFloat())}级 | 湿度${observation.rh}%"
        }
    }

    override fun getItemCount() = data.size

    class ViewHolder(val binding: ItemPeripheralBinding) : RecyclerView.ViewHolder(binding.root)
}