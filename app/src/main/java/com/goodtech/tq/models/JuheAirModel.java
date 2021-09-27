package com.goodtech.tq.models;

import com.goodtech.tq.helpers.AqiHelper;

/**
 * com.goodtech.tq.models
 */
public class JuheAirModel {

    private String temperature;
    private String humidity;
    private String info;
    private String wid;
    private String direct;
    private String power;
    private String aqi;
    private long updateTime;

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }

    public String getDirect() {
        return direct;
    }

    public void setDirect(String direct) {
        this.direct = direct;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取提示语
     */
    public String getRemindString() {
        return AqiHelper.getRemindString(Integer.parseInt(aqi));
    }

    /**
     * 获取背景颜色图片
     */
    public int getImgResId() {
        return AqiHelper.getImgResId(Integer.parseInt(aqi));
    }

    public int getColorResId() {
        return AqiHelper.getColorResId(Integer.parseInt(aqi));
    }

    public String getQuality() {
        return AqiHelper.getQuality(Integer.parseInt(aqi));
    }
}
