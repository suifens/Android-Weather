package com.chunjing.tq.dialog

import android.content.Context
import android.content.ContextParams
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.chunjing.tq.R
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.SearchCityActivity
import com.chunjing.tq.ui.activity.TravelDetailActivity
import com.lxj.xpopup.core.BottomPopupView

/**
 * com.chunjing.tq.dialog
 * 出行天气弹窗
 */
class TravelPopup(context: Context) : BottomPopupView(context) {

    private lateinit var currentCity: CityEntity
    private var travelCity: CityEntity? = null

    private lateinit var travelNameTv: TextView

    override fun getImplLayoutId(): Int = R.layout.dialog_travel

    override fun onCreate() {
        super.onCreate()

        val timeTv = findViewById<TextView>(R.id.timeTv)
        timeTv.text = TimeUtils.millis2String(System.currentTimeMillis(), "MM月dd日")

        currentCity.let {
            val cityNameTv = findViewById<TextView>(R.id.curCityTv)
            cityNameTv.text = it.cityName
        }

        travelNameTv = findViewById(R.id.travelCityTv)
        travelNameTv.setOnClickListener {
            SearchCityActivity.startActivity(context, false)
        }

        findViewById<Button>(R.id.showDetailBtn).setOnClickListener {
            if (travelCity == null) {
                ToastUtils.showShort("请选择出行城市")
                return@setOnClickListener
            }

            mainViewModel.fetchWeather(travelCity!!) {
                it?.let {
                    TravelDetailActivity.startActivity(context, currentCity.cityId, travelCity!!.cityId)
                }
            }
        }
        //  close
        findViewById<ImageButton>(R.id.closeBtn).setOnClickListener {
            dismiss()
        }
    }

    //  配置城市
    fun setupCity(city: CityEntity) {
        currentCity = city
    }

    fun setupTravelCity(city: CityEntity) {
        travelCity = city
        travelNameTv.setTextColor(ContextCompat.getColor(context, R.color.black))
        travelNameTv.text = travelCity!!.cityName
    }

}