package com.goodtech.tq.modules.weather.holder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.databinding.WeatherItemLineBinding
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.widget.weatherview.WeatherView

class GTLineTempHolder(val binding: WeatherItemLineBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun getBinding(context: Context, parent: ViewGroup): WeatherItemLineBinding {
            return WeatherItemLineBinding.inflate(LayoutInflater.from(context), parent, false)
        }
    }

    init {
        binding.weatherView.lineType = WeatherView.LINE_TYPE_CURVE
        binding.weatherView.lineWidth = 2f
        //设置一屏幕显示几列(最少3列)
        try {
            binding.weatherView.setColumnNumber(5)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //设置白天和晚上线条的颜色
        binding.weatherView.setDayAndNightLineColor(
            Color.parseColor("#FFD34E"),
            Color.parseColor("#00C4FF")
        )
    }

    @SuppressLint("DefaultLocale")
    fun setData(model: WeatherModel?) {
        if (model?.dailies != null) {
            //填充天气数据
            binding.weatherView.list = model.dailies
            Handler(Looper.getMainLooper()).postDelayed({ binding.weatherView.invalidate() }, 100)
        }
    }

}