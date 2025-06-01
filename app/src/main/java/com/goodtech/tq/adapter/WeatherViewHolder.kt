package com.goodtech.tq.adapter

import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.R
import com.goodtech.tq.fragment.view.*
import com.goodtech.tq.modules.others.airQuality.view.AirLifeView

sealed class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    class CurrentViewHolder(val view: CurrentItemView) : WeatherViewHolder(view)
    class RecentViewHolder(val view: RecentItemView) : WeatherViewHolder(view)
    class HoursViewHolder(val view: HoursItemView) : WeatherViewHolder(view)
    class DailyViewHolder(val view: DailyListItemView) : WeatherViewHolder(view)
    class LineTempViewHolder(val view: LineTempItemView) : WeatherViewHolder(view)
    class LifeViewHolder(val view: AirLifeView) : WeatherViewHolder(view)
    class ObservationViewHolder(val view: ObservationView) : WeatherViewHolder(view)
    class AdViewHolder(itemView: View) : WeatherViewHolder(itemView) {
        val feedContainer: FrameLayout = itemView.findViewById(R.id.feed_container)
    }
} 