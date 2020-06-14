package com.goodtech.tq.utils;

import com.goodtech.tq.httpClient.ErrorCode;

/**
 * com.goodtech.tq.utils
 *
 * @author: mac
 * @date: 2020/5/23
 */
public class WeatherUtils {

    /**
     * 风速等级
     */
    public static int windGrade(float windSpeed) {
        if (windSpeed <= 0.72) {
            return 0;
        } else if (windSpeed <= 5.4) {
            return 1;
        } else if (windSpeed <= 11.9) {
            return 2;
        } else if (windSpeed <= 19.4) {
            return 3;
        } else if (windSpeed <= 28.4) {
            return 4;
        } else if (windSpeed <= 38.5) {
            return 5;
        } else if (windSpeed <= 49.7) {
            return 6;
        } else if (windSpeed <= 61.6) {
            return 7;
        } else if (windSpeed <= 74.5) {
            return 8;
        } else if (windSpeed <= 87.8) {
            return 9;
        } else if (windSpeed <= 102.2) {
            return 10;
        } else if (windSpeed <= 117.4) {
            return 11;
        } else if (windSpeed <= 132.8) {
            return 12;
        } else {
            return 13;
        }
    }


}
