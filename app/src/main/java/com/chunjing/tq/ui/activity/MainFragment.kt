package com.chunjing.tq.ui.activity

import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.adapter.WeatherPagerAdapter
import com.chunjing.tq.databinding.FragmentMainBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.LOCATION_ID
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.imageLoader
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.modules.removeAd.RemoveAdActivity
import com.chunjing.tq.ui.activity.vm.MainViewModel
import com.chunjing.tq.ui.base.BaseVmFragment
import com.goodtech.weatherlib.ext.clickNoRepeat
import com.goodtech.weatherlib.extension.startActivity

class MainFragment : BaseVmFragment<FragmentMainBinding, MainViewModel>() {

    private val mCityList = ArrayList<CityEntity>()
    private var mCurIndex = 0
    private var isNewCity = false
    private var weatherPagerAdapter: WeatherPagerAdapter? = null
    //  是否在 Start和Pause之间
    private var isShowing: Boolean = true

    override fun bindView() = FragmentMainBinding.inflate(layoutInflater)
    
    override fun initView(view: View?) {

        val bars = ConstraintLayout.LayoutParams(mBinding.privateStationBar.layoutParams)
        bars.height += BarUtils.getStatusBarHeight()
        mBinding.privateStationBar.layoutParams = bars

        mBinding.cityNameTv.isSelected = true
        mBinding.listImgView.setOnClickListener {
            startActivity<CityListActivity>()
        }
        mBinding.btnAdd.setOnClickListener { startActivity<AddCityActivity>() }

        weatherPagerAdapter = WeatherPagerAdapter(this)
        mBinding.viewPager.adapter = weatherPagerAdapter
        mBinding.viewPager.offscreenPageLimit = 2

        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(i: Int) {
                bindCurrentCityTitle(mCityList.getOrNull(i))
                mBinding.llRound.getChildAt(mCurIndex)?.isEnabled = false
                mBinding.llRound.getChildAt(i)?.isEnabled = true
                mCurIndex = i

                if (mCityList.size > i) {
                    mainViewModel.setCity(mCityList[i])
                    mainViewModel.updateWeatherRefreshWindow(mCityList, i)
                }
            }
        })
    }

    override fun initEvent() {
        mainViewModel.cities.observe(this) {
            if (it.isEmpty()) {
                startActivity<AddCityActivity>()
            } else {
                configCities(it)
            }
            dismissLoading()
        }

        mainViewModel.curBgEntity.observe(this) {
            mBgEntity = it
            if (isShowing) {
                showBg(it)
            }
        }

        mainViewModel.showIndex.observe(this) { idx ->
            if (idx == 1000) {
                isNewCity = true
            } else {
                isNewCity = false
                val last = (mCityList.size - 1).coerceAtLeast(0)
                mCurIndex = idx.coerceIn(0, last)
                if (mCityList.isNotEmpty() && mBinding.llRound.childCount == mCityList.size) {
                    for (i in 0 until mBinding.llRound.childCount) {
                        mBinding.llRound.getChildAt(i).isEnabled = mCurIndex == i
                    }
                    mBinding.viewPager.adapter?.let { ad ->
                        if (ad.itemCount > mCurIndex) {
                            mBinding.viewPager.setCurrentItem(mCurIndex, false)
                        }
                    }
                }
            }
        }

        mainViewModel.curLocation.observe(this) { city ->
            if (!city.isLocal()) return@observe
            if (city.latitude.isBlank() || city.longitude.isBlank()) return@observe

            val locIndex = mCityList.indexOfFirst { it.cityId == LOCATION_ID }
            if (locIndex >= 0) {
                mCityList[locIndex] = city
            }
            if (locIndex >= 0 && mCurIndex == locIndex) {
                bindCurrentCityTitle(city)
            }
        }

        mBinding.fabRemoveAd.clickNoRepeat {
            RemoveAdActivity.startActivity(requireActivity())
        }
    }

    override fun loadData() {
        mainViewModel.getCitiesCache()
        mainViewModel.curBgEntity.value?.let {
            showBg(it)
        }
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
        (mBgEntity ?: mainViewModel.curBgEntity.value)?.let { showBg(it) }
        if (mCityList.isNotEmpty()) {
            for (i in mCityList.indices) {
                mBinding.llRound.getChildAt(i)?.isEnabled = mCurIndex == i
            }
            mBinding.viewPager.setCurrentItem(mCurIndex, true)
            mainViewModel.updateWeatherRefreshWindow(mCityList, mCurIndex)
        }
    }

    override fun onPause() {
        isShowing = false
        isNewCity = false
        super.onPause()
    }

    private var mBgEntity: WeatherBgEntity? = null
    private fun showBg(bgEntity: WeatherBgEntity) {
        mBgEntity = bgEntity
        showWeatherImg(bgEntity)
    }

    private fun showWeatherImg(bgEntity: WeatherBgEntity) {
        if (bgEntity.imgPath.isBlank()) return
        mBinding.weatherImgV.visibility = View.VISIBLE
        val originDrawable = mBinding.weatherImgV.drawable
        mBinding.weatherImgV.load(bgEntity.imgPath, imageLoader) {
            if (originDrawable == null) {
                placeholder(R.drawable.gradient_weather_main)
            } else {
                placeholder(originDrawable)
            }
        }
    }

    private fun configCities(cityList: List<CityEntity>) {
        val previousTabIds = weatherPagerAdapter?.currentCityIds().orEmpty()

        mCityList.clear()
        mCityList.addAll(cityList)

        val pendingTabCityId = mainViewModel.consumePendingSelectCityTab()
        if (!pendingTabCityId.isNullOrBlank()) {
            val idx = cityList.indexOfFirst { it.cityId == pendingTabCityId }
            mCurIndex = if (idx >= 0) idx else cityList.lastIndex.coerceAtLeast(0)
            isNewCity = false
        } else if (mCurIndex >= cityList.size || isNewCity) {
            mCurIndex = (cityList.size - 1).coerceAtLeast(0)
            isNewCity = false
        }

        if (cityList.isNotEmpty()) {
            bindCurrentCityTitle(cityList[mCurIndex])
        }

        val nextTabIds = cityList.map { it.cityId }
        val adapter = weatherPagerAdapter
        val canAppendOneTab = previousTabIds.isNotEmpty() &&
            nextTabIds.size == previousTabIds.size + 1 &&
            nextTabIds.take(previousTabIds.size) == previousTabIds

        if (canAppendOneTab && adapter != null) {
            adapter.appendCity(nextTabIds.last())
            bindCityPageIndicators(cityList)
            if (cityList.isNotEmpty()) {
                mainViewModel.setCity(cityList[mCurIndex])
                mainViewModel.updateWeatherRefreshWindow(cityList, mCurIndex)
                mBinding.viewPager.setCurrentItem(mCurIndex, false)
            }
            return
        }

        val sameTabStructure =
            previousTabIds.isNotEmpty() && previousTabIds.size == nextTabIds.size && previousTabIds == nextTabIds

        if (sameTabStructure) {
            bindCityPageIndicators(cityList)
            if (cityList.isNotEmpty()) {
                mainViewModel.setCity(cityList[mCurIndex])
                mainViewModel.updateWeatherRefreshWindow(cityList, mCurIndex)
                mBinding.viewPager.setCurrentItem(mCurIndex, false)
            }
            return
        }

        bindCityPageIndicators(cityList)
        if (cityList.isNotEmpty()) {
            mainViewModel.setCity(cityList[mCurIndex])
            mainViewModel.updateWeatherRefreshWindow(cityList, mCurIndex)
        }
        adapter?.submitCityIds(nextTabIds)
        mBinding.viewPager.currentItem = mCurIndex
    }

    private fun bindCityPageIndicators(cityList: List<CityEntity>) {
        mBinding.llRound.removeAllViews()
        val size = SizeUtils.dp2px(4f)
        val layoutParams = LinearLayout.LayoutParams(size, size)
        layoutParams.rightMargin = 10
        for (i in cityList.indices) {
            val view = View(mContext)
            view.setBackgroundResource(R.drawable.item_round)
            view.isEnabled = false
            mBinding.llRound.addView(view, layoutParams)
        }
        if (cityList.isNotEmpty()) {
            val dotIndex = mCurIndex.coerceIn(0, cityList.lastIndex)
            mBinding.llRound.getChildAt(dotIndex).isEnabled = true
        }
        mBinding.llRound.visibility = View.VISIBLE
    }

    private fun bindCurrentCityTitle(city: CityEntity?) {
        if (city == null) return
        if (city.isLocal()) {
            mBinding.ivLoc.visibility = View.VISIBLE
            mBinding.cityNameTv.text = city.mergerName.ifBlank { city.cityName }
        } else {
            mBinding.ivLoc.visibility = View.INVISIBLE
            mBinding.cityNameTv.text = city.cityName
        }
    }

}
