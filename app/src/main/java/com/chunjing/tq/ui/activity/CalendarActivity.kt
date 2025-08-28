package com.chunjing.tq.ui.activity

import android.content.Intent
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.BarUtils
import com.chunjing.tq.databinding.ActivityCalendarBinding
import com.chunjing.tq.ext.bindViewPager2
import com.chunjing.tq.ext.init
import com.chunjing.tq.ui.activity.vm.CalendarViewModel
import com.chunjing.tq.ui.base.BaseVmActivity
import com.chunjing.tq.ui.fragment.CalendarContentFragment
import com.chunjing.tq.ui.fragment.HolidayFragment

class CalendarActivity : BaseVmActivity<ActivityCalendarBinding, CalendarViewModel>() {

    private val titleData = arrayListOf("日历", "假期")
    private val fragments: ArrayList<Fragment> = arrayListOf()

    override fun bindView() = ActivityCalendarBinding.inflate(layoutInflater)

    override fun prepareData(intent: Intent?) {
        fragments.add(CalendarContentFragment())
        fragments.add(HolidayFragment())
    }

    override fun initView() {

        configStationBar(mBinding.privateStationBar)
        mBinding.backButton.setOnClickListener { finish() }
        BarUtils.setStatusBarLightMode(this, true)

        //初始化viewpager2
        mBinding.viewPager.init(this, fragments).offscreenPageLimit = fragments.size
        mBinding.viewPager.isUserInputEnabled = false
        //初始化 magic_indicator
        mBinding.magicIndicator.bindViewPager2(mBinding.viewPager, titleData) {

        }
    }

    override fun initEvent() {

    }

    override fun initData() {
    }
}