package com.goodtech.tq.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;

import com.goodtech.tq.app.WeatherApp;

import java.lang.reflect.Field;

/**
 * com.goodtech.tq.utils
 */
public class DeviceUtils {

    private static float currentDensity = 0;

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

    /**
     * 返回屏幕尺寸(宽)
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return metrics.widthPixels;
    }

    /**
     * 返回屏幕尺寸(高)
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        int sdk = Build.VERSION.SDK_INT;

        DisplayMetrics metrics = getDisplayMetrics(context);
        if (sdk > 10 && sdk < 13) {
            return metrics.heightPixels - getStatusBarHeight(context);
        }
        return metrics.heightPixels;
    }

    /**
     * 返回屏幕尺寸
     *
     * @param context
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = getInternalDimenPixelSize(context, "status_bar_height");
        return statusBarHeight;
    }

    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    private static int getInternalDimenPixelSize(Context context, String fieldName) {
        int result = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField(fieldName);
            result = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int dip2px(Context context, float dipValue) {
        if (currentDensity > 0)
            return (int) (dipValue * currentDensity + 0.5f);

        currentDensity = getScreenDensity(context);
        return (int) (dipValue * currentDensity + 0.5f);
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (v.getTranslationX() + 0.5f);
        final int ty = (int) (v.getTranslationY() + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    private static final int[] EMPTY_STATE = new int[] {};

    public static void clearState(Drawable drawable) {
        if (drawable != null) {
            drawable.setState(EMPTY_STATE);
        }
    }

}
