package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.chunjing.tq.bean.Hourly
import com.chunjing.tq.databinding.ItemForecastBinding
import com.goodtech.weatherlib.ext.getRainfall
import com.goodtech.weatherlib.utils.WeatherUtils

@SuppressLint("NotifyDataSetChanged")
class ForecastHourlyAdapter(val context: Context, val datas: List<Hourly>) :
    RecyclerView.Adapter<ForecastHourlyAdapter.ViewHolder>() {

    private var mMin = 0
    private var mMax = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemForecastBinding.inflate(LayoutInflater.from(context), parent,false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datas[position]
        holder.binding.apply {
            timeTv.text = item.time
            tempTv.text = "${item.temp}°"
            iconImgV.load(WeatherUtils.getIcon(item.icon_cd))
            phraseTv.text = item.getPhrasesChar()

            val rainfall = item.getPhrasesChar().getRainfall()
            if (rainfall > 0) {
                rainfallTv.text = "$rainfall%"
                rainfallTv.visibility = View.VISIBLE
            } else {
                rainfallTv.visibility = View.GONE
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

    override fun getItemCount(): Int = datas.size

    fun setRange(min: Int, max: Int) {
        mMin = min
        mMax = max
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root)
}