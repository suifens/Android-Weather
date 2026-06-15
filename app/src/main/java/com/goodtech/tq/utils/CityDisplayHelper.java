package com.goodtech.tq.utils;

import android.text.TextUtils;

import com.goodtech.tq.models.CityMode;

/**
 * 城市展示文案，对齐纯净天气 Simple：
 * 定位城市 → mergerName（区县 + POI）；普通城市 → cityName。
 */
public final class CityDisplayHelper {

    private static final String LOCATE_PLACEHOLDER = "立即定位";

    private CityDisplayHelper() {
    }

    public static String getHomeDisplayName(CityMode city) {
        if (city == null) {
            return "";
        }
        if (city.getLocation()) {
            String mergerName = trim(city.getMergerName());
            if (!TextUtils.isEmpty(mergerName)) {
                return mergerName;
            }
            String cityName = trim(city.getCity());
            return TextUtils.isEmpty(cityName) ? LOCATE_PLACEHOLDER : cityName;
        }
        String cityName = trim(city.getCity());
        if (!TextUtils.isEmpty(cityName)) {
            return cityName;
        }
        return trim(city.getMergerName());
    }

    public static boolean shouldShowLocationIcon(CityMode city) {
        return city != null && city.getLocation() && !TextUtils.isEmpty(getHomeDisplayName(city))
                && !LOCATE_PLACEHOLDER.contentEquals(getHomeDisplayName(city));
    }

    private static String trim(String value) {
        return value != null ? value.trim() : "";
    }
}
