package com.goodtech.tq.jpush

import android.text.TextUtils

enum class WebViewType {
    NORMAL,
    FULL_SCREEN,    //  全屏展示
    NEED_NAVBAR;     //  导航栏

    companion object {
        @JvmStatic
        fun getType(type: String?): WebViewType {
            if (TextUtils.isEmpty(type)) {return NORMAL}
            return try {
                valueOf(type!!)
            } catch (e: Exception) {
                NORMAL
            }
        }
    }
}