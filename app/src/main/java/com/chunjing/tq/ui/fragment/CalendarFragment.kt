package com.chunjing.tq.ui.fragment

import android.annotation.SuppressLint
import android.graphics.*
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.FileUtils
import com.chunjing.tq.R
import com.chunjing.tq.calendarVM
import com.chunjing.tq.databinding.FragmentCalendarBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.ext.shotView
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.CalendarActivity
import com.chunjing.tq.ui.activity.CalendarShareActivity
import com.chunjing.tq.ui.base.BaseVmFragment
import com.chunjing.tq.ui.fragment.vm.WeatherViewModel
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.utils.WeatherUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n")
open class CalendarFragment : BaseVmFragment<FragmentCalendarBinding, WeatherViewModel>() {

    private var mCity: CityEntity? = null
    private var mShareImgPath: String? = null

    override fun onResume() {
        super.onResume()
        dismissLoading()
        viewModel.loadLocationCache()
        //  删除分享图片
        if (mShareImgPath != null && mShareImgPath!!.isNotEmpty()) {
            FileUtils.delete(mShareImgPath).let { success ->
                if (success) {
                    mShareImgPath = null
                }
            }
        }

        changeShareView(false)
    }

    override fun bindView() = FragmentCalendarBinding.inflate(layoutInflater)

    override fun initView(view: View?) {

        val bars = ConstraintLayout.LayoutParams(mBinding.privateStationBar.layoutParams)
        bars.height = bars.height + BarUtils.getStatusBarHeight()
        mBinding.privateStationBar.layoutParams = bars

        mBinding.btnOpenCalendar.setOnClickListener {
            startActivity<CalendarActivity>()
        }

        mBinding.shareLayout.setOnClickListener {
            startActivity<CalendarActivity>()
        }

        mBinding.btnShare.setOnClickListener {
            shareBtnPressed()
        }
    }

    override fun initEvent() {
        calendarVM.dayDetail.observe(this) {
            mBinding.tvTime.text = "农历${it.lunar} • ${it.weekday}"
        }

        //  背景图片
        viewModel.curBgEntity.observe(this) {
            if (viewModel.calendarBgPath.value != null)
                return@observe

            val originDrawable = mBinding.weatherBgImgV.drawable
            mBinding.weatherBgImgV.load(it.imgPath, imageLoader) {
                if (originDrawable == null) {
                    placeholder(R.drawable.img_bg)
                } else {
                    placeholder(originDrawable)
                }
            }
        }

        viewModel.calendarBgPath.observe(this) {
            if (it != null) {
                val originDrawable = mBinding.weatherBgImgV.drawable
                mBinding.weatherBgImgV.load(it, imageLoader) {
                    if (originDrawable == null) {
                        placeholder(R.drawable.img_bg)
                    } else {
                        placeholder(originDrawable)
                    }
                }
            }
        }

        viewModel.weatherNow.observe(this) {
            //  获取背景
            viewModel.getWeatherBgEntity(it, LOCATION_ID, true)

            mBinding.imgWeather.load(WeatherUtils.getIcon(it.getIconCd()))
            mBinding.tvWeather.text = "${it.getCurrentTemp()}°"
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun loadData() {
        val calendar = Calendar.getInstance()
        mBinding.tvDay.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
        mBinding.tvMonth.text = "${calendar.get(Calendar.MONTH) + 1}月"

        val sdf = SimpleDateFormat("yyyy-M-d")
        calendarVM.getDayDetails(sdf.format(calendar.time))
        //  获取日历背景
        viewModel.getCalendarBgEntity()

        mainViewModel.curLocation.value?.let {
            mCity = it
            mBinding.cityNameTv.text = it.mergerName
        }
        viewModel.loadLocationCache()
    }

    /**
     * 分享按钮点击
     */
    private fun shareBtnPressed() {

        val bgEntity = viewModel.curBgEntity.value
        CalendarShareActivity.startActivity(requireActivity(),
            bgEntity?.imgPath)

//        val permission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
//            Manifest.permission.READ_MEDIA_IMAGES
//        else Manifest.permission.READ_EXTERNAL_STORAGE
//
//        PermissionUtils.permission(
//            permission, Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ).callback(object : PermissionUtils.SimpleCallback {
//            override fun onGranted() {
//                showLoading(true)
//                getScreenShotBitmap() {
//                    mShareImgPath = ImageTools.saveImageToStorages(requireContext(), saveBitmap)
//                    mShareImgPath?.let { path ->
//                        requireActivity().shareToMore(path)
//                    }
//                }
//            }
//            override fun onDenied() {
//            }
//        }).request()
    }

    private var saveBitmap: Bitmap? = null
    private fun getScreenShotBitmap(action: (agree: Boolean) -> Unit = {}) {
        if (saveBitmap != null && !saveBitmap!!.isRecycled) {
//            saveBitmap!!.recycle()
            saveBitmap = null
        }
        CoroutineScope(Dispatchers.Main).launch {
            changeShareView(true)
            delay(500L)
            requireActivity().shotView(mBinding.shareLayout, requireActivity().window) {
                saveBitmap = it
                changeShareView(false)
                action.invoke(true)
            }
        }
    }

    private fun changeShareView(show: Boolean) {
        if (show) {
            if (mCity != null) {
                mBinding.ivLoc.visibility = VISIBLE
                mBinding.cityNameTv.visibility = VISIBLE
            }
            mBinding.ivShareTop.visibility = VISIBLE
            mBinding.qrCodeImgV.visibility = VISIBLE
            mBinding.btnOpenCalendar.visibility = INVISIBLE
        } else {
            mBinding.ivLoc.visibility = INVISIBLE
            mBinding.cityNameTv.visibility = INVISIBLE
            mBinding.ivShareTop.visibility = INVISIBLE
            mBinding.qrCodeImgV.visibility = INVISIBLE
            mBinding.btnOpenCalendar.visibility = VISIBLE
        }
    }

}