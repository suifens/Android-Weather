package com.goodtech.tq.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class CheckApkExist {
    public static String facebookPkgName = "com.facebook.katana";
    public static String instagramPkgName = "com.instagram.android";
    
    public static boolean checkApkExist(Context context, String packageName){
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    public static boolean checkFacebookExist(Context context){
        return checkApkExist(context, facebookPkgName);
    }
}
