package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.clear
import coil.dispose
import coil.load
import coil.transform.RoundedCornersTransformation
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.databinding.ItemCityListBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.goodtech.weatherlib.utils.WeatherUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CityListAdapter(
    var cities: List<CityEntity>,
    var weatherMap: HashMap<String, WeatherBean>,
    var onClick: ((CityEntity?, Int) -> Unit)? = null
) :
    RecyclerView.Adapter<CityListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemCityListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    private fun changeAddHolder(holder: ViewHolder) {
        CoroutineScope(Dispatchers.Main).launch {
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
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            changeAddHolder(holder)
            holder.itemView.setOnClickListener {
                onClick?.invoke(null, 0)
            }

        } else {
            //  城市列表
            if (cities.size >= position) {
                val city = cities[position - 1]
                if (city.isLocal()) {
                    holder.binding.cityTv.text = city.mergerName
                    holder.binding.locationIcon.visibility = View.VISIBLE
                } else {
                    holder.binding.cityTv.text = city.cityName
                    holder.binding.locationIcon.visibility = View.GONE
                }
                holder.binding.cityTv.isSelected = true
                //  加载天气
                weatherMap[city.cityId]?.let { weather ->
                    updateWeatherDetail(holder, weather)
                }

                holder.itemView.setOnClickListener {
                    onClick?.invoke(city, position)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeatherDetail(holder: ViewHolder, weatherBean: WeatherBean) {
        CoroutineScope(Dispatchers.Main).launch {
            val observation = weatherBean.observation
            holder.binding.tvTemp.text = "${observation.metric.temp}°"
            holder.binding.iconImgV.load(WeatherUtils.getIcon(observation.wxIcon))
            holder.binding.tvDesc.text = observation.getWxcPhrase()
            holder.binding.weatherDetailTv.text = "${observation.wdirCardinal}风" +
                    "${WeatherUtils.windGrade(observation.metric.wspd)}级 | 湿度${observation.rh}%"

            mainViewModel.getWeatherBg(weatherBean) {
                if (it != null) {
                    if (holder.absoluteAdapterPosition == 0) {
                        changeAddHolder(holder)
                        return@getWeatherBg
                    }
                    holder.binding.weatherImgV.load(it.imgPath, imageLoader) {
                        transformations(
                            RoundedCornersTransformation(
                                SizeUtils.dp2px(10f).toFloat()
                            )
                        )
                    }
                }
            }
        }
    }

    override fun getItemCount() = cities.size + 1

    class ViewHolder(val binding: ItemCityListBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}