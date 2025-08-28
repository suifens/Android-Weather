package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.bean.Daily
import com.chunjing.tq.databinding.ItemWeatherListBinding
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.WeatherUtils

/**
 * com.chunjing.tq.adapter
 */
class WeatherListAdapter(val data: List<Daily>) : RecyclerView.Adapter<WeatherListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWeatherListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val daily = data[position]
        val timestamp = DateUtil.switchTime(daily.fcst_valid_local)
        //  时间
        holder.binding.weatherTimeTv.text =
            "${DateUtil.getWeek(timestamp)}（${TimeUtils.millis2String(timestamp, "MM月dd日")}）"

        //  温度
        val metric = daily.metric
        if (metric != null) {
            holder.binding.tvTemp.text = "${metric.maxTemp}/${metric.minTemp}°"
        }

        val weatherPart = daily.weatherPart
        if (weatherPart != null) {
            //  风、温度
            val detail = "${weatherPart.wdirCardinal}风${WeatherUtils.windGrade(weatherPart.wspd)}级" +
                    " | 湿度${weatherPart.rh}%"
            holder.binding.weatherDetailTv.text = detail
            //  图标
            holder.binding.iconImgV.load(WeatherUtils.getIcon(weatherPart.iconCd)) {
                transformations(RoundedCornersTransformation(SizeUtils.dp2px(16f).toFloat()))
            }
            //  天气
            holder.binding.tvDesc.text = weatherPart.getPhrasesChar()
        }

        mainViewModel.getDailyWeatherBg(daily) {
            if (it != null) {
                holder.binding.weatherImgV.load(it.imgPath, imageLoader) {
                    transformations(RoundedCornersTransformation(SizeUtils.dp2px(10f).toFloat()))
                }
            }
        }

    }

    override fun getItemCount() = data.size

    class ViewHolder(val binding: ItemWeatherListBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}