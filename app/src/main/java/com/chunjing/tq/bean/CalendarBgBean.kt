package com.chunjing.tq.bean

import com.chunjing.tq.db.entity.CalendarBgEntity
import java.io.Serializable

/**
 * com.chunjing.tq.bean
 */
data class CalendarBgBean(
    val endTime: String,
    val updateTime: String,
    val holidayList: List<CalendarBgEntity>,
    val startTime: String
) : Serializable