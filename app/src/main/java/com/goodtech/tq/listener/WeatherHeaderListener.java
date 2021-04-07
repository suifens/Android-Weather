package com.goodtech.tq.listener;

public interface WeatherHeaderListener {
    //  周末天气
    void onWeekendWeather();
    //  空气质量
    void onAirQuality();
    //  万年历
    void onCalendar();
    //  星座运势
    void onFortune();
}
