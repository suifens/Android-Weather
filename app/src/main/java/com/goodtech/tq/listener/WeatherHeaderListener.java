package com.goodtech.tq.listener;

import com.goodtech.tq.models.LifeItemBean;

public interface WeatherHeaderListener {
    //  台风路径
    void onTyphoon();
    //  空气质量
    void onAirQuality();
    //  万年历
    void onCalendar();
    //  星座
    void onFortune();
    // //  新闻点击
    // void onNews();
    //  点击签到
    void onSignIn();
    //  打车
    void onTaxi();
    //  美团
    void onMeituan();
    //  饿了吗
    void onEleme();

    void onWarningBtn();

    void onShortPlayer();
    void onMiniVideo();

    void onLifeItem(LifeItemBean lifeItemBean);

}
