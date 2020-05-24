package com.goodtech.tq.utils;

import android.content.res.Resources;

import com.goodtech.tq.app.WeatherApp;

/**
 * com.goodtech.tq.utils
 */
public class DeviceUtils {

    /**
     * 利用反射获取状态栏高度
     * @return
     */
    public static int getStatusBarHeight() {

        Resources resources = WeatherApp.getInstance().getResources();

        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
