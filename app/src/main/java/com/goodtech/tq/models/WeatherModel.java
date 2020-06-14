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

    public int icon_cd;

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
            return dailies.get(0);
        }
        return null;
    }

}
