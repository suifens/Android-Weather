package com.goodtech.tq.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * com.goodtech.tq.utils.Weather
 * author: zhengyixiong
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtils {

    public static long switchTime(String time) {
        long timestamp = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//注意格式化的表达式
        try {
            Date formatTime = format.parse(time);
            String date = formatTime.toString();
            //将西方形式的日期字符串转换成java.util.Date对象
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US);
            Date datetime = (Date) sdf.parse(date);
            //再转换成自己想要显示的格式
            timestamp = dateToLong(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public static long dateToLong(Date date) {
        return date.getTime();
    }

    public static String timeToHH(long timeMills) {
        SimpleDateFormat format = new SimpleDateFormat("HH");
        String timeStr = format.format(timeMills);
//        if (timeStr.equals("00")) {
//            return "24";
//        }
        return timeStr;
    }

    public static String timeToHHmm(long timeMills) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(timeMills);
    }

    public static String longToString(long timeMills, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(timeMills);
    }

    /**
     * 是否需要重新定位， 5分钟内不需要
     */
    public static boolean needLocation() {
        long locationTime = SpUtils.getInstance().getLong(Constants.TIME_LOCATION, (long) 0);
        return System.currentTimeMillis() - locationTime > 30 * 60 * 1000;
    }

    public static boolean needFetchWeather() {
        long locationTime = SpUtils.getInstance().getLong(Constants.TIME_WEATHER, (long) 0);
        return System.currentTimeMillis() - locationTime > 5 * 60 * 1000;
    }

}
