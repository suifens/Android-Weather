package com.chunjing.tq.ui.activity.vm

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.chunjing.tq.R
import com.chunjing.tq.bean.calendar.*
import com.chunjing.tq.ext.JUHE_DAY_ALMANAC
import com.chunjing.tq.ext.JUHE_DAY_DETAIL
import com.chunjing.tq.ext.JUHE_HOURS_ALMANAC
import com.chunjing.tq.ext.JUHE_MONTH_HOLIDAY
import com.chunjing.tq.ui.base.BaseViewModel
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.utils.SpUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CalendarViewModel(val app: Application) : BaseViewModel(app) {

    val dayDetail = MutableLiveData<DayDetail>()
    val dayAlmanac = MutableLiveData<DayAlmanac>()
    val hoursAlmanac = MutableLiveData<List<HourAlmanac>>()
    val curHour = MutableLiveData<HourAlmanac?>()
    var mHolidayList = MutableLiveData<List<Holiday>>()
    var mHolidayYear: String = "" //  加载假期的年份

    val hourArray = app.resources.getStringArray(R.array.double_hour)

    private val mDayDetails: MutableMap<String, DayDetail?> = HashMap()
    private val mDayAlmanacs: MutableMap<String, DayAlmanac?> = HashMap()
    private val mHoursAlmanacs: MutableMap<String, List<HourAlmanac>> = HashMap()
    private val mHolidayMap: MutableMap<String, List<Holiday>> = HashMap()
    private var hours: List<HourAlmanac> = ArrayList()

    fun getDayDetails(day: String) {
        launchSilent {
            if (mDayDetails.containsKey(day)) {
                dayDetail.postValue(mDayDetails[day])
                return@launchSilent
            }

            val url = String.format(JUHE_DAY_DETAIL, day)
            val result = HttpUtils.get<CalendarBean<DayDetail>>(url)
            result?.let {
                it.result!!.data.let { data ->
                    dayDetail.postValue(data)
                    mDayDetails[day] = data
                }
            }
        }
    }

    /**
     * 获取老黄历日历
     */
    fun getDayAlmanac(day: String) {
        launchSilent {
            if (mDayAlmanacs.containsKey(day)) {
                dayAlmanac.postValue(mDayAlmanacs[day])
                return@launchSilent
            }

            val url = String.format(JUHE_DAY_ALMANAC, day)
            val result = HttpUtils.get<AlmanacBean<DayAlmanac>>(url)
            result?.let {
                it.result?.let { data ->
                    dayAlmanac.postValue(data)
                    mDayAlmanacs[day] = data
                }
            }
        }
    }

    /**
     * 获取老黄历日历
     */
    fun getHoursAlmanac(day: String) {
        launchSilent {
            if (mHoursAlmanacs.containsKey(day)) {
                hoursAlmanac.postValue(mHoursAlmanacs[day])
                hours = mHoursAlmanacs[day]!!
                return@launchSilent
            }

            val url = String.format(JUHE_HOURS_ALMANAC, day)
            val result = HttpUtils.get<AlmanacBean<List<HourAlmanac>>>(url)
            result?.let {
                it.result?.let { data ->
                    hours = data
                    hoursAlmanac.postValue(data)
                    mHoursAlmanacs[day] = data
                }
            }
        }
    }

    fun getHolidays(year: String) {
        launchSilent {
            mHolidayYear = year
            if (mHolidayMap.containsKey(year)) {
                val holidayList = mHolidayMap[year]
                mHolidayList.postValue(holidayList?.sorted() ?: arrayListOf())
                return@launchSilent
            }
            val holidayJson = SpUtils.instance.getString("holiday-$year", "")
            if (holidayJson.isNotEmpty()) {
                val list: List<Holiday> = Gson().fromJson(
                    holidayJson,
                    object : TypeToken<List<Holiday?>?>() {}.type
                )
                if (list.isNotEmpty()) {
                    mHolidayList.postValue(list)
                    mHolidayMap[year] = list
                    return@launchSilent
                }
            }
            val holidayList = arrayListOf<Holiday>()
            var count = 0
            for (i in 1..11) {
                @SuppressLint("DefaultLocale")
                val yearMonth = String.format("%s-%d", year, i)
                val url: String = String.format(JUHE_MONTH_HOLIDAY, yearMonth)
                val result = HttpUtils.get<CalendarBean<HolidayBean>>(url)
                result?.let {
                    count++
                    it.result!!.data.let { data ->
                        data.holiday_array.let { dataList ->
                            for (holiday in dataList) {
                                var isContain = false
                                for (j in holidayList.indices.reversed()) {
                                    val lastDay: Holiday = holidayList[j]
                                    if (lastDay.name == holiday.name) {
                                        isContain = true
                                        break
                                    }
                                }
                                if (!isContain) {
                                    holidayList.add(holiday)
                                }
                            }
                        }
                    }
                }
            }
            if (count == 11) {
                if (holidayList.size > 0) {
                    mHolidayList.postValue(holidayList)
                    mHolidayMap[year] = holidayList
                    SpUtils.instance.putString(
                        "holiday-$year",
                        Gson().toJson(holidayList)
                    )
                } else {
                    mHolidayList.postValue(holidayList)
                }
            }
        }

    }

    //  选择时辰
    fun selectedHour(index: Int) {
        if (hours.isEmpty()) {
            curHour.postValue(null)
        } else {
            val hourAlmanac = hours[index]
            val time = hourArray[index]
            hourAlmanac.time = time
            curHour.postValue(hourAlmanac)
        }
    }

}