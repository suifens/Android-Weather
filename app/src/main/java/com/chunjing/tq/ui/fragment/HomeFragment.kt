package com.chunjing.tq.ui.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.R
import com.chunjing.tq.adapter.FragmentPagerAdapter
import com.chunjing.tq.bean.MessageEvent
import com.chunjing.tq.databinding.FragmentHomeBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ext.checkPermissionAgree
import com.chunjing.tq.ext.init
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.activity.CityListActivity
import com.chunjing.tq.ui.activity.WeatherShareActivity
import com.chunjing.tq.ui.activity.vm.MainViewModel
import com.chunjing.tq.ui.base.BaseVmFragment
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.net.LoadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("SetTextI18n")
class HomeFragment : BaseVmFragment<FragmentHomeBinding, MainViewModel>() {

    private var mCurIndex = 1
    private val mCityList = ArrayList<CityEntity>()
    private val fragments: MutableList<Fragment> by lazy { ArrayList() }

    private var tempAdapter: FragmentPagerAdapter? = null
    private var mCityChanged: Boolean = false
    private var mSelectedCityId: String = ""
    //  是否在 Start和Pause之间
    private var isShowing: Boolean = false

    override fun bindView() = FragmentHomeBinding.inflate(layoutInflater)

    override fun initView(view: View?) {

        val bars = ConstraintLayout.LayoutParams(mBinding.privateStationBar.layoutParams)
        bars.height = bars.height + BarUtils.getStatusBarHeight()
        mBinding.privateStationBar.layoutParams = bars
        mBinding.soulTv.isSelected = true

        mBinding.btnAdd.setOnClickListener {
            requireActivity().checkPermissionAgree {
                startActivity<CityListActivity>()
            }
        }

        mBinding.btnShare.setOnClickListener {
            showLoading(true)
            startActivity<WeatherShareActivity>()
        }

        mBinding.viewPager.let {
            it.setPadding(SizeUtils.dp2px(50f), 0, SizeUtils.dp2px(50f), 0)
            it.clipToPadding = false
            it.clipChildren = false

            it.adapter = FragmentPagerAdapter(this, fragments)
            it.offscreenPageLimit = 5
            val pageTransformer = CompositePageTransformer()
            //  设置间距
            pageTransformer.addTransformer(MarginPageTransformer(SizeUtils.dp2px(20f)))
            //  设置缩放
            pageTransformer.addTransformer(MyTransformer())
            it.setPageTransformer(pageTransformer)

            //  滑动监听
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    mBinding.llRound.getChildAt(mCurIndex)?.isEnabled = false
                    mBinding.llRound.getChildAt(position)?.isEnabled = true
                    mCurIndex = position

                    if (position == 0) {
                        mBinding.contentView.setBackgroundResource(R.drawable.gradient_weather_main)
                    } else {
                        if (mCityList.size > position - 1) {
                            mainViewModel.setCity(mCityList[position - 1])
                        }
                    }
                }
            })
        }

        //注册订阅者
        //避免重复注册，重复注册会导致崩溃
        if (!EventBus.getDefault().isRegistered(this)) { //这里的取反别忘记了
            EventBus.getDefault().register(this)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initEvent() {
        mainViewModel.curLocation.observe(this) {
            if (isShowing) {
                mCityChanged = true
                mainViewModel.getCitiesCache()
            }
        }

        mainViewModel.cities.observe(this) {
            if (mCityChanged && isShowing) {
                showCity(it)
                mCityChanged = false
            }
        }

        mainViewModel.curBgEntity.observe(this) {
            if (isShowing && !TextUtils.isEmpty(it.startColor)) {
                changeBackground(it.startColor, it.endColor)
            }
        }

        mainViewModel.weatherMap.observe(this) {
            if (isShowing) {
                tempAdapter?.notifyDataSetChanged()
            }
        }

        mainViewModel.loadState.observe(this) {
            if (isShowing) {
                when (it) {
                    is LoadState.Start -> {
                        showLoading(true)
                    }
                    is LoadState.Error -> {

                    }
                    is LoadState.Finish -> {
                        showLoading(false)
                    }
                }
            }
        }

        mainViewModel.todaySoul.observe(this) {
            if (it != null) {
                mBinding.soulTv.text = it
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun loadData() {
        mCityChanged = true
        isShowing = true
    }

    /**
     * 显示城市
     *
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun showCity(cityList: List<CityEntity>) {
        mCityList.clear()
        mCityList.addAll(cityList)

        if (mCurIndex >= mCityList.size) {
            mCurIndex = mCityList.size
        } else if (mCurIndex == 0) {
            mCurIndex = 1
        }

        configRounds(mCityList.size + 1)

        fragments.clear()
        val addItemFragment = HomeItemFragment.newInstance("0", 0)
        fragments.add(addItemFragment)
        for (i in 0 until mCityList.size) {
            val city = cityList[i]
            val homeItemFragment = HomeItemFragment.newInstance(city.cityId, i + 1)
            fragments.add(homeItemFragment)
        }
        mBinding.viewPager.adapter = FragmentPagerAdapter(this, fragments)
        mBinding.viewPager.currentItem = mCurIndex
    }

    //  配置 rounds
    private fun configRounds(count: Int) {
        mBinding.llRound.removeAllViews()

        // 宽高参数
        val size = SizeUtils.dp2px(6f)
        val layoutParams = LinearLayout.LayoutParams(size, size)
        // 设置间隔
        layoutParams.rightMargin = 12

        for (i in 0 until count) {
            // 创建底部指示器(小圆点)
            val view = View(requireActivity())
            view.setBackgroundResource(R.drawable.item_round)
            view.isEnabled = false

            // 添加到LinearLayout
            mBinding.llRound.addView(view, layoutParams)
        }
        // 小白点
        mBinding.llRound.getChildAt(mCurIndex).isEnabled = true
    }

    //  更改背景颜色
    private fun changeBackground(start: String, end: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val gradient = GradientDrawable()
            val startColor = ColorUtils.string2Int(start)
            val endColor = ColorUtils.string2Int(end)
            mBinding.contentView.background =
                gradient.init(startColor, endColor, GradientDrawable.Orientation.TOP_BOTTOM)
        }
    }

    override fun onStart() {
        super.onStart()
        isShowing = true
    }

    override fun onResume() {
        super.onResume()
        if (mCityChanged) {
            mainViewModel.getCitiesCache()
        } else {
            for (i in mainViewModel.cities.value!!.indices) {
                mBinding.llRound.getChildAt(i).isEnabled = mCurIndex == i
            }
            mBinding.viewPager.setCurrentItem(mCurIndex, true)
        }

        mainViewModel.fetchSoul()

        val hour = TimeUtils.millis2String(System.currentTimeMillis(), "HH").toInt()
        mBinding.tvGood.text = when (hour) {
            in 5..7 -> "早上好"
            in 8..11 -> "上午好"
            in 12..13 -> "中午好"
            in 14..17 -> "下午好"
            in 18..21 -> "晚上好"
            else -> "早睡早起哦"
        }
    }

    override fun onPause() {
        isShowing = false
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        dismissLoading()
    }

    override fun onDestroy() {
        super.onDestroy()
        //注销订阅者
        EventBus.getDefault().unregister(this)
    }

    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.cityChanged) {
            mCityChanged = true
        }
        mSelectedCityId = event.selectedCityId
    }

    inner class MyTransformer : ViewPager2.PageTransformer {
        private val DEFAULT_MIN_SCALE = 0.85f
        private val DEFAULT_CENTER = 0.5f

        private val mMinScale = DEFAULT_MIN_SCALE
        override fun transformPage(page: View, position: Float) {
            val pageWidth = page.width
            val pageHeight = page.height
            //动画锚点设置为View中心
            //动画锚点设置为View中心
            page.pivotX = (pageWidth / 2).toFloat()
            page.pivotY = (pageHeight / 2).toFloat()
            if (position < -1) {
                //屏幕左侧不可见时
                page.scaleX = mMinScale
                page.scaleY = mMinScale
                page.pivotY = (pageWidth / 2).toFloat()
            } else if (position <= 1) {
                if (position < 0) {
                    //屏幕左侧
                    //(0,-1)
                    val scaleFactor: Float = (1 + position) * (1 - mMinScale) + mMinScale
                    page.scaleX = scaleFactor
                    page.scaleY = scaleFactor
                    page.pivotX = pageWidth.toFloat()
                } else {
                    //屏幕右侧
                    //(1,0)
                    val scaleFactor: Float = (1 - position) * (1 - mMinScale) + mMinScale
                    page.scaleX = scaleFactor
                    page.scaleY = scaleFactor
                    page.pivotX = pageWidth * ((1 - position) * DEFAULT_CENTER)
                }
            } else {
                //屏幕右侧不可见
                page.pivotX = 0f
                page.scaleY = mMinScale
                page.scaleY = mMinScale
            }
        }
    }
}