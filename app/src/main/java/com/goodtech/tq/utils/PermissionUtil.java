package com.goodtech.tq.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * com.goodtech.tq.utils
 */
public class PermissionUtil {

    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

}
