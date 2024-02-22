package com.goodtech.tq.modules.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.weather.holder.GTAirLifeHolder
import com.goodtech.tq.modules.weather.holder.GTCurrentHolder
import com.goodtech.tq.modules.weather.holder.GTDailyListHolder
import com.goodtech.tq.modules.weather.holder.GTHoursHolder
import com.goodtech.tq.modules.weather.holder.GTLineTempHolder
import com.goodtech.tq.modules.weather.holder.GTObservationHolder
import com.goodtech.tq.modules.weather.holder.GTRecentHolder

@SuppressLint("NotifyDataSetChanged")
class WeatherAdapter(
    val context: Context,
    val listener: WeatherHeaderListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_CURRENT = 0
        const val ITEM_RECENT = 1
        const val ITEM_HOURS = 2
        const val ITEM_DAILY = 3
        const val ITEM_LINE_TEMP = 4
        const val ITEM_AIR_LIFE = 5
        const val ITEM_OBSERVATION = 6
    }

    private var mWeatherModel: WeatherModel? = null
    private var mCityMode: CityMode? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_RECENT -> {
                GTRecentHolder(GTRecentHolder.getBinding(context, parent))
            }
            ITEM_HOURS -> {
                GTHoursHolder(GTHoursHolder.getBinding(context, parent))
            }
            ITEM_DAILY -> {
                GTDailyListHolder(GTDailyListHolder.getBinding(context, parent))
            }
            ITEM_LINE_TEMP -> {
                GTLineTempHolder(GTLineTempHolder.getBinding(context, parent))
            }
            ITEM_AIR_LIFE -> {
                GTAirLifeHolder(GTAirLifeHolder.getBinding(context, parent))
            }
            ITEM_OBSERVATION -> {
                GTObservationHolder(GTObservationHolder.getBinding(context, parent))
            }
            else -> {
                GTCurrentHolder(GTCurrentHolder.getBinding(context, parent))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            1 -> ITEM_RECENT
            2 -> ITEM_HOURS
            3 -> ITEM_DAILY
            4 -> ITEM_LINE_TEMP
            5 -> ITEM_AIR_LIFE
            6 -> ITEM_OBSERVATION
            else -> ITEM_CURRENT
        }
    }

    fun changeWeather(model: WeatherModel?, cityMode: CityMode?) {
        mWeatherModel = model
        mCityMode = cityMode
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (mWeatherModel == null) return
        when(getItemViewType(position)) {
            ITEM_RECENT -> (holder as GTRecentHolder).setData(mWeatherModel)
            ITEM_HOURS -> (holder as GTHoursHolder).setData(mWeatherModel)
            ITEM_DAILY -> (holder as GTDailyListHolder).setData(mWeatherModel)
            ITEM_LINE_TEMP -> (holder as GTLineTempHolder).setData(mWeatherModel)
            ITEM_AIR_LIFE -> (holder as GTAirLifeHolder).setupLife(mWeatherModel!!.lifeModel, Color.WHITE)
            ITEM_OBSERVATION -> (holder as GTObservationHolder).setData(mWeatherModel)
            else -> {
                (holder as GTCurrentHolder).setData(mWeatherModel)
                holder.setItemListener(listener)
            }
        }
    }

    override fun getItemCount(): Int = 7
}