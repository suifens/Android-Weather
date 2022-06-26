package com.goodtech.tq.listener;

public interface WeatherHeaderListener {
    //  台风路径
    void onTyphoon();
    //  空气质量
    void onAirQuality();
    //  万年历
    void onCalendar();
    //  星座运势
    void onFortune();
    // //  新闻点击
    // void onNews();
    //  点击签到
    void onSignIn();
    //  疫情出行
    void onOutbreakTravel();
    //  抖音彩铃
    void onDyMovie();
}
