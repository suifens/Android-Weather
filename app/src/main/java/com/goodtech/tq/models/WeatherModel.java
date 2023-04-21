package com.goodtech.tq.models;

import androidx.annotation.NonNull;

import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.WeatherUtils;

import java.util.List;

/**
 * com.goodtech.tq.models
 */
public class WeatherModel {

    public int cid;

    public double latitude;

    public double longitude;
    //  到期时间
    public long expireTime;

    public Observation observation;

    //  24小时天气
    public List<Hourly> hourlies;

    //  10天天气
    public List<Daily> dailies;

    //  天气质量
    public int aqi;

    public JuheLifeModel lifeModel;

    public JuheAlarmModel alarmModel;

    public boolean needReload() {
        long curTime = System.currentTimeMillis() / 1000;
        return curTime - expireTime > 3600
                || !TimeUtils.timeToHH(curTime).equals(TimeUtils.timeToHH(expireTime));
    }

    //  获取背景图标
    public int getIconCd() {
        int icon_cd = -1;
        if (observation != null) {
            icon_cd = observation.wxIcon;

            String current = TimeUtils.longToString(System.currentTimeMillis(), "MMddHH");
            for (Hourly hourly : hourlies) {
                if (hourly != null) {
                    String dayHour = TimeUtils.longToString(hourly.fcst_valid * 1000, "MMddHH");
                    if (dayHour.equals(current)) {
                        icon_cd = hourly.icon_cd;
                    }
                }
            }
        }
        return icon_cd;
    }

    @NonNull
    @Override
    public String toString() {
        return "WeatherModel {" + "\n" +
                "cid = " + cid + "\n" +
                "latitude = " + latitude + "\n" +
                "longitude = " + longitude + "\n" +
                "expireTime = " + expireTime + "\n" +
                "observation = " + observation.toString() + "\n" +
                "hourlies = " + hourlies.toString() + "\n" +
                "dailies = " + dailies.toString() + "\n" +
                '}';
    }

    public Daily today() {
        if (dailies != null && dailies.size() > 0) {
            long currentTime = System.currentTimeMillis();
            String currentLoc = TimeUtils.longToString(currentTime, "yyyy-MM-dd");

            for (int i = 0; i < dailies.size(); i++) {
                Daily daily = dailies.get(i);
                String dayLocal = daily.fcst_valid_local;
                if (dayLocal.contains(currentLoc)) {
                    return daily;
                }
            }
        }
        return null;
    }

    public Daily tomorrow() {

        if (dailies != null && dailies.size() > 0) {

            long time = System.currentTimeMillis() + 1000 * 3600 * 24;
            String timeLoc = TimeUtils.longToString(time, "yyyy-MM-dd");

            for (int i = 0; i < dailies.size(); i++) {
                Daily daily = dailies.get(i);
                String dayLocal = daily.fcst_valid_local;
                if (dayLocal.contains(timeLoc)) {
                    return daily;
                }
            }
        }
        return null;
    }

}
