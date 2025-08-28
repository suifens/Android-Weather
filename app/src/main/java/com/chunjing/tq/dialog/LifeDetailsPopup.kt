package com.chunjing.tq.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.chunjing.tq.R
import com.chunjing.tq.bean.LifeItemBean
import com.chunjing.tq.bean.Observation
import com.chunjing.tq.ui.activity.vm.LifeViewModel
import com.goodtech.weatherlib.utils.WeatherUtils
import com.lxj.xpopup.core.BottomPopupView

/**
 * com.chunjing.tq.dialog
 */
@SuppressLint("SetTextI18n")
class LifeDetailsPopup(context: Context) : BottomPopupView(context) {
    
    private lateinit var lifeImageView: ImageView
    private lateinit var lifeTitleTv: TextView
    private lateinit var lifeValueTv: TextView
    private lateinit var weatherIconImgV: ImageView
    private lateinit var weatherInfoTv: TextView
    private lateinit var weatherTempTv: TextView
    private lateinit var cityTv: TextView
    private lateinit var desTv: TextView

    var lifeDetails: LifeItemBean? = null
    var lifeTitle: String = ""
    var lifeImgRes: Int = 0
    var observation: Observation? = null
    var cityName: String = ""

    override fun getImplLayoutId(): Int = R.layout.dialog_life_details

    override fun onCreate() {
        super.onCreate()
        lifeImageView = findViewById(R.id.lifeImageV)
        lifeTitleTv = findViewById(R.id.tv_title)
        lifeValueTv = findViewById(R.id.tv_value)
        weatherIconImgV = findViewById(R.id.weatherIcon)
        weatherInfoTv = findViewById(R.id.weatherInfoTv)
        weatherTempTv = findViewById(R.id.weatherTempTv)
        cityTv = findViewById(R.id.cityTv)
        desTv = findViewById(R.id.desTv)

        findViewById<ImageButton>(R.id.closeBtn).setOnClickListener {
            dismiss()
        }

        //  数据配置
        if (cityName.isNotEmpty()) {
            cityTv.text = cityName
        }
        if (lifeImgRes != 0) lifeImageView.load(lifeImgRes)
        if (lifeTitle.isNotEmpty()) lifeTitleTv.text = lifeTitle
        if (lifeDetails != null) {
            lifeValueTv.text = lifeDetails!!.v
            desTv.text = lifeDetails!!.des
        }
        if (observation != null) {
            weatherIconImgV.load(WeatherUtils.getIcon(observation!!.wxIcon))
            weatherTempTv.text = "${observation!!.metric.minTemp}/${observation!!.metric.maxTemp}°"
            weatherInfoTv.text = observation!!.getWxcPhrase()
        }
    }
}
