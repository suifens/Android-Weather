package com.chunjing.tq.bean

import java.io.Serializable

/*
* private String cityName;
    private String cityId;
    private String cnty;
    private String location;
    private String parentCity;
    private String adminArea;
    private boolean isFavor;
*
* */
data class CityBean(
    val cid: Int = 0,
    val mergerName: String = "",
    val lat: String = "",
    val lon: String = "",
    val pinyin: String = "",

    var listNum: Int = 0,
    var location: Boolean = false,
    var viewType: Int = 0
) : Serializable