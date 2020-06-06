package com.goodtech.tq.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

    /**
     * 返回版本名字
     * 对应build.gradle中的versionName
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = WeatherApp.getInstance().getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(WeatherApp.getInstance().getPackageName(), 0);
            versionName = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

}
