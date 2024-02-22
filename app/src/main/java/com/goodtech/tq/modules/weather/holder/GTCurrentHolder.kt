package com.goodtech.tq.modules.weather.holder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gengee.insaitlib.ext.clickNoRepeat
import com.goodtech.tq.R
import com.goodtech.tq.databinding.WeatherItemCurrentBinding
import com.goodtech.tq.helpers.BtnLinkHelper
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.ImageUtils
import com.goodtech.tq.utils.TimeUtils
import com.goodtech.tq.utils.WeatherUtils

class GTCurrentHolder(val binding: WeatherItemCurrentBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun getBinding(context: Context, parent: ViewGroup): WeatherItemCurrentBinding {
            return WeatherItemCurrentBinding.inflate(LayoutInflater.from(context), parent, false)
        }
    }

    private var mListener: WeatherHeaderListener? = null

    /**
     * 设置回调
     */
    fun setItemListener(listener: WeatherHeaderListener?) {
        mListener = listener
        if (listener != null) {
            binding.viewItems.setListener(listener)
        }
    }

    init {
        /// 打车
        binding.btnHandle1.clickNoRepeat { mListener?.onTaxi() }
        val taxiModel = BtnLinkHelper.getBtnLink("AD_1")
        if (taxiModel != null) {
            binding.btnHandle1.visibility = View.VISIBLE
            binding.btnHandle1.load(taxiModel.imgPath) {
                placeholder(R.drawable.pic_dache)
            }
        } else {
            binding.btnHandle1.visibility = View.GONE
        }

        binding.btnHandle2.clickNoRepeat { mListener?.onMeituan() }
        val meituanModel = BtnLinkHelper.getBtnLink("AD_2")
        if (meituanModel != null) {
            binding.btnHandle2.visibility = View.VISIBLE
            binding.viewHandle2.visibility = View.VISIBLE
            binding.btnHandle2.load(meituanModel.imgPath) {
                placeholder(R.drawable.pic_meituan)
            }
        } else {
            binding.btnHandle2.visibility = View.GONE
            binding.viewHandle2.visibility = View.GONE
        }

        binding.btnHandle3.clickNoRepeat { mListener?.onEleme() }
        val elemeModel = BtnLinkHelper.getBtnLink("AD_3")
        if (elemeModel != null) {
            binding.btnHandle3.visibility = View.VISIBLE
            binding.viewHandle3.visibility = View.VISIBLE
            binding.btnHandle3.load(elemeModel.imgPath) {
                placeholder(R.drawable.pic_eleme)
            }
        } else {
            binding.btnHandle3.visibility = View.GONE
            binding.viewHandle3.visibility = View.GONE
        }

        binding.warningBtn.clickNoRepeat { mListener?.onWarningBtn() }
    }

    @SuppressLint("DefaultLocale")
    fun setData(model: WeatherModel?) {
        if (model != null) {
            binding.root.visibility = View.VISIBLE

            binding.warningBtn.visibility =
                if (model.alarmModel != null) View.VISIBLE else View.INVISIBLE
            var hadSetTemp = false
            val current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH")
            for (hourly in model.hourlies) {
                if (hourly != null) {
                    val dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH")
                    if (dayHour == current) {
                        binding.imgIcon.setImageResource(ImageUtils.weatherImageRes(hourly.icon_cd))
                        if (hourly.metric != null) {
                            binding.tvRhWrap.text = String.format(
                                "%s风 %d级｜ 湿度%d%%", hourly.wdir_cardinal,
                                WeatherUtils.windGrade(hourly.metric.wspd.toFloat()), hourly.rh
                            )
                            binding.tvTemperature.text = String.format("%d°", hourly.metric.temp)
                            hadSetTemp = true
                        }
                        binding.tvWxPhrase.text = hourly.getPhraseChar()
                    }
                }
            }
            if (model.observation != null) {
                val observation = model.observation
                val metric = observation.metric
                if (!hadSetTemp) {
                    binding.tvRhWrap.text = String.format(
                        "%s风 %d级｜ 湿度%d%%", observation.wdirCardinal,
                        WeatherUtils.windGrade(metric.wspd.toFloat()), observation.rh
                    )
                    binding.imgIcon.setImageResource(ImageUtils.weatherImageRes(observation.wxIcon))
                    binding.tvTemperature.text = String.format("%d°", metric.temp)
                    binding.tvWxPhrase.text = observation.getWxPhrase()
                }
            }

            binding.layoutData.visibility = View.VISIBLE
        }
    }

}