package com.goodtech.tq.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.goodtech.tq.R
import com.goodtech.tq.ad.AdManager
import com.goodtech.tq.fragment.view.*
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.others.airQuality.view.AirLifeView

private fun View.setMatchParentWidth() {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

class WeatherAdapter : RecyclerView.Adapter<WeatherViewHolder>() {
    
    companion object {
        private const val TYPE_CURRENT = 0
        private const val TYPE_RECENT = 1
        private const val TYPE_HOURS = 2
        private const val TYPE_AD1 = 3
        private const val TYPE_DAILY = 4
        private const val TYPE_LINE_TEMP = 5
        private const val TYPE_AD2 = 6
        private const val TYPE_LIFE = 7
        private const val TYPE_OBSERVATION = 8
    }

    private var weatherModel: WeatherModel? = null
    private var cityMode: CityMode? = null
    private var feedAd1: TTFeedAd? = null
    private var feedAd2: TTFeedAd? = null
    private var weatherHeaderListener: WeatherHeaderListener? = null

    fun setData(model: WeatherModel, mode: CityMode?) {
        weatherModel = model
        cityMode = mode
        notifyDataSetChanged()
    }

    fun setAd1(ad: TTFeedAd?) {
        feedAd1 = ad
        notifyItemChanged(3)
    }

    fun setAd2(ad: TTFeedAd?) {
        feedAd2 = ad
        notifyItemChanged(6)
    }

    fun setWeatherHeaderListener(listener: WeatherHeaderListener) {
        weatherHeaderListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_CURRENT
            1 -> TYPE_RECENT
            2 -> TYPE_HOURS
            3 -> TYPE_AD1
            4 -> TYPE_DAILY
            5 -> TYPE_LINE_TEMP
            6 -> TYPE_AD2
            7 -> TYPE_LIFE
            8 -> TYPE_OBSERVATION
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        return when (viewType) {
            TYPE_CURRENT -> {
                val view = CurrentItemView(parent.context)
                view.setMatchParentWidth()
                view.setItemListener(weatherHeaderListener)
                WeatherViewHolder.CurrentViewHolder(view)
            }
            TYPE_RECENT -> {
                val view = RecentItemView(parent.context)
                view.setMatchParentWidth()
                WeatherViewHolder.RecentViewHolder(view)
            }
            TYPE_HOURS -> {
                val view = HoursItemView(parent.context)
                view.setMatchParentWidth()
                WeatherViewHolder.HoursViewHolder(view)
            }
            TYPE_AD1, TYPE_AD2 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ad_container, parent, false)
                view.setMatchParentWidth()
                WeatherViewHolder.AdViewHolder(view)
            }
            TYPE_DAILY -> {
                val view = DailyListItemView(parent.context)
                view.setMatchParentWidth()
                WeatherViewHolder.DailyViewHolder(view)
            }
            TYPE_LINE_TEMP -> {
                val view = LineTempItemView(parent.context)
                view.setMatchParentWidth()
                WeatherViewHolder.LineTempViewHolder(view)
            }
            TYPE_LIFE -> {
                val view = AirLifeView(parent.context)
                view.setMatchParentWidth()
                WeatherViewHolder.LifeViewHolder(view)
            }
            TYPE_OBSERVATION -> {
                val view = ObservationView(parent.context)
                view.setMatchParentWidth()
                WeatherViewHolder.ObservationViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        when (holder) {
            is WeatherViewHolder.CurrentViewHolder -> {
                weatherModel?.let { holder.view.setData(it) }
            }
            is WeatherViewHolder.RecentViewHolder -> {
                weatherModel?.let { holder.view.setData(it) }
            }
            is WeatherViewHolder.HoursViewHolder -> {
                weatherModel?.let { holder.view.setHourlies(it) }
            }
            is WeatherViewHolder.DailyViewHolder -> {
                weatherModel?.let { holder.view.setData(it) }
            }
            is WeatherViewHolder.LineTempViewHolder -> {
                weatherModel?.let { holder.view.setData(it) }
            }
            is WeatherViewHolder.LifeViewHolder -> {
                weatherModel?.lifeModel?.let { holder.view.setupLife(it, Color.WHITE) }
            }
            is WeatherViewHolder.ObservationViewHolder -> {
                weatherModel?.let { holder.view.setData(it) }
            }
            is WeatherViewHolder.AdViewHolder -> {
                if (position == 3 && feedAd1 != null) {
                    showAd(holder.feedContainer, feedAd1!!)
                } else if (position == 6 && feedAd2 != null) {
                    showAd(holder.feedContainer, feedAd2!!)
                }
            }
        }
    }

    override fun getItemCount(): Int = 9

    private fun showAd(container: FrameLayout, ad: TTFeedAd) {
        container.visibility = View.VISIBLE
        AdManager.getInstance().showFeedAd(container.context as android.app.Activity, container, ad)
    }
} 