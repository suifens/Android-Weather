package com.chunjing.tq.ui.fragment

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import coil.load
import coil.transform.RoundedCornersTransformation
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.databinding.FragmentHomeItemBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.ext.checkPermissionAgree
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.AddCityActivity
import com.chunjing.tq.ui.activity.MainActivity
import com.chunjing.tq.ui.base.BaseFragment
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.utils.WeatherUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("SetTextI18n")
class WeatherItemFragment : BaseFragment<FragmentHomeItemBinding>() {

    private var mCityId: String = ""
    private var mCity: CityEntity? = null
    private var weatherBean: WeatherBean? = null
    private var bgEntity: WeatherBgEntity? = null
    private var mPosition = 0
    private var needUpdate: Boolean = true

    override fun onStart() {
        super.onStart()
        if (mCityId.isEmpty() || mCityId == "0") {
            mBinding.imgWeather.load(R.drawable.item_add) {
                transformations(RoundedCornersTransformation(SizeUtils.dp2px(20f).toFloat()))
            }
            mBinding.detailImgV.visibility = View.GONE
        }
    }

    fun setupWeather(city: CityEntity, weather: WeatherBean?, position: Int = 0) {
        this.mCity = city
        this.mCityId = city.cityId
        this.weatherBean = weather
        this.mPosition = position
        this.needUpdate = true
    }

    override fun bindView() = FragmentHomeItemBinding.inflate(layoutInflater)

    override fun initView(view: View?) {

        mBinding.containerView.setOnClickListener {
            requireActivity().checkPermissionAgree {
                showLoading(true)
                if (mPosition == 0) {
                    requireActivity().startActivity<AddCityActivity>()
                } else {
                    mainViewModel.setCityId(mCityId)
                    //  跳转到相应的城市
                    MainActivity.startActivity(requireActivity(), mPosition - 1)
                }
            }
        }

        updateData()
        needUpdate = false
    }

    override fun initEvent() {
    }

    override fun onResume() {
        super.onResume()
        bgEntity?.let {
            mBinding.imgWeather.load(it.imgPath, imageLoader) {
                transformations(
                    RoundedCornersTransformation(
                        SizeUtils.dp2px(10f).toFloat()
                    )
                )
            }
        }
        Log.e("TAG", "onResume: $this" )
        if (needUpdate) {
            updateData()
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun loadData() {

    }

    private fun updateData() {
        mCity?.let {
            showCity(it)
        }

        weatherBean?.let { weather ->
            showWeather(weather)
            mainViewModel.getWeatherBg(weather) {
                bgEntity = it
                if (it != null) {
                    mBinding.imgWeather.load(it.imgPath, imageLoader) {
                        transformations(
                            RoundedCornersTransformation(
                                SizeUtils.dp2px(10f).toFloat()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun showCity(city: CityEntity) {
        if (city.isLocal()) {
            mBinding.locationImgView.visibility = View.VISIBLE
            mBinding.tvCity.text = city.mergerName
            mBinding.tvCity.isSelected = true
        } else {
            mBinding.locationImgView.visibility = View.GONE
            mBinding.tvCity.text = city.cityName
        }
    }

    private fun showWeather(weather: WeatherBean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val observation = weather.observation
            mBinding.tempTv.text = "${observation.metric.temp}°"
            mBinding.iconImgV.setImageResource(WeatherUtils.getIcon(observation.wxIcon))
            mBinding.phraseTv.text = observation.getWxcPhrase()
            mBinding.weatherDetailTv.text = "${observation.wdirCardinal}风" +
                    "${WeatherUtils.windGrade(observation.metric.wspd)}级\n湿度${observation.rh}%"

            //  是否降雨
            var showRain = false
            if (weather.hourlies.size > 2) {
                val firstHour = weather.hourlies[0]
                val secondHour = weather.hourlies[1]
                if (firstHour.getPhrasesChar().contains("雨")
                    || firstHour.getPhrasesChar().contains("雪")
                    || firstHour.getPhrasesChar().contains("阴")
                    || secondHour.getPhrasesChar().contains("雨")
                    || secondHour.getPhrasesChar().contains("雪")
                    || secondHour.getPhrasesChar().contains("阴")) {
                    showRain = true
                }
                mBinding.rainTipTv.visibility = if (showRain) View.VISIBLE else View.INVISIBLE
            }
        }
    }
}