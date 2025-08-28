package com.chunjing.tq.bean.juhe

import java.io.Serializable

/**
 * com.chunjing.tq.bean
 */
data class JuheAlarmBean(
    val city: String,
    val content: String,
    val district: String,
    val id: String,
    val level: String,
    val province: String,
    val time: String,
    val title: String,
    val type: String,
    //  更新时间
    var updateTime: Long
) : Serializable