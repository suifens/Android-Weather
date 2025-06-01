package com.goodtech.tq.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.goodtech.tq.base.callback.DataCallback;
import com.goodtech.tq.views.popup.TopTitlePopup;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;

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

    private static final String TAG = "JumpPermissionManagement";

    /**
     * Build.MANUFACTURER
     */
    private static final String MANUFACTURER_HUAWEI = "HUAWEI";//华为
    private static final String MANUFACTURER_MEIZU = "Meizu";//魅族
    private static final String MANUFACTURER_XIAOMI = "Xiaomi";//小米
    private static final String MANUFACTURER_SONY = "Sony";//索尼
    private static final String MANUFACTURER_OPPO = "OPPO";//oppo
    private static final String MANUFACTURER_LG = "LG";
    private static final String MANUFACTURER_VIVO = "vivo";//vivo
    private static final String MANUFACTURER_SAMSUNG = "samsung";//三星
    private static final String MANUFACTURER_ZTE = "ZTE";//中兴
    private static final String MANUFACTURER_YULONG = "YuLong";//酷派
    private static final String MANUFACTURER_LENOVO = "LENOVO";//联想

    /**
     * 此函数可以自己定义
     * @param activity
     */
    public static void goToSetting(Activity activity){
        switch (Build.MANUFACTURER){
            case MANUFACTURER_HUAWEI:
                Huawei(activity);
                break;
            case MANUFACTURER_MEIZU:
                Meizu(activity);
                break;
            case MANUFACTURER_XIAOMI:
                Xiaomi(activity);
                break;
            case MANUFACTURER_SONY:
                Sony(activity);
                break;
            case MANUFACTURER_OPPO:
                OPPO(activity);
                break;
            case MANUFACTURER_VIVO:
                VIVO(activity);
                break;
            case MANUFACTURER_LG:
                LG(activity);
                break;
            default:
                ApplicationInfo(activity);
                Log.e("goToSetting", "目前暂不支持此系统");
                break;
        }
    }

    public static void Huawei(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getApplicationInfo().packageName);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void Meizu(Activity activity) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", activity.getPackageName());
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void Xiaomi(Activity activity) {
        try {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.putExtra("extra_pkgname", activity.getPackageName());
            ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.setComponent(componentName);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void Sony(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void OPPO(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            //        ComponentName comp = new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity");
            ComponentName comp = new ComponentName("com.coloros.securitypermission", "com.coloros.securitypermission.permission.PermissionAppAllPermissionActivity");//R11t 7.1.1 os-v3.2
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void VIVO(Activity activity) {
        Intent localIntent;
        if (((Build.MODEL.contains("Y85")) && (!Build.MODEL.contains("Y85A"))) || (Build.MODEL.contains("vivo Y53L"))) {
            localIntent = new Intent();
            localIntent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity");
            localIntent.putExtra("packagename", activity.getPackageName());
            localIntent.putExtra("tabId", "1");
            activity.startActivity(localIntent);
        } else {
            localIntent = new Intent();
            localIntent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity");
            localIntent.setAction("secure.intent.action.softPermissionDetail");
            localIntent.putExtra("packagename", activity.getPackageName());
            activity.startActivity(localIntent);
        }
    }

    public static void LG(Activity activity) {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    /**
     * 只能打开到自带安全软件
     * @param activity
     */
    public static void _360(Activity activity) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", activity.getPackageName());
        ComponentName comp = new ComponentName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
        intent.setComponent(comp);
        activity.startActivity(intent);
    }

    /**
     * 应用信息界面
     * @param activity
     */
    public static void ApplicationInfo(Activity activity){
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        activity.startActivity(localIntent);
    }

    /**
     * 系统设置界面
     * @param activity
     */
    public static void SystemConfig(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        activity.startActivity(intent);
    }

    /**
     * 默认打开应用详细页
     */
    private static void goIntentSetting(Activity pActivity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", pActivity.getPackageName(), null);
        intent.setData(uri);
        try {
            pActivity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] initializePermissions(String... permissions) {
        return permissions;
    }

    private static void requestPermission(Activity activity, PermissionUtils permissionUtils,
                                          String title, String message, DataCallback<Boolean> callback) {
        TopTitlePopup popup = new TopTitlePopup(activity);
        popup.setupData(title, message);
        BasePopupView popupView = new XPopup.Builder(activity)
                .isDestroyOnDismiss(true)
                .popupWidth(ScreenUtils.getScreenWidth())
                .popupPosition(PopupPosition.Top)
                .isLightStatusBar(true)
                .hasShadowBg(false)
                .hasStatusBarShadow(false)
                .asCustom(popup);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 执行延时任务的逻辑
                // 这里可以写需要延时执行的代码
                popupView.show();
            }
        };

        permissionUtils.callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                handler.removeCallbacks(runnable);
                popupView.dismiss();
                callback.onComplete(true, "");
            }

            @Override
            public void onDenied() {
                handler.removeCallbacks(runnable);
                popupView.dismiss();
                callback.onComplete(false, "onDenied");
            }
        }).request();
        handler.postDelayed(runnable, 500);
    }

    /**
     * 图片读取权限
     */
    public static void readImagePermission(Activity activity, @NonNull DataCallback<Boolean> callback) {
        String[] permissions = initializePermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        PermissionUtils utils = PermissionUtils.permission(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (PermissionUtils.isGranted(Manifest.permission.READ_MEDIA_IMAGES)) {
                callback.onComplete(true, "");
                return;
            } else {
                utils = PermissionUtils.permission(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (PermissionUtils.isGranted(permissions)) {
            callback.onComplete(true, "");
            return;
        }

        requestPermission(activity, utils, "请允许天气预报使用读写权限",
                "使用分享功能，我们需要将您的图片先储存手机文件中，如果您拒绝，也不会影响您使用产品的其他功能", callback);
    }

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            return Settings.canDrawOverlays(context);
        } else {
            if (Settings.canDrawOverlays(context)) return true;
            try {
                WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (mgr == null) return false; //getSystemService might return null
                View viewToAdd = new View(context);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
                viewToAdd.setLayoutParams(params);
                mgr.addView(viewToAdd, params);
                mgr.removeView(viewToAdd);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
