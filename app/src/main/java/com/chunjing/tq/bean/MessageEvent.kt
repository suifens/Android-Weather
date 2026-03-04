package com.chunjing.tq.bean

/**
 * com.chunjing.tq.bean
 */
data class MessageEvent(
    var cityChanged: Boolean = false,
    //  定位更改
    var locationChanged: Boolean = false,
    //  选中的城市
    var selectedCityId: String = "",
    //  去广告后需要刷新界面（隐藏广告）
    var needReload: Boolean = false,
)



