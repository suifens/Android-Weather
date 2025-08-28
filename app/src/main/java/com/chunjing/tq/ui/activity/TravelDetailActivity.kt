package com.chunjing.tq.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.adapter.TravelDetailAdapter
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.databinding.ActivityTravelDetailBinding
import com.chunjing.tq.databinding.ItemTravelWeatherBinding
import com.chunjing.tq.databinding.LayoutCurrentBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.vm.TravelDetailViewModel
import com.chunjing.tq.ui.base.BaseVmActivity
import com.goodtech.weatherlib.net.LoadState
import com.goodtech.weatherlib.utils.DateUtil
import com.goodtech.weatherlib.utils.WeatherUtils

class TravelDetailActivity : BaseVmActivity<ActivityTravelDetailBinding, TravelDetailViewModel>() {

    companion object {
        fun startActivity(context: Context, curCity: String, travelCity: String) {
            val intent = Intent(context, TravelDetailActivity::class.java)
            intent.putExtra("currentCity", curCity)
            intent.putExtra("travelCity", travelCity)
            context.startActivity(intent)
        }
    }

    private lateinit var curCityId: String
    private lateinit var travelCityId: String
    private lateinit var curCity: CityEntity
    private lateinit var travelCity: CityEntity
    private lateinit var mCurrentBinding: ItemTravelWeatherBinding
    private lateinit var mTravelBinding: ItemTravelWeatherBinding
    private var currentWeather: WeatherBean? = null
    private var travelWeather: WeatherBean? = null

    private var mAdapter: TravelDetailAdapter? = null

    override fun bindView() = ActivityTravelDetailBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
        intent?.let {
            curCityId = it.getStringExtra("currentCity")?:""
            travelCityId = it.getStringExtra("travelCity")?:""
        }
    }

    override fun initView() {

        configStationBar(mBinding.privateStationBar)
        mBinding.backButton.setOnClickListener { finish() }

        mCurrentBinding = mBinding.curCityView
        mTravelBinding = mBinding.travelCityView

        mAdapter = TravelDetailAdapter(System.currentTimeMillis()) {
            showCurrentWeather(it)
            showTravelWeather(it)
        }
        mBinding.recyclerView.adapter = mAdapter
        mBinding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            @Deprecated("Deprecated in Java")
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                if (itemPosition == 0) {
                    outRect.left = SizeUtils.dp2px(20f)
                }
                outRect.right = SizeUtils.dp2px(20f)
            }
        })

    }

    private fun showCurrentWeather(position: Int) {
        currentWeather?.let {
            val dayList = it.dailies
            if (dayList.size > position) {
                val daily = dayList[position]
                //  温度
                val metric = daily.metric
                if (metric != null) {
                    mCurrentBinding.tempTv.text = "${metric.maxTemp}/${metric.minTemp}°"
                }

                val weatherPart = daily.weatherPart
                if (weatherPart != null) {
                    //  风、温度
                    val detail = "${weatherPart.wdirCardinal}风${WeatherUtils.windGrade(weatherPart.wspd)}级" +
                            " | 湿度${weatherPart.rh}%"
                    mCurrentBinding.tvDesc.text = detail
                    //  图标
                    mCurrentBinding.iconImgV.load(WeatherUtils.getIcon(weatherPart.iconCd)) {
                        transformations(RoundedCornersTransformation(SizeUtils.dp2px(16f).toFloat()))
                    }
                    //  天气
                    mCurrentBinding.phraseTv.text = weatherPart.getPhrasesChar()
                }
            }
        }
    }

    private fun showTravelWeather(position: Int) {
        travelWeather?.let {
            val dayList = it.dailies
            if (dayList.size > position) {
                val daily = dayList[position]
                //  温度
                val metric = daily.metric
                if (metric != null) {
                    mTravelBinding.tempTv.text = "${metric.maxTemp}/${metric.minTemp}°"
                }

                val weatherPart = daily.weatherPart
                if (weatherPart != null) {
                    //  风、温度
                    val detail = "${weatherPart.wdirCardinal}风${WeatherUtils.windGrade(weatherPart.wspd)}级" +
                            " | 湿度${weatherPart.rh}%"
                    mTravelBinding.tvDesc.text = detail
                    //  图标
                    mTravelBinding.iconImgV.load(WeatherUtils.getIcon(weatherPart.iconCd)) {
                        transformations(RoundedCornersTransformation(SizeUtils.dp2px(16f).toFloat()))
                    }
                    //  天气
                    mTravelBinding.phraseTv.text = weatherPart.getPhrasesChar()
                }
            }
        }
    }

    override fun initEvent() {

        viewModel.currentCity.observe(this) {
            mBinding.curCityTv.text = it.cityName
            mCurrentBinding.cityNameTv.text = it.cityName
        }

        viewModel.currentWeather.observe(this) {
            currentWeather = it
            val dayList = it.dailies
            val firstDay = dayList[0]
            mAdapter?.let { adapter ->
                adapter.firstTime = DateUtil.switchTime(firstDay.fcst_valid_local)
                adapter.dayCount = dayList.size
                adapter.notifyDataSetChanged()
            }
            showCurrentWeather(0)
        }

        viewModel.travelCity.observe(this) {
            mBinding.travelCityTv.text = it.cityName
            mTravelBinding.cityNameTv.text = it.cityName
        }

        viewModel.travelWeather.observe(this) {
            travelWeather = it
            showTravelWeather(0)
        }

        viewModel.loadState.observe(this) {
            when (it) {
                is LoadState.Start -> {
                    showLoading()
                }
                is LoadState.Error -> {

                }
                is LoadState.Finish -> {
                    dismissLoading()
                }
            }
        }
    }

    override fun initData() {
        if (curCityId.isNotEmpty() && travelCityId.isNotEmpty()) {
            viewModel.setupCities(curCityId, travelCityId)
        }
    }

}