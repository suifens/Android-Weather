package com.chunjing.tq.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.Button
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.github.gzuliyujiang.wheelpicker.DatePicker
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity
import com.github.gzuliyujiang.wheelpicker.impl.BirthdayFormatter
import com.chunjing.tq.R
import com.chunjing.tq.calendarVM
import com.chunjing.tq.databinding.FragmentCalendarContentBinding
import com.chunjing.tq.ui.activity.vm.CalendarViewModel
import com.chunjing.tq.ui.base.BaseViewModel
import com.chunjing.tq.ui.base.BaseVmFragment
import com.goodtech.weatherlib.utils.CalendarUtil
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import java.util.*

@SuppressLint("SetTextI18n")
class CalendarContentFragment : BaseVmFragment<FragmentCalendarContentBinding, BaseViewModel>(),
    CalendarView.OnCalendarSelectListener,
    CalendarView.OnYearChangeListener {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private val mCalendar = java.util.Calendar.getInstance()
    private var mPicker: CalendarPicker? = null
    private var mLastBtn: Button? = null

    override fun bindView() = FragmentCalendarContentBinding.inflate(layoutInflater)

    override fun initView(view: View?) {
        mBinding.calendarView.setOnCalendarSelectListener(this)
        mBinding.calendarView.setOnYearChangeListener(this)

        selectedCalendar(mBinding.calendarView.selectedCalendar)

        mBinding.tvYearMonth.setOnClickListener {
            if (mPicker != null && mPicker!!.isShowing) {
                return@setOnClickListener
            }
            val picker = CalendarPicker(requireActivity())
            picker.setDefaultValue(mYear, mMonth, mDay)
            picker.setOnDatePickedListener { year: Int, month: Int, day: Int ->
                mBinding.calendarView.scrollToCalendar(
                    year,
                    month,
                    day,
                    false,
                    true
                )
            }
            picker.show()
            mPicker = picker
        }
        mBinding.hourAlmanac.btn1.setOnClickListener { onHourBtnClick(it as Button, 0) }
        mBinding.hourAlmanac.btn2.setOnClickListener { onHourBtnClick(it as Button, 1) }
        mBinding.hourAlmanac.btn3.setOnClickListener { onHourBtnClick(it as Button, 2) }
        mBinding.hourAlmanac.btn4.setOnClickListener { onHourBtnClick(it as Button, 3) }
        mBinding.hourAlmanac.btn5.setOnClickListener { onHourBtnClick(it as Button, 4) }
        mBinding.hourAlmanac.btn6.setOnClickListener { onHourBtnClick(it as Button, 5) }
        mBinding.hourAlmanac.btn7.setOnClickListener { onHourBtnClick(it as Button, 6) }
        mBinding.hourAlmanac.btn8.setOnClickListener { onHourBtnClick(it as Button, 7) }
        mBinding.hourAlmanac.btn9.setOnClickListener { onHourBtnClick(it as Button, 8) }
        mBinding.hourAlmanac.btn10.setOnClickListener { onHourBtnClick(it as Button, 9) }
        mBinding.hourAlmanac.btn11.setOnClickListener { onHourBtnClick(it as Button, 10) }
        mBinding.hourAlmanac.btn12.setOnClickListener { onHourBtnClick(it as Button, 11) }
    }

    override fun initEvent() {
        calendarVM.dayDetail.observe(this) {
            mBinding.detailLayout.tvLunarDay.text = it.lunar
            mBinding.detailLayout.tvLunar.text = "${it.lunarYear}(${it.animalsYear})年"
            mBinding.detailLayout.tvDetailSuit.text = it.suit
            mBinding.detailLayout.tvDetailAvoid.text = it.avoid
        }

        calendarVM.mHolidayList.observe(this) {
            val map: MutableMap<String, Calendar> = HashMap()
            for (holiday in it) {
                if (holiday.list != null) {
                    for (day in holiday.list) {
                        val date: Long = CalendarUtil.longWithDate(day.date, "yyyy-M-d")
                        val scheme = getSchemeCalendar(date, 0x40DB25, "")
                        map[scheme.toString()] = scheme
                    }
                }
            }
            mBinding.calendarView.setSchemeDate(map)
        }

        calendarVM.dayAlmanac.observe(this) {
            val view = mBinding.dayAlmanac
            view.tvWuxing.text = it.wuxing
            view.tvChongsha.text = it.chongsha
            view.tvJishen.text = it.jishen
            view.tvXiongshen.text = it.xiongshen
            view.tvBaiji.text = it.baiji
        }

        calendarVM.hoursAlmanac.observe(this) {
            onHourBtnClick(mBinding.hourAlmanac.btn1, 0)
        }

        calendarVM.curHour.observe(this) {
            val view = mBinding.hourAlmanac
            if (it != null) {
                view.tvHours.text = "${it.time}时：${it.hours}"
                view.tvDes.text = it.des
                view.tvDetailSuit.text = it.yi
                view.tvDetailAvoid.text = it.ji
            } else {
                view.tvHours.text = ""
                view.tvDes.text = ""
                view.tvDetailSuit.text = ""
                view.tvDetailAvoid.text = ""
            }
        }
    }

    override fun loadData() {
        val calendar = mBinding.calendarView.selectedCalendar
        getData(calendar.timeInMillis)
    }

    override fun onCalendarOutOfRange(calendar: Calendar) {
    }

    override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {
        selectedCalendar(calendar)
    }

    override fun onYearChange(year: Int) {

    }

    private fun selectedCalendar(calendar: Calendar) {
        mBinding.tvYearMonth.text = "${calendar.year}年${calendar.month}月"
        mYear = calendar.year
        mMonth = calendar.month
        mDay = calendar.day
        mCalendar.set(mYear, mMonth, mDay)
        getData(calendar.timeInMillis)
    }

    private fun getData(millis: Long) {
        updateHoliday(millis)
        val day = CalendarUtil.longToString(millis, "yyyy-M-d")
        calendarVM.getDayDetails(day)
        calendarVM.getDayAlmanac(day)
        calendarVM.getHoursAlmanac(day)
    }

    private fun getSchemeCalendar(date: Long, color: Int, text: String): Calendar {
        val calendar = Calendar()
        calendar.year = CalendarUtil.getYear(date)
        calendar.month = CalendarUtil.getMonth(date)
        calendar.day = CalendarUtil.getDay(date)
        calendar.schemeColor = color //如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        return calendar
    }

    private fun onHourBtnClick(btn: Button, index: Int) {
        mLastBtn?.let { setButtonSelected(it, false) }
        setButtonSelected(btn, true)
        mLastBtn = btn

        calendarVM.selectedHour(index)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setButtonSelected(btn: Button, isSelected: Boolean) {
        if (isSelected) {
            btn.background = AppCompatResources.getDrawable(requireContext(), R.drawable.bg_circle_red)
            btn.setTextColor(Color.WHITE)
        } else {
            btn.setBackgroundColor(Color.TRANSPARENT)
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_515151_50))
        }
    }

    /**
     * 更新假期
     */
    private fun updateHoliday(timeMillis: Long) {
        var year: Int = CalendarUtil.getYear(timeMillis)
        if (CalendarUtil.afterHoliday(timeMillis)) {
            //  之后无假期，加载下一年的假期
            year += 1
        }
        val yearStr = year.toString()
        calendarVM.getHolidays(yearStr)
    }

    class CalendarPicker : DatePicker {
        constructor(activity: Activity) : super(activity)
        constructor(activity: Activity, @StyleRes themeResId: Int) : super(activity, themeResId)

        private var defaultValue: DateEntity? = null
        override fun initData() {
            super.initData()
            titleView.text = "选择日期"
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar[java.util.Calendar.YEAR]
            val startValue = DateEntity.target(currentYear - 50, 1, 1)
            val endValue = DateEntity.target(currentYear + 50, 12, 12)
            wheelLayout.setRange(startValue, endValue)
            wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY)
            wheelLayout.setDateFormatter(BirthdayFormatter())
            defaultValue?.let {
                wheelLayout.setDefaultValue(defaultValue)
            }
        }

        fun setDefaultValue(year: Int, month: Int, day: Int) {
            defaultValue = DateEntity.target(year, month, day)
        }
    }

}