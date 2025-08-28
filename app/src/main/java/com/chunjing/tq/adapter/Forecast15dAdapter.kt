package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import coil.clear
import coil.load
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.Daily
import com.chunjing.tq.databinding.ItemForecast15Binding
import com.goodtech.weatherlib.ext.getRainfall
import com.goodtech.weatherlib.ext.visible
import com.goodtech.weatherlib.utils.CalendarUtil
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.WeatherUtils

class Forecast15dAdapter(val context: Context, val datas: List<Daily>) :
    RecyclerView.Adapter<Forecast15dAdapter.ViewHolder>() {

    private var mMin = 0
    private var mMax = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemForecast15Binding.inflate(LayoutInflater.from(context), parent,false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datas[position]
        holder.binding.apply {
            tvWeek.text = getWeekDay(position)
            val time = DateUtil.switchTime(item.fcst_valid_local)
            tvDate.text = TimeUtils.millis2String(time, "MM/dd")

            if (TimeUtils.isToday(time)) {
                dayItem.setBackgroundResource(R.drawable.gradient_forecast_today)
            } else {
                dayItem.setBackgroundColor(Color.TRANSPARENT)
            }

            tvDayTemp.text = "${item.metric?.maxTemp}°"
            tvNightTemp.text = "${item.metric?.minTemp}°"

            if (item.dayPart != null) {
                val part = item.dayPart
                tvDayDesc.text = part.getPhrasesChar()
                ivDay.visibility = View.VISIBLE
                ivDay.load(WeatherUtils.getIcon(part.iconCd))

                val rainfall = part.getPhrasesChar().getRainfall()
                if (rainfall > 0) {
                    dayRainfallTv.text = "$rainfall%"
                    dayRainfallTv.visibility = View.VISIBLE
                } else {
                    dayRainfallTv.visibility = View.GONE
                }
            } else {
                tvDayDesc.text = ""
                ivDay.visibility = View.INVISIBLE
                dayRainfallTv.visibility = View.GONE
            }

            item.nightPart?.let {
                tvNightDesc.text = it.getPhrasesChar()
                ivNight.load(WeatherUtils.getIcon(it.iconCd))
                tvWind.text = it.wdirCardinal
                tvWindScale.text = "${WeatherUtils.windGrade(it.wspd)}级"

                val rainfall = it.getPhrasesChar().getRainfall()
                if (rainfall > 0) {
                    nightRainfallTv.text = "$rainfall%"
                    nightRainfallTv.visibility = View.VISIBLE
                } else {
                    nightRainfallTv.visibility = View.GONE
                }
            }

            tempChart.setData(
                mMin,
                mMax,
                if (position == 0) null else datas[position - 1],
                item,
                if (position == datas.size - 1) null else datas[position + 1]
            )
        }
    }

    private fun getWeekDay(position: Int): String {

        val timestamp = DateUtil.switchTime(datas[position].fcst_valid_local)
        val time = TimeUtils.millis2String(timestamp, "yyyy-MM-dd")

        val timeDay = CalendarUtil.timeToDay(timestamp).toInt()
        val today = CalendarUtil.timeToDay(System.currentTimeMillis()).toInt()

        val weekStr = CalendarUtil.getWeek(time)
        return when (timeDay - today) {
            -1 -> "昨天"
            0 -> "今天"
            1 -> "明天"
            else -> weekStr
        }
    }

    override fun getItemCount(): Int = datas.size

    @SuppressLint("NotifyDataSetChanged")
    fun setRange(min: Int, max: Int) {
        mMin = min
        mMax = max
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemForecast15Binding) : RecyclerView.ViewHolder(binding.root)
}