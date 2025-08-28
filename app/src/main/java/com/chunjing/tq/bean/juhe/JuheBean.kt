package com.chunjing.tq.bean.juhe

/**
 * com.chunjing.tq.bean
 */
data class JuheBean<T>(
    val error_code: Int,
    val reason: String,
    val result: T?
)
