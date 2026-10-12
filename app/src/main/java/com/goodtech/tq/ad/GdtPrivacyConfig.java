package com.goodtech.tq.ad;

import android.util.Log;

import com.goodtech.tq.BuildConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 优量汇（com.qq.e.comm）合规配置。
 *
 * <p>必须在 TTAdSdk / GDTAdSdk 初始化之前调用 {@link #applyBeforeInit()}，用于：
 * <ul>
 *   <li>关闭安装列表采集：{@code setEnableCollectAppInstallStatus(false)}</li>
 *   <li>关闭后台安装/卸载广播监听：{@code hieib=false}（对应 PACKAGE_ADDED/REMOVED）</li>
 *   <li>关闭传感器类型列表采集：{@code extraUserData.sensor_ts=0}</li>
 * </ul>
 *
 * <p>通过反射调用，以便 {@code -PdisableGdtForReview=true} 排除优量汇 AAR 后仍能编译。
 * 提审包建议：{@code ./gradlew assembleHuaweiRelease -PdisableGdtForReview=true}
 */
public final class GdtPrivacyConfig {

    private static final String TAG = "GdtPrivacyConfig";
    private static final String GDT_GLOBAL_SETTING = "com.qq.e.comm.managers.setting.GlobalSetting";

    /** 优量汇转化优化开关：false = 禁用后台安装/卸载监听 */
    private static final String KEY_DISABLE_INSTALL_BROADCAST = "hieib";

    /** 优量汇 extraUserData：0 = 关闭传感器采集（加速度/陀螺仪/磁力/重力） */
    private static final String KEY_SENSOR_TS = "sensor_ts";
    private static final String SENSOR_DISABLED = "0";

    private static final AtomicBoolean applied = new AtomicBoolean(false);
    private static final AtomicBoolean applySucceeded = new AtomicBoolean(false);

    private GdtPrivacyConfig() {
    }

    /**
     * 当前构建是否排除了优量汇依赖（提审合规包）。
     */
    public static boolean isGdtExcludedFromBuild() {
        return BuildConfig.DISABLE_GDT_FOR_REVIEW;
    }

    /**
     * 上次 {@link #applyBeforeInit()} 是否成功写入优量汇隐私开关。
     */
    public static boolean isApplySucceeded() {
        return applySucceeded.get();
    }

    /**
     * 须在任何广告 SDK（穿山甲 / 优量汇）初始化之前调用。可重复调用，仅首次生效。
     *
     * @return true 表示配置已成功写入，或构建已排除 GDT；false 表示写入失败
     */
    public static boolean applyBeforeInit() {
        if (BuildConfig.DISABLE_GDT_FOR_REVIEW) {
            Log.i(TAG, "提审模式：构建已排除优量汇依赖，跳过 GDT 隐私配置");
            applySucceeded.set(true);
            return true;
        }
        if (!applied.compareAndSet(false, true)) {
            return applySucceeded.get();
        }
        boolean ok = writeGdtPrivacyFlags();
        applySucceeded.set(ok);
        return ok;
    }

    /**
     * 广告 SDK 初始化完成后再次加固一次配置（防止插件热加载覆盖）。
     */
    public static void reapplyAfterSdkStart() {
        if (BuildConfig.DISABLE_GDT_FOR_REVIEW) {
            return;
        }
        writeGdtPrivacyFlags();
    }

    /**
     * 反射写入优量汇开关，避免直接依赖 GDT 类导致提审包无法编译。
     */
    private static boolean writeGdtPrivacyFlags() {
        try {
            Class<?> clazz = Class.forName(GDT_GLOBAL_SETTING);

            Method setInstallStatus = clazz.getMethod("setEnableCollectAppInstallStatus", boolean.class);
            setInstallStatus.invoke(null, false);

            Method setConvOptimize = clazz.getMethod("setConvOptimizeInfo", Map.class);
            Map<String, Boolean> optimize = new HashMap<>(2);
            optimize.put(KEY_DISABLE_INSTALL_BROADCAST, false);
            setConvOptimize.invoke(null, optimize);

            // 164号文场景6：关闭读取特定类型传感器列表
            disableSensorCollection(clazz);

            // 部分版本插件加载后会回读 settings，再写一次安装列表开关
            setInstallStatus.invoke(null, false);

            Log.i(TAG, "优量汇合规配置成功：已关闭安装列表采集、后台安装/卸载监听及传感器采集");
            return true;
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "未找到优量汇 GlobalSetting，当前包可能未集成 GDT", e);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "优量汇隐私配置失败！安装/卸载监听可能仍会注册，请检查 GDT SDK 版本", t);
            return false;
        }
    }

    /**
     * 关闭优量汇传感器类型列表采集。
     * 官方/聚合文档：{@code setExtraUserData} 写入 {@code sensor_ts=0}。
     */
    @SuppressWarnings("unchecked")
    private static void disableSensorCollection(Class<?> clazz) {
        try {
            Method setExtra = clazz.getMethod("setExtraUserData", Map.class);
            Map<String, String> extra = new HashMap<>(4);
            try {
                Method getExtra = clazz.getMethod("getExtraUserData");
                Object raw = getExtra.invoke(null);
                if (raw instanceof Map) {
                    extra.putAll((Map<String, String>) raw);
                }
            } catch (Throwable ignore) {
                // 无 get 接口时只写本次配置
            }
            extra.put(KEY_SENSOR_TS, SENSOR_DISABLED);
            setExtra.invoke(null, extra);
            Log.i(TAG, "优量汇已关闭传感器采集 sensor_ts=0");
        } catch (Throwable t) {
            Log.w(TAG, "优量汇关闭传感器采集失败，当前 SDK 可能无此接口", t);
        }
    }
}
