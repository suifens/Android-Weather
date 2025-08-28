package com.chunjing.tq.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chunjing.tq.R
import com.chunjing.tq.adapter.ActivityPagerAdapter
import com.chunjing.tq.adapter.CityListAdapter
import com.chunjing.tq.adapter.FragmentPagerAdapter
import com.chunjing.tq.bean.MessageEvent
import com.chunjing.tq.bean.WeatherBean
import com.chunjing.tq.databinding.ActivityCityListBinding
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ext.init
import com.chunjing.tq.mainViewModel
import com.chunjing.tq.ui.base.BaseActivity
import com.chunjing.tq.ui.fragment.WeatherItemFragment
import com.goodtech.weatherlib.ext.clickNoRepeat
import com.goodtech.weatherlib.extension.startActivity
import com.goodtech.weatherlib.view.GridSpaceItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CityListActivity : BaseActivity<ActivityCityListBinding>() {

    private var mCurIndex = 1
    private val cities by lazy { ArrayList<CityEntity>() }
    private val fragments: MutableList<Fragment> by lazy { ArrayList() }
    private val weatherMap by lazy { HashMap<String, WeatherBean>() }
    private var mCityChanged = false
    private var mShowRecycler = true

    private var mAdapter: CityListAdapter? = null
    private var tempAdapter: ActivityPagerAdapter? = null

    override fun bindView() = ActivityCityListBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
    }

    override fun initView() {

        configStationBar(mBinding.privateStationBar)
        mBinding.backButton.setOnClickListener {
            finish()
        }
        BarUtils.setStatusBarLightMode(this, true)
        //  城市列表
        mBinding.editButton.setOnClickListener { startActivity<CityManagerActivity>() }

        showRecycler()
    }

    private fun showRecycler() {
        mShowRecycler = true
        if (mAdapter == null) {
            mAdapter = CityListAdapter(cities, weatherMap) { _, i ->
                if (i == 0) {
                    startActivity<AddCityActivity>()
                } else {
                    MainActivity.startActivity(this, i - 1)
                    finish()
                }
            }

            //  添加间距
            mBinding.recyclerView.addItemDecoration(
                GridSpaceItemDecoration(
                    2,
                    SizeUtils.dp2px(10f),
                    (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(150f) * 2) / 3
                )
            )
            mBinding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            mBinding.recyclerView.adapter = mAdapter
        }
        mBinding.recyclerView.visibility = View.VISIBLE
        mBinding.viewPager.visibility = View.GONE
        mBinding.llRound.visibility = View.GONE
        mBinding.contentView.setBackgroundResource(R.drawable.gradient_weather_main)
    }

    private fun showPager(update: Boolean) {
        mShowRecycler = false
        mBinding.recyclerView.visibility = View.GONE
        mBinding.viewPager.visibility = View.VISIBLE
        mBinding.llRound.visibility = View.VISIBLE

        var needUpdate = update
        if (tempAdapter == null) {
            needUpdate = true
            tempAdapter = ActivityPagerAdapter(this, fragments)
            mBinding.viewPager.let {
                it.setPadding(SizeUtils.dp2px(50f), 0, SizeUtils.dp2px(50f), 0)
                it.clipToPadding = false
                it.clipChildren = false

                it.adapter = tempAdapter
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
                            if (cities.size > position - 1) {
                                val entity = cities[position - 1]
                                weatherMap[entity.cityId]?.let { weather ->
                                    mainViewModel.getWeatherBg(weather) { bgEntity ->
                                        if (bgEntity != null && !TextUtils.isEmpty(bgEntity.startColor)) {
                                            changeBackground(bgEntity.startColor, bgEntity.endColor)
                                        }
                                    }
                                }

                            }
                        }
                    }
                })
            }
        }

        if (needUpdate) {
            updatePager()
        }
    }
    private fun updatePager() {
        tempAdapter?.let {
            if (fragments.size > 0) {
                fragments.clear()
                it.updateData(fragments)
            }
            val addItemFragment = WeatherItemFragment()
            fragments.add(addItemFragment)
            for (i in 0 until cities.size) {
                val city = cities[i]
                val weather = weatherMap[city.cityId]
                val itemFragment = WeatherItemFragment()
                itemFragment.setupWeather(city, weather, i + 1)
                Log.e("TAG", "updatePager: $itemFragment")
                fragments.add(itemFragment)
            }
            mBinding.viewPager.adapter = ActivityPagerAdapter(this, fragments)
            mBinding.viewPager.currentItem = mCurIndex
        }
    }

    override fun initEvent() {

        mBinding.gridImgView.clickNoRepeat {
            mBinding.gridImgView.setImageResource(R.drawable.ic_collection_s)
            mBinding.pagerImgView.setImageResource(R.drawable.ic_pager_n)
            showRecycler()
        }

        mBinding.pagerImgView.clickNoRepeat {
            mBinding.gridImgView.setImageResource(R.drawable.ic_collection_n)
            mBinding.pagerImgView.setImageResource(R.drawable.ic_pager_s)
            showPager(false)
        }

        mainViewModel.cities.observe(this) {
            updateCities()
            mCityChanged = false
        }

        mainViewModel.weatherMap.observe(this) {
            changeWeatherMap(it)
        }

        //注册订阅者
        //避免重复注册，重复注册会导致崩溃
        if (!EventBus.getDefault().isRegistered(this)) { //这里的取反别忘记了
            EventBus.getDefault().register(this)
        }
    }

    override fun initData() {
//        mainViewModel.getCitiesCache()
        updateCities()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun backAction() {
        if (mainViewModel.cities.value == null || mainViewModel.cities.value!!.isEmpty()) {
            AddCityActivity.startActivity(this, true)
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (mCityChanged) {
            mainViewModel.getCitiesCache()
        } else if (mainViewModel.cities.value != null) {
            for (i in mainViewModel.cities.value!!.indices) {
                mBinding.llRound.getChildAt(i).isEnabled = mCurIndex == i
            }
            mBinding.viewPager.setCurrentItem(mCurIndex, true)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateCities() {
        mainViewModel.cities.value?.let {
            mainViewModel.weatherMap.value?.let { map ->
                this.weatherMap.clear()
                this.weatherMap.putAll(map)
            }

            this.cities.clear()
            this.cities.addAll(it)
            this.mAdapter?.notifyDataSetChanged()

            configRounds(cities.size + 1)
            updatePager()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeWeatherMap(weatherMap: HashMap<String, WeatherBean>?) {
        weatherMap?.let {
            this.weatherMap.clear()
            this.weatherMap.putAll(it)
            this.mAdapter?.notifyDataSetChanged()
            updatePager()
        }
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
            val view = View(this)
            view.setBackgroundResource(R.drawable.item_round)
            view.isEnabled = false

            // 添加到LinearLayout
            mBinding.llRound.addView(view, layoutParams)
        }
        // 小白点
        mBinding.llRound.getChildAt(mCurIndex).isEnabled = true
    }

    private fun changeBackground(start: String, end: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val gradient = GradientDrawable()
            val startColor = ColorUtils.string2Int(start)
            val endColor = ColorUtils.string2Int(end)
            mBinding.contentView.background =
                gradient.init(startColor, endColor, GradientDrawable.Orientation.TOP_BOTTOM)
        }
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