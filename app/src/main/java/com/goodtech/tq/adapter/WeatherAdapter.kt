package com.goodtech.tq.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.bytedance.sdk.openadsdk.TTNativeAd
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.goodtech.tq.R
import com.goodtech.tq.ad.AdManager
import com.goodtech.tq.app.App
import com.goodtech.tq.eventbus.MessageEvent
import com.goodtech.tq.fragment.view.CurrentItemView
import com.goodtech.tq.fragment.view.DailyListItemView
import com.goodtech.tq.fragment.view.HoursItemView
import com.goodtech.tq.fragment.view.LineTempItemView
import com.goodtech.tq.fragment.view.ObservationView
import com.goodtech.tq.fragment.view.RecentItemView
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.others.airQuality.view.AirLifeView
import org.greenrobot.eventbus.EventBus

private fun View.setMatchParentWidth() {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

@SuppressLint("NotifyDataSetChanged")
class WeatherAdapter : RecyclerView.Adapter<WeatherViewHolder>() {
    
    companion object {
        private const val TYPE_AD0 = 0
        private const val TYPE_CURRENT = 1
        private const val TYPE_RECENT = 2
        private const val TYPE_HOURS = 3
        private const val TYPE_AD1 = 4
        private const val TYPE_LINE_TEMP = 5
        private const val TYPE_AD2 = 6
        private const val TYPE_DAILY = 7
        private const val TYPE_LIFE = 8
        private const val TYPE_AD3 = 9
        private const val TYPE_OBSERVATION = 10
    }

    interface AdLoadCallback {
        fun onAdLoaded(position: Int)
    }

    private var weatherModel: WeatherModel? = null
    private var cityMode: CityMode? = null
    private var feedAd0: TTNativeExpressAd? = null
    private var feedAd1: TTFeedAd? = null
    private var feedAd2: TTFeedAd? = null
    private var feedAd3: TTFeedAd? = null
    private var weatherHeaderListener: WeatherHeaderListener? = null
    private var adLoadCallback: AdLoadCallback? = null

    fun setData(model: WeatherModel, mode: CityMode?) {
        weatherModel = model
        cityMode = mode
        notifyDataSetChanged()
    }

    fun setAd0(ad: TTNativeExpressAd?) {
        feedAd0 = ad
        if (ad == null) {
            notifyItemChanged(0)
        } else {
            notifyItemChanged(0)
        }
    }

    fun setAd1(ad: TTFeedAd?) {
        feedAd1 = ad
        if (ad == null) {
            notifyItemChanged(4)
        } else {
            notifyItemChanged(4)
        }
    }

    fun setAd2(ad: TTFeedAd?) {
        feedAd2 = ad
        if (ad == null) {
            notifyItemChanged(6)
        } else {
            notifyItemChanged(6)
        }
    }

    fun setAd3(ad: TTFeedAd?) {
        feedAd3 = ad
        if (ad == null) {
            notifyItemChanged(9)
        } else {
            notifyItemChanged(9)
        }
    }

    fun setWeatherHeaderListener(listener: WeatherHeaderListener) {
        weatherHeaderListener = listener
    }

    fun setAdLoadCallback(callback: AdLoadCallback) {
        adLoadCallback = callback
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_AD0
            1 -> TYPE_CURRENT
            2 -> TYPE_RECENT
            3 -> TYPE_HOURS
            4 -> TYPE_AD1
            5 -> TYPE_LINE_TEMP
            6 -> TYPE_AD2
            7 -> TYPE_DAILY
            8 -> TYPE_LIFE
            9 -> TYPE_AD3
            10 -> TYPE_OBSERVATION
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        return when (viewType) {
            TYPE_AD0, TYPE_AD1, TYPE_AD2, TYPE_AD3 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ad_container, parent, false)
                view.setMatchParentWidth()
                WeatherViewHolder.AdViewHolder(view)
            }
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
                when (position) {
                    0 -> {
                        if (feedAd0 != null) {
                            holder.feedContainer.visibility = View.VISIBLE
                            holder.feedContainer.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            showExpressAd(holder.feedContainer, feedAd0!!)
                        } else {
                            holder.feedContainer.visibility = View.GONE
                            holder.feedContainer.layoutParams.height = 0
                        }
                        adLoadCallback?.onAdLoaded(4)
                    }
                    4 -> {
                        if (feedAd1 != null) {
                            holder.feedContainer.visibility = View.VISIBLE
                            holder.feedContainer.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            showAd(holder.feedContainer, feedAd1!!)
                        } else {
                            holder.feedContainer.visibility = View.GONE
                            holder.feedContainer.layoutParams.height = 0
                        }
                        adLoadCallback?.onAdLoaded(6)
                    }
                    6 -> {
                        if (feedAd2 != null) {
                            holder.feedContainer.visibility = View.VISIBLE
                            holder.feedContainer.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            showAd(holder.feedContainer, feedAd2!!)
                        } else {
                            holder.feedContainer.visibility = View.GONE
                            holder.feedContainer.layoutParams.height = 0
                        }
                        if (!App.instance.hadInitAd) {
                            EventBus.getDefault().post(MessageEvent().needLoadIntAd(true))
                        }
                        adLoadCallback?.onAdLoaded(9)
                    }
                    9 -> {
                        if (feedAd3 != null) {
                            holder.feedContainer.visibility = View.VISIBLE
                            holder.feedContainer.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            showAd(holder.feedContainer, feedAd3!!)
                        } else {
                            holder.feedContainer.visibility = View.GONE
                            holder.feedContainer.layoutParams.height = 0
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = 11

    private fun showAd(container: FrameLayout, ad: TTFeedAd) {
        container.visibility = View.VISIBLE
        container.background = container.context.getDrawable(R.drawable.bg_round_8)
        AdManager.getInstance().showFeedAd(container.context as android.app.Activity, container, ad)
    }

    private fun showExpressAd(container: FrameLayout, ad: TTNativeExpressAd) {
        container.visibility = View.VISIBLE
        container.background = container.context.getDrawable(R.drawable.bg_round_8)
        container.removeAllViews()
        ad.setExpressInteractionListener(object : TTNativeExpressAd.ExpressAdInteractionListener {
            override fun onAdClicked(view: View?, type: Int) {
                // 广告点击回调
            }

            override fun onAdShow(view: View?, type: Int) {
                // 广告展示回调
            }

            override fun onRenderFail(view: View?, msg: String?, code: Int) {
                // 渲染失败回调
                container.visibility = View.GONE
                container.layoutParams.height = 0
            }

            override fun onRenderSuccess(view: View?, width: Float, height: Float) {
                // 渲染成功回调
                ad.expressAdView?.let { adView ->
                    // 如果广告视图已经有父视图，先移除
                    (adView.parent as? ViewGroup)?.removeView(adView)
                    container.removeAllViews()
                    container.addView(adView)
                }
            }
        })
        ad.render()
    }
} 