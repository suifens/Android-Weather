package com.goodtech.tq.modules.weather

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import androidx.annotation.RequiresApi
import com.gengee.insaitlib.ui.base.BaseFragment
import com.gengee.insaitlib.ui.base.BaseViewModel
import com.goodtech.tq.activity.BaseActivity
import com.goodtech.tq.databinding.FragmentWeatherBinding
import com.goodtech.tq.helpers.BtnLinkHelper
import com.goodtech.tq.listener.WeatherHeaderListener
import com.goodtech.tq.models.CityMode
import com.goodtech.tq.models.WeatherModel
import com.goodtech.tq.modules.others.airQuality.AirQualityActivity
import com.goodtech.tq.modules.others.calendar.CalendarActivity
import com.goodtech.tq.modules.others.constellation.ConstellationActivity
import com.goodtech.tq.modules.others.taifeng.TyphoonActivity
import com.goodtech.tq.modules.others.test.MyTestActivity
import com.goodtech.tq.modules.signing.SigningActivity
import com.goodtech.tq.utils.SpUtils
import com.goodtech.tq.views.popup.AlarmPopup
import com.lxj.xpopup.XPopup

class WeatherFragment: BaseFragment<BaseViewModel, FragmentWeatherBinding>() {

    private val dataAdapter by lazy {
        WeatherAdapter(requireContext(), headerListener)
    }

    private var mWeatherModel: WeatherModel? = null
    private var mCityMode: CityMode? = null
    private var mStateBarBg: View? = null
    private var mHadLoad = false
    private var totalDy = 0

    fun setStateBar(stateBar: View) {
        this.mStateBarBg = stateBar
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView(savedInstanceState: Bundle?) {
        mViewBind.recyclerView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (mStateBarBg != null) {
                if (scrollY <= mStateBarBg!!.height && scrollY > 10) {
                    val alpha: Float = totalDy / (mStateBarBg!!.height * 1.0).toFloat()
                    mStateBarBg!!.alpha = alpha
                } else if (scrollY > mStateBarBg!!.height) {
                    mStateBarBg!!.alpha = 1f
                } else {
                    mStateBarBg!!.alpha = 0f
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            configRecyclerView()
            isFirstLoad = false
            mHadLoad = true
        }
    }

    fun changeWeather(model: WeatherModel?, cityMode: CityMode?) {
        mWeatherModel = model
        mCityMode = cityMode
        if (mHadLoad) {
            dataAdapter.changeWeather(model, cityMode)
        }
    }

    private fun configRecyclerView() {
        mViewBind.recyclerView.apply {
            adapter = dataAdapter
        }
        if (mWeatherModel != null && mCityMode != null) {
            dataAdapter.changeWeather(mWeatherModel, mCityMode)
        }
    }

    /// 顶部回调
    private val headerListener = object : WeatherHeaderListener {
        override fun onTyphoon() {
            if (activity != null) {
                val intent = Intent(activity, TyphoonActivity::class.java)
                activity!!.startActivity(intent)
            }
        }

        override fun onAirQuality() {
            if (activity != null && mWeatherModel != null) {
                AirQualityActivity.redirectTo(activity, mCityMode, mWeatherModel!!.aqi)
            }
        }

        override fun onCalendar() {
            if (activity != null) {
                val intent = Intent(activity, CalendarActivity::class.java)
                activity!!.startActivity(intent)
            }
        }

        override fun onFortune() {
            if (activity != null) {
                val intent = Intent(
                    activity,
                    ConstellationActivity::class.java
                )
                activity!!.startActivity(intent)
            }
        }

        override fun onSignIn() {
            if (activity != null) {
                if (!SpUtils.getInstance().isAgreePermission) {
                    (requireActivity() as BaseActivity).showPermissionDialog(
                        requireActivity()
                    ) { _: View? ->
                        SigningActivity.redirectTo(
                            activity,
                            mWeatherModel!!.hourlies[0],
                            mCityMode,
                            0
                        )
                    }
                    return
                }
                SigningActivity.redirectTo(activity, mWeatherModel!!.hourlies[0], mCityMode, 0)
            }
        }

        override fun onTaxi() {
            var link = "https://kzurl10.cn/Z9Nks"
            var title = "免费打车券"
            val model = BtnLinkHelper.getBtnLink(0)
            if (model != null) {
                link = model.h5link
                title = model.tempType
            }
            MyTestActivity.redirectTo(
                requireActivity(),
                link,
                title,
                "DaChe"
            )
        }

        override fun onMeituan() {
            var link = "https://kurl04.cn/ZRSxc"
            var title = "美团大额券"
            val model = BtnLinkHelper.getBtnLink(1)
            if (model != null) {
                link = model.h5link
                title = model.tempType
            }
            MyTestActivity.redirectTo(
                requireActivity(),
                link,
                title,
                "MeiTuan"
            )
        }

        override fun onEleme() {
            var link = "https://kzurl05.cn/ZRJjc"
            var title = "饿了么大红包"
            val model = BtnLinkHelper.getBtnLink(2)
            if (model != null) {
                link = model.h5link
                title = model.tempType
            }
            MyTestActivity.redirectTo(
                requireActivity(),
                link,
                title,
                "Eleme"
            )
        }

        override fun onWarningBtn() {
            if (mWeatherModel != null && mWeatherModel!!.alarmModel != null) {
                val popup = AlarmPopup(requireActivity())
                popup.setupData(mWeatherModel!!.alarmModel)
                XPopup.Builder(requireActivity())
                    .isDestroyOnDismiss(true)
                    .asCustom(popup)
                    .show()
            }
        }
    }


}