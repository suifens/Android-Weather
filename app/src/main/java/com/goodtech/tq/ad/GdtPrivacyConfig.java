package com.goodtech.tq.ad;

import android.util.Log;

import com.qq.e.comm.managers.setting.GlobalSetting;

import java.util.HashMap;
import java.util.Map;

/**
 * 优量汇（com.qq.e.comm）合规配置：在 SDK 初始化前关闭安装列表采集及后台安装/卸载广播监听。
 * 检测项 2.8 举证：com.qq.e.comm.plugin.apkmanager.e 注册 PACKAGE_ADDED/PACKAGE_REMOVED。
 */
public final class GdtPrivacyConfig {

    private static final String TAG = "GdtPrivacyConfig";

    private GdtPrivacyConfig() {
    }

    /** 须在 TTAdSdk / GDTAdSdk 初始化之前调用 */
    public static void applyBeforeInit() {
        try {
            GlobalSetting.setEnableCollectAppInstallStatus(false);
            Map<String, Boolean> optimize = new HashMap<>(1);
            optimize.put("hieib", false);
            GlobalSetting.setConvOptimizeInfo(optimize);
            Log.d(TAG, "已关闭优量汇安装列表采集及后台安装/卸载监听");
        } catch (Throwable t) {
            Log.w(TAG, "优量汇隐私配置失败（可能未集成 GDT SDK）", t);
        }
    }
}
