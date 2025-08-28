package com.chunjing.tq.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import coil.load
import coil.transform.RoundedCornersTransformation
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.databinding.FragmentHomeItemBinding
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.ext.checkPermissionAgree
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.AddCityActivity
import com.chunjing.tq.ui.activity.MainActivity
import com.chunjing.tq.ui.activity.WeatherActivity
import com.chunjing.tq.ui.base.BaseVmFragment
import com.chunjing.tq.ui.fragment.vm.WeatherItemViewModel
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.utils.WeatherUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("SetTextI18n")
class HomeItemFragment : BaseVmFragment<FragmentHomeItemBinding, WeatherItemViewModel>() {

    private val PARAM_CITY_ID = "param_city_id"
    private val PARAM_POSITION = "param_position"
    private lateinit var mCityId: String
    private var mPosition = 0
    private var bgEntity: WeatherBgEntity? = null
    private var weatherBean: WeatherBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mCityId = it.getString(PARAM_CITY_ID).toString()
            mPosition = it.getInt(PARAM_POSITION)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(cityId: String, position: Int) =
            HomeItemFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_CITY_ID, cityId)
                    putInt(PARAM_POSITION, position)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadCache(mCityId)

//        Calendar.getInstance().apply {
//            val day = get(Calendar.DAY_OF_MONTH)
//            mBinding.timeTv.text = "$day/${get(Calendar.MONTH) + 1} 农历${Lunar(this)}"
//        }

        if (mCityId.isEmpty() || mCityId == "0") {
            mBinding.imgWeather.load(R.drawable.item_add) {
                transformations(RoundedCornersTransformation(SizeUtils.dp2px(20f).toFloat()))
            }
            mBinding.detailImgV.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        mCityId.let { mainViewModel.setCityId(it) }

        weatherBean?.let {
            mainViewModel.setWeather(mCityId, it)
        }

        bgEntity?.let {
            mainViewModel.setBgEntity(mCityId, it)
        }
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
    }

    override fun initEvent() {

        viewModel.weatherNow.observe(this) {
            weatherBean = it
            showWeather(it)
            viewModel.getWeatherBgEntity(it)
        }

        //  城市
        viewModel.curCity.observe(this) {
            if (it.cityId == mainViewModel.curCityId) {
                mainViewModel.setCity(it)
            }
            if (it.isLocal()) {
                mBinding.locationImgView.visibility = View.VISIBLE
                mBinding.tvCity.text = it.mergerName
                mBinding.tvCity.isSelected = true
            } else {
                mBinding.locationImgView.visibility = View.GONE
                mBinding.tvCity.text = it.cityName
            }
        }

        viewModel.curBgEntity.observe(this) {
            bgEntity = it
            mainViewModel.setBgEntity(mCityId, it)
            mBinding.imgWeather.load(it.imgPath, imageLoader) {
                placeholder(R.drawable.bg_radius20_purple)
                crossfade(true) //渐进进出
                transformations(RoundedCornersTransformation(SizeUtils.dp2px(20f).toFloat()))
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun loadData() {
        viewModel.loadData(mCityId)
    }

    override fun onStop() {
        super.onStop()
        dismissLoading()
    }

    private fun showWeather(weather: WeatherBean) {
        CoroutineScope(Dispatchers.Main).launch {
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