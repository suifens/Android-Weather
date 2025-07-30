package com.goodtech.tq.fragment.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.goodtech.tq.R
import com.goodtech.tq.databinding.WeatherItemCurrentBinding
import com.goodtech.tq.fragment.viewholder.HeaderItemsView
import com.goodtech.tq.helpers.BtnLinkHelper
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.utils.TimeUtils
import com.goodtech.tq.utils.WeatherUtils


/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */
@SuppressLint("ViewConstructor")
class CurrentItemView @JvmOverloads constructor(
    context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: WeatherItemCurrentBinding =
        WeatherItemCurrentBinding.inflate(LayoutInflater.from(context), this, true)
    private var mListener: WeatherHeaderListener? = null

    init {
        initData()
    }

    @SuppressLint("DefaultLocale")
    private fun initData() {
        // 打车按钮
        binding.btnHandle1.setOnClickListener {
            mListener?.onTaxi()
        }
        val taxiModel = BtnLinkHelper.getBtnLink("AD_1")
        if (taxiModel != null) {
            binding.btnHandle1.visibility = View.VISIBLE
            Glide.with(context)
                .load(taxiModel.imgPath)
                .placeholder(R.drawable.pic_dache)
                .into(binding.btnHandle1)
        } else {
            binding.btnHandle1.visibility = View.GONE
        }

        // 美团按钮
        binding.btnHandle2.setOnClickListener {
            mListener?.onMeituan()
        }
        val meituanModel = BtnLinkHelper.getBtnLink("AD_2")
        if (meituanModel != null) {
            binding.btnHandle2.visibility = View.VISIBLE
            binding.viewHandle2.visibility = View.VISIBLE
            Glide.with(context)
                .load(meituanModel.imgPath)
                .placeholder(R.drawable.pic_meituan)
                .into(binding.btnHandle2)
        } else {
            binding.btnHandle2.visibility = View.GONE
            binding.viewHandle2.visibility = View.GONE
        }

        // 饿了么按钮
        binding.btnHandle3.setOnClickListener {
            mListener?.onEleme()
        }
        val elemeModel = BtnLinkHelper.getBtnLink("AD_3")
        if (elemeModel != null) {
            binding.btnHandle3.visibility = View.VISIBLE
            binding.viewHandle3.visibility = View.VISIBLE
            Glide.with(context)
                .load(elemeModel.imgPath)
                .placeholder(R.drawable.pic_eleme)
                .into(binding.btnHandle3)
        } else {
            binding.btnHandle3.visibility = View.GONE
            binding.viewHandle3.visibility = View.GONE
        }

        // 警告按钮
        binding.warningBtn.setOnClickListener {
            mListener?.onWarningBtn()
        }

        binding.peripheryLayout.setOnClickListener {
            mListener?.onPeriphery()
        }

        setupBlurView()
    }

    private fun setupBlurView() {
        val radius = 16f
        val minBlurRadius = 4f
        val step = 4f

        //set background, if your root layout doesn't have one
//        App.instance.mainActivity?.let { activity ->
//            val windowBackground = activity.window.decorView.background
//            binding.blurView.setupWith(binding.target)
//                .setFrameClearDrawable(windowBackground)
//                .setBlurRadius(radius)
//        }
    }

    /**
     * 设置回调
     */
    fun setItemListener(listener: WeatherHeaderListener?) {
        mListener = listener
        binding.viewItems.setListener(listener)
    }

    /**
     * 数据赋值
     */
    @SuppressLint("DefaultLocale")
    fun setData(model: WeatherModel?) {
        if (model != null) {
            visibility = VISIBLE

            // 设置警告按钮可见性
            binding.warningBtn.visibility = if (model.alarmModel != null) VISIBLE else INVISIBLE

            var hadSetTemp = false
            val current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH")

            var feelsLike = 0
            var wdirCardinal = ""
            var wspd = 0
            var rh = 0
            // 处理小时数据
            for (hourly in model.hourlies) {
                if (hourly != null) {
                    val dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH")
                    if (dayHour == current) {
                        binding.imgIcon.setImageResource(WeatherUtils.weatherImageRes(hourly.icon_cd))
                        if (hourly.metric != null) {
                            wdirCardinal = hourly.wdir_cardinal
                            wspd = WeatherUtils.windGrade(hourly.metric.wspd.toFloat())
                            rh = hourly.rh
                            feelsLike = hourly.metric.feelsLike
                            binding.tvTemperature.text = String.format("%d°", hourly.metric.temp)
                            hadSetTemp = true
                        }
                        binding.tvWxPhrase.text = hourly.phraseChar
                    }
                }
            }

            // 处理观测数据
            if (model.observation != null) {
                val observation = model.observation
                val metric = observation.metric

                if (!hadSetTemp) {
                    wdirCardinal = observation.wdirCardinal
                    wspd = WeatherUtils.windGrade(metric.wspd.toFloat())
                    rh = observation.rh
                    binding.imgIcon.setImageResource(WeatherUtils.weatherImageRes(observation.wxIcon))
                    binding.tvTemperature.text = String.format("%d°", metric.temp)
                    binding.tvWxPhrase.text = observation.wxPhrase
                }
                feelsLike = metric.feelsLike
            }
            binding.tvRhWrap.text = String.format(
                "%s风 %d级｜ 湿度%d%%\n体感温度：%d°",
                wdirCardinal,
                wspd,
                rh,
                feelsLike
            )

            // 数据设置完成，整个视图已经可见
            model.lifeModel?.let { lifeModel ->
                binding.dressingView.visibility = VISIBLE
                binding.dressingView.setOnClickListener {
                    mListener?.onLifeItem(lifeModel.chuanyi)
                }
                binding.dressingTv.text = lifeModel.chuanyi?.des ?: ""
            } ?: run {
                binding.dressingView.visibility = GONE
            }
        }
    }

    /**
     * 获取HeaderItemsView
     */
    val itemsView: HeaderItemsView
        get() = binding.viewItems

    /**
     * 获取天气图标ImageView
     */
    val iconImgV = binding.imgIcon

    /**
     * 获取风向湿度TextView
     */
    val windRh = binding.tvRhWrap

    /**
     * 获取温度TextView
     */
    val tempTv = binding.tvTemperature

    /**
     * 获取天气状态TextView
     */
    val phraseTv = binding.tvWxPhrase

    /**
     * 获取提醒按钮
     */
    val warningBtn = binding.warningBtn
} 