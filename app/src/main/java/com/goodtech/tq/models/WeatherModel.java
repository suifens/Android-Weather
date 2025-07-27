package com.goodtech.tq.models;

import androidx.annotation.NonNull;

import com.goodtech.tq.utils.TimeUtils;

import java.util.List;

/**
 * 天气数据模型类
 * 包含当前天气、24小时预报、10天预报、空气质量、生活指数、预警信息等完整天气数据
 * 是天气应用的核心数据模型
 */
public class WeatherModel {

    /** 地点ID，用于标识具体的地理位置 */
    public String poiId;

    /** 纬度坐标 */
    public double latitude;

    /** 经度坐标 */
    public double longitude;
    
    /** 数据到期时间戳（秒），用于判断数据是否需要刷新 */
    public long expireTime;

    /** 当前天气观测数据，包含实时天气信息 */
    public Observation observation;

    /** 24小时天气预报列表，每小时一个数据点 */
    public List<Hourly> hourlies;

    /** 10天天气预报列表，每天一个数据点 */
    public List<Daily> dailies;

    /** 空气质量指数(AQI)，数值越大表示污染越严重 */
    public int aqi;

    /** 生活指数数据，包含穿衣、运动、紫外线等生活建议 */
    public LifeEntity lifeModel;

    /** 天气预警信息，包含预警类型、级别、内容等 */
    public JuheAlarmModel alarmModel;

    /**
     * 判断是否需要重新加载数据
     * 当数据过期时间超过1小时或小时数发生变化时需要重新加载
     * @return true表示需要重新加载，false表示数据仍然有效
     */
    public boolean needReload() {
        long curTime = System.currentTimeMillis() / 1000;
        return curTime - expireTime > 3600
                || !TimeUtils.timeToHH(curTime).equals(TimeUtils.timeToHH(expireTime));
    }

    /**
     * 获取当前应该显示的天气图标代码
     * 优先使用当前小时的预报图标，如果没有则使用观测数据的图标
     * @return 天气图标代码
     */
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

    /**
     * 重写toString方法，用于调试输出
     * @return 格式化的字符串表示
     */
    @NonNull
    @Override
    public String toString() {
        return "WeatherModel {" + "\n" +
                "latitude = " + latitude + "\n" +
                "longitude = " + longitude + "\n" +
                "expireTime = " + expireTime + "\n" +
                "observation = " + observation.toString() + "\n" +
                "hourlies = " + hourlies.toString() + "\n" +
                "dailies = " + dailies.toString() + "\n" +
                '}';
    }

    /**
     * 获取今天的天气预报数据
     * @return 今天的Daily对象，如果没找到则返回null
     */
    public Daily today() {
        if (dailies != null && !dailies.isEmpty()) {
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

    /**
     * 获取明天的天气预报数据
     * @return 明天的Daily对象，如果没找到则返回null
     */
    public Daily tomorrow() {

        if (dailies != null && !dailies.isEmpty()) {

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
