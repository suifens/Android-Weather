package com.goodtech.tq.views.popup

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import coil3.load
import com.goodtech.tq.R
import com.goodtech.tq.databinding.DialogLifeDetailsBinding
import com.goodtech.tq.models.LifeItemBean
import com.goodtech.tq.models.Observation
import com.goodtech.tq.utils.WeatherUtils
import com.lxj.xpopup.core.BottomPopupView

/**
 * com.chunjing.tq.dialog
 */
@SuppressLint("SetTextI18n")
class LifeDetailsPopup(context: Context) : BottomPopupView(context) {
    
    private lateinit var binding: DialogLifeDetailsBinding

    var lifeDetails: LifeItemBean? = null
    var lifeTitle: String = ""
    var lifeImgRes: Int = 0
    var observation: Observation? = null
    var cityName: String = ""

    override fun getImplLayoutId(): Int = R.layout.dialog_life_details

    override fun onCreate() {
        super.onCreate()
        binding = DialogLifeDetailsBinding.bind(popupImplView)
        
        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        //  数据配置
        if (cityName.isNotEmpty()) {
            binding.cityTv.text = cityName
        }
        if (lifeImgRes != 0) binding.lifeImageV.load(lifeImgRes)
        if (lifeTitle.isNotEmpty()) binding.tvTitle.text = lifeTitle
        if (lifeDetails != null) {
            binding.tvValue.text = lifeDetails!!.v
            binding.desTv.text = lifeDetails!!.des
        }
        if (observation != null) {
            binding.weatherIcon.load(WeatherUtils.weatherImageRes(observation!!.wxIcon))
            binding.weatherTempTv.text = "${observation!!.metric.minTemp}/${observation!!.metric.maxTemp}°"
            binding.weatherInfoTv.text = observation!!.wxPhrase
        }
    }
}
