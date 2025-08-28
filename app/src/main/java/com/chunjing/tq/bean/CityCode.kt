package com.chunjing.tq.bean

import java.io.Serializable

/**
 * com.chunjing.tq.bean
 */
data class CityCode(
    val city_code: String,
    val city_name: String,
    val province_code: String
) : Serializable