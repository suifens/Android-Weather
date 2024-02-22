package com.goodtech.tq.modules.weather.holder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.databinding.WeatherItemHoursBinding
import com.goodtech.tq.fragment.adapter.HoursRecyclerAdapter
import com.goodtech.tq.models.Hourly
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.TimeUtils

class GTHoursHolder(val binding: WeatherItemHoursBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun getBinding(context: Context, parent: ViewGroup): WeatherItemHoursBinding {
            return WeatherItemHoursBinding.inflate(LayoutInflater.from(context), parent, false)
        }
    }

    private var mAdapter: HoursRecyclerAdapter? = null

    init {
        mAdapter = HoursRecyclerAdapter(ArrayList<Any?>())
        binding.listHours.adapter = mAdapter
    }

    fun setData(model: WeatherModel?) {
        if (model?.hourlies != null) {
            val hourlies = ArrayList<Hourly>()
            checkFirstHourly(model, hourlies)
            val daily = model.dailies[0]
            val tomorrow = model.dailies[1]
            if (daily != null) {
                val tSunrise = TimeUtils.switchTime(daily.sunRise)
                val tSunset = TimeUtils.switchTime(daily.sunSet)
                val mSunrise = TimeUtils.switchTime(tomorrow.sunRise)
                val mSunset = TimeUtils.switchTime(tomorrow.sunSet)
                val current = System.currentTimeMillis()
                val currentStr = TimeUtils.timeToHH(System.currentTimeMillis())
                val riseString = TimeUtils.timeToHH(tSunrise)
                val setString = TimeUtils.timeToHH(tSunset)
                val sunrise = if (current > tSunrise) mSunrise else tSunrise
                val sunset = if (current > tSunset) mSunset else tSunset
                var addRise = false
                var addSet = false
                for (i in model.hourlies.indices) {
                    val hourly = model.hourlies[i]
                    if (currentStr.compareTo(riseString) != 0 && !addRise && sunrise < hourly.fcst_valid * 1000) {
                        addRise = true
                        val riseHourly = Hourly()
                        riseHourly.fcst_valid = sunrise / 1000
                        riseHourly.fcst_valid_local = daily.sunRise
                        riseHourly.sunrise = true
                        hourlies.add(riseHourly)
                    }
                    if (currentStr.compareTo(setString) != 0 && !addSet && sunset < hourly.fcst_valid * 1000) {
                        val setHourly = Hourly()
                        addSet = true
                        setHourly.fcst_valid = sunset / 1000
                        setHourly.fcst_valid_local = daily.sunSet
                        setHourly.sunset = true
                        hourlies.add(setHourly)
                    }
                    hourlies.add(hourly)
                }
            }
            setHourlies(hourlies)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setHourlies(hourlies: ArrayList<Hourly>) {
        mAdapter!!.notifyDataSetChanged(hourlies as List<Parcelable>?)
    }

    private fun checkFirstHourly(model: WeatherModel?, hourlies: ArrayList<Hourly>) {
        if (model?.hourlies != null && model.hourlies.size > 0) {
            val hourly = model.hourlies[0]
            if (hourly.fcst_valid > System.currentTimeMillis() / 1000) {
                if (model.observation != null) {
                    val observation = model.observation
                    val metric = observation.metric
                    val curHourly = Hourly()
                    curHourly.fcst_valid = System.currentTimeMillis() / 1000
                    curHourly.metric = metric
                    curHourly.icon_cd = observation.wxIcon
                    curHourly.phraseChar = observation.getWxPhrase()
                    hourlies.add(curHourly)
                }
            }
        }
    }

}