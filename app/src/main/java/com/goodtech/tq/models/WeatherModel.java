package com.goodtech.tq.models;

import com.goodtech.tq.utils.TimeUtils;

import java.util.Date;
import java.util.List;

/**
 * com.goodtech.tq.models
 */
public class WeatherModel {

    public double latitude;

    public double longitude;

    public long expireTime;

    public Observation observation;

    //  24小时天气
    public List<Hourly> hourlies;

    //  10天天气
    public List<Daily> dailies;

    public boolean needReload() {
        long curTime = System.currentTimeMillis()/1000;
        if (curTime - expireTime > 3600
                || !TimeUtils.timeToHH(curTime).equals(TimeUtils.timeToHH(expireTime))) {
            return true;
        }
        return false;
    }

    public Daily today() {
        if (dailies.size() > 0) {
            long currentTime = System.currentTimeMillis();
            String currentLoc = TimeUtils.stringToLong(currentTime, "yyyy-MM-dd");

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

        if (dailies.size() > 0) {

            long time = System.currentTimeMillis() + 1000*3600*24;
            String timeLoc = TimeUtils.stringToLong(time, "yyyy-MM-dd");

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
