package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import coil.transform.RoundedCornersTransformation
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.databinding.ItemCityListBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.goodtech.weatherlib.utils.WeatherUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class CityListRow {
    data object Add : CityListRow()
    data class City(val entity: CityEntity) : CityListRow()
}

class CityListAdapter(
    private val onClick: ((CityEntity?, Int) -> Unit)? = null
) : ListAdapter<CityListRow, CityListAdapter.ViewHolder>(DIFF) {

    private var weatherMap: Map<String, WeatherBean> = emptyMap()

    fun submit(cities: List<CityEntity>, weather: Map<String, WeatherBean>) {
        weatherMap = weather
        val rows = ArrayList<CityListRow>(cities.size + 1)
        rows.add(CityListRow.Add)
        cities.forEach { rows.add(CityListRow.City(it)) }
        submitList(rows)
    }

    fun updateWeatherMap(weather: Map<String, WeatherBean>) {
        weatherMap = weather
        if (itemCount > 1) {
            notifyItemRangeChanged(1, itemCount - 1, PAYLOAD_WEATHER)
        }
    }

    fun updateWeatherForCity(cityId: String, weather: WeatherBean) {
        weatherMap = weatherMap + (cityId to weather)
        val index = currentList.indexOfFirst { row ->
            row is CityListRow.City && row.entity.cityId == cityId
        }
        if (index >= 0) {
            notifyItemChanged(index + 1, PAYLOAD_WEATHER)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCityListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_WEATHER)) {
            val row = getItem(position)
            if (row is CityListRow.City) {
                weatherMap[row.entity.cityId]?.let { bindWeather(holder, it) }
            }
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindJob?.cancel()
        when (val row = getItem(position)) {
            is CityListRow.Add -> {
                bindAddHolder(holder)
                holder.itemView.setOnClickListener {
                    onClick?.invoke(null, 0)
                }
            }
            is CityListRow.City -> {
                val city = row.entity
                if (city.isLocal()) {
                    holder.binding.cityTv.text = city.mergerName
                    holder.binding.locationIcon.visibility = View.VISIBLE
                } else {
                    holder.binding.cityTv.text = city.cityName
                    holder.binding.locationIcon.visibility = View.GONE
                }
                holder.binding.cityTv.isSelected = true

                weatherMap[city.cityId]?.let { weather ->
                    bindWeather(holder, weather)
                } ?: run {
                    holder.binding.tvTemp.text = ""
                    holder.binding.tvDesc.text = ""
                    holder.binding.weatherDetailTv.text = ""
                    holder.binding.iconImgV.dispose()
                }

                holder.itemView.setOnClickListener {
                    onClick?.invoke(city, position)
                }
            }
        }
    }

    private fun bindAddHolder(holder: ViewHolder) {
        holder.binding.weatherImgV.load(R.drawable.img_city_add) {
            transformations(RoundedCornersTransformation(SizeUtils.dp2px(10f).toFloat()))
        }
        holder.binding.cityTv.text = "添加城市"
        holder.binding.tvTemp.text = ""
        holder.binding.iconImgV.dispose()
        holder.binding.tvDesc.text = ""
        holder.binding.weatherDetailTv.text = ""
        holder.binding.locationIcon.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun bindWeather(holder: ViewHolder, weatherBean: WeatherBean) {
        val observation = weatherBean.observation
        holder.binding.tvTemp.text = "${observation.metric.temp}°"
        holder.binding.iconImgV.load(WeatherUtils.getIcon(observation.wxIcon))
        holder.binding.tvDesc.text = observation.getWxcPhrase()
        holder.binding.weatherDetailTv.text = "${observation.wdirCardinal}风" +
                "${WeatherUtils.windGrade(observation.metric.wspd)}级 | 湿度${observation.rh}%"

        holder.bindJob?.cancel()
        holder.bindJob = holder.scope.launch {
            val bg = awaitWeatherBg(weatherBean) ?: return@launch
            if (holder.bindingAdapterPosition == RecyclerView.NO_POSITION) return@launch
            if (getItem(holder.bindingAdapterPosition) is CityListRow.Add) {
                bindAddHolder(holder)
                return@launch
            }
            holder.binding.weatherImgV.load(bg.imgPath, imageLoader) {
                transformations(
                    RoundedCornersTransformation(SizeUtils.dp2px(10f).toFloat())
                )
            }
        }
    }

    private suspend fun awaitWeatherBg(weatherBean: WeatherBean): WeatherBgEntity? =
        suspendCancellableCoroutine { cont ->
            mainViewModel.getWeatherBg(weatherBean) { entity ->
                if (cont.isActive) {
                    cont.resume(entity)
                }
            }
        }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.bindJob?.cancel()
        holder.binding.weatherImgV.dispose()
        holder.binding.iconImgV.dispose()
        super.onViewRecycled(holder)
    }

    class ViewHolder(val binding: ItemCityListBinding) : RecyclerView.ViewHolder(binding.root) {
        val scope = MainScope()
        var bindJob: Job? = null
    }

    companion object {
        private const val PAYLOAD_WEATHER = "weather"

        private val DIFF = object : DiffUtil.ItemCallback<CityListRow>() {
            override fun areItemsTheSame(oldItem: CityListRow, newItem: CityListRow): Boolean {
                return when {
                    oldItem is CityListRow.Add && newItem is CityListRow.Add -> true
                    oldItem is CityListRow.City && newItem is CityListRow.City ->
                        oldItem.entity.cityId == newItem.entity.cityId
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: CityListRow, newItem: CityListRow): Boolean {
                return when {
                    oldItem is CityListRow.Add && newItem is CityListRow.Add -> true
                    oldItem is CityListRow.City && newItem is CityListRow.City -> {
                        val a = oldItem.entity
                        val b = newItem.entity
                        a.cityId == b.cityId &&
                            a.cityName == b.cityName &&
                            a.mergerName == b.mergerName &&
                            a.sortOrder == b.sortOrder &&
                            a.latitude == b.latitude &&
                            a.longitude == b.longitude
                    }
                    else -> false
                }
            }
        }
    }
}
