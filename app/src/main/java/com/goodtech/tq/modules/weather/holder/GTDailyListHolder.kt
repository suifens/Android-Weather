package com.goodtech.tq.modules.weather.holder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.databinding.WeatherDailyListBinding
import com.goodtech.tq.models.WeatherModel

class GTDailyListHolder(val binding: WeatherDailyListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun getBinding(context: Context, parent: ViewGroup): WeatherDailyListBinding {
            return WeatherDailyListBinding.inflate(LayoutInflater.from(context), parent, false)
        }
    }

    @SuppressLint("DefaultLocale")
    fun setData(model: WeatherModel?) {
        if (model == null || model.dailies == null) {
            return
        }
        for (i in model.dailies.indices) {
            val daily = model.dailies[i]
            when (i) {
                0 -> binding.itemDaily1.setData(model, daily)
                1 -> binding.itemDaily2.setData(model, daily)
                2 -> binding.itemDaily3.setData(model, daily)
                3 -> binding.itemDaily4.setData(model, daily)
                4 -> binding.itemDaily5.setData(model, daily)
                5 -> binding.itemDaily6.setData(model, daily)
                6 -> binding.itemDaily7.setData(model, daily)
                7 -> binding.itemDaily8.setData(model, daily)
                8 -> binding.itemDaily9.setData(model, daily)
                9 -> binding.itemDaily10.setData(model, daily)
                10 -> {
                    binding.line11.visibility = View.VISIBLE
                    binding.itemDaily11.visibility = View.VISIBLE
                    binding.itemDaily11.setData(model, daily)
                }
            }
        }
    }

}