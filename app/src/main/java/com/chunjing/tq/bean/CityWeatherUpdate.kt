package com.chunjing.tq.bean

/**
 * 单城天气更新，避免 [com.chunjing.tq.ui.activity.vm.MainViewModel.weatherMap] 全量广播。
 */
data class CityWeatherUpdate(
    val cityId: String,
    val weather: WeatherBean,
)
