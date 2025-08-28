package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.clear
import coil.dispose
import coil.load
import coil.transform.RoundedCornersTransformation
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.databinding.ItemWeatherMainBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.goodtech.weatherlib.utils.WeatherUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * com.chunjing.tq.adapter
 */
class WeatherItemsAdapter(
    var data: List<CityEntity>,
    var callback: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<WeatherItemsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWeatherMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.binding.tempTv.text = ""
            holder.binding.iconImgV.setImageResource(0)
            holder.binding.phraseTv.text = ""
            holder.binding.weatherDetailTv.text = ""
            holder.binding.tvCity.text = ""
            holder.binding.imgWeather.load(R.drawable.item_add)
        }
        else {
            val index = position - 1
            val city = data[index]
            if (city.isLocal()) {
                holder.binding.tvCity.isSelected = true
                holder.binding.tvCity.text = city.mergerName
                holder.binding.locationImgView.load(R.drawable.ic_loc_w)
            } else {
                holder.binding.tvCity.text = city.cityName
                holder.binding.locationImgView.dispose()
            }

            var weatherBean = mainViewModel.getCityWeather(city.cityId)
            if (weatherBean == null) {
                mainViewModel.getWeatherCache(city.cityId) {
                    weatherBean = it
                    updateWeather(holder, weatherBean)
                }
            } else {
                updateWeather(holder, weatherBean)
            }
        }

        holder.itemView.setOnClickListener {
            callback?.invoke(position)
        }
    }

    private fun updateWeather(holder: ViewHolder, weather: WeatherBean?) {
        if (weather != null) {
            changeWeather(holder, weather)
            mainViewModel.getWeatherBg(weather) {
                it?.let { bgEntity ->
                    if (holder.absoluteAdapterPosition == 0) {
                        holder.binding.imgWeather.load(R.drawable.item_add)
                        return@let
                    }
                    holder.binding.imgWeather.load(bgEntity.imgPath, imageLoader) {
                        placeholder(R.drawable.bg_radius20_purple)
                        crossfade(true) //渐进进出
                        transformations(RoundedCornersTransformation(SizeUtils.dp2px(20f).toFloat()))
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeWeather(holder: ViewHolder, weather: WeatherBean) {
        CoroutineScope(Dispatchers.Main).launch {
            val observation = weather.observation
            holder.binding.tempTv.text = "${observation.metric.temp}°"
            holder.binding.iconImgV.setImageResource(WeatherUtils.getIcon(observation.wxIcon))
            holder.binding.phraseTv.text = observation.getWxcPhrase()
            holder.binding.weatherDetailTv.text = "${observation.wdirCardinal}风" +
                    "${WeatherUtils.windGrade(observation.metric.wspd)}级\n湿度${observation.rh}%"
        }
    }

    override fun getItemCount() = data.size + 1

    class ViewHolder(val binding: ItemWeatherMainBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}
