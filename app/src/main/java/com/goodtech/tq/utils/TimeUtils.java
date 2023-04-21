package com.goodtech.tq.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

        if (time.contains("+0600")) {
            timestamp += 2 * 60 * 60 * 1000;
        } else if (time.contains("+0700")) {
            timestamp += 60 * 60 * 1000;
        }

        return timestamp;
    }

    /**
     * @param strTime    要转换的string类型的时间，
     * @param formatType 要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
     * @return Date
     */
    public static Date stringToDate(String strTime, String formatType) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        try {
            date = formatter.parse(strTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static long longWithDate(String strTime, String formatType) {
        Date date = stringToDate(strTime, formatType);
        if (date != null) {
            return date.getTime();
        } else {
            return 0;
        }
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

    public static String getNowTime() {
        return timeToHHmm(System.currentTimeMillis());
    }

    public static String timeToDay(String strTime, String formatType) {
        long timeMills = longWithDate(strTime, formatType);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
        return dateFormat.format(timeMills);
    }

    public static String timeToDay(long timeMills) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
        return dateFormat.format(timeMills);
    }

    public static String longToString(long timeMills, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(timeMills);
    }

    public static String timeToString(long timeMills, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(timeMills);
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

    /**
     * 之后是否还有假期
     */
    public static boolean afterHoliday(long timeMills) {
        int month = Integer.parseInt(longToString(timeMills, "MM"));
        if (month == 10) {
            int day = Integer.parseInt(longToString(timeMills, "dd"));
            return day > 7;
        } else {
            return month > 10;
        }
    }

    public static String getYesterday() {
        long yesterday = System.currentTimeMillis() - getDayMillis();
        return longToString(yesterday, "MM月dd日");
    }

    /**
     * 获取年份
     */
    public static int getYear(long timeMills) {
        String year = longToString(timeMills, "yyyy");
        return Integer.parseInt(year);
    }

    public static int getMonth(long timeMills) {
        String year = longToString(timeMills, "MM");
        return Integer.parseInt(year);
    }

    public static int getDay(long timeMills) {
        String year = longToString(timeMills, "dd");
        return Integer.parseInt(year);
    }

    public static int getYearWeek(Date date) {
        Calendar cal = Calendar.getInstance();//这一句必须要设置，否则美国认为第一天是周日，而我国认为是周一，对计算当期日期是第几周会有错误
        int weekYear = cal.get(Calendar.YEAR);//获得当前的年
        cal.set(weekYear, 0,1);// 每周从周一开始
        cal.setTime(date);
        int weeks = cal.get(Calendar.WEEK_OF_YEAR);
        return weeks;
    }

    /**
     * 获取某天的00点
     */
    public static long getZoneTime(long timeMillis) {
        String day = longToString(timeMillis, "yyyy-MM-dd");
        return longWithDate(day, "yyyy-MM-dd");
    }

    /**
     * 是否是今天
     */
    public static boolean isCurrentDay(long timeMillis) {
        long zoneTime = getZoneTime(timeMillis);
        long curZoneTime = getZoneTime(System.currentTimeMillis());
        return zoneTime == curZoneTime;
    }

    /**
     * 是否为白天时间 [4:00 ~ 18:00)
     */
    public static boolean isDaytime(long timeMillis) {
       long zoneTime = getZoneTime(timeMillis);
       long startTime = zoneTime + 4 * getHourMillis();
       long endTime = zoneTime + 17 * getHourMillis();
       return timeMillis >= startTime && timeMillis < endTime;
    }

    public static long getHourMillis() {
        return 60 * 60 * 1000;
    }

    public static long getDayMillis() {
        return 60 * 60 * 1000 * 24;
    }

    public static String getWeek(String time) {
        String Week = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int wek=c.get(Calendar.DAY_OF_WEEK);

        if (wek == 1) {
            Week += "周日";
        }
        if (wek == 2) {
            Week += "周一";
        }
        if (wek == 3) {
            Week += "周二";
        }
        if (wek == 4) {
            Week += "周三";
        }
        if (wek == 5) {
            Week += "周四";
        }
        if (wek == 6) {
            Week += "周五";
        }
        if (wek == 7) {
            Week += "周六";
        }
        return Week;
    }

}
