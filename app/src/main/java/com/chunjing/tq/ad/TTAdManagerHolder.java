package com.chunjing.tq.ad;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;
import com.bytedance.sdk.openadsdk.mediation.init.MediationConfigUserInfoForSegment;
import com.bytedance.sdk.openadsdk.mediation.init.MediationPrivacyConfig;
import com.chunjing.tq.BuildConfig;
import com.qq.e.comm.managers.setting.GlobalSetting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class TTAdManagerHolder {

    private static final String TAG = "TTAdManagerHolder";

    private static boolean sInit;

    public static TTAdManager get() {

        return TTAdSdk.getAdManager();
    }

    public static void init(final Context context) {
        //初始化穿山甲SDK
        doInit(context);
    }

    //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
    private static void doInit(Context context) {
        if (!sInit && !TTAdSdk.isSdkReady()) {
            applyGdtPrivacyBeforeInit();
            //setp1.1：初始化SDK

            TTAdSdk.init(context, buildConfig(context));
            //setp1.2：启动SDK

            TTAdSdk.start(new TTAdSdk.Callback() {
                @Override
                public void success() {

                    Log.i(TAG, "success: " + TTAdSdk.isInitSuccess());
                }

                @Override
                public void fail(int code, String msg) {
                    Log.i(TAG, "fail:  code = " + code + " msg = " + msg);
                }
            });
            sInit = true;
        }
    }


    private static TTAdConfig buildConfig(Context context) {

        return new TTAdConfig.Builder()
                /**
                 * 注：需要替换成在媒体平台申请的appID ，切勿直接复制
                 */
                .appId(BuildConfig.PGE_APP_ID)
                .appName(AppUtils.getAppName())
                /**
                 * 上线前需要关闭debug开关，否则会影响性能
                 */
                .debug(false)
                /**
                 * 使用聚合功能此开关必须设置为true，默认为false，不会初始化聚合模板，聚合功能会吟唱
                 */
                .useMediation(true)
                .supportMultiProcess(true)
                .customController(getTTCustomController())
//                .customController(getTTCustomController()) //如果您需要设置隐私策略请参考该api
//                .setMediationConfig(new MediationConfig.Builder() //可设置聚合特有参数详细设置请参考该api
//                        .setMediationConfigUserInfoForSegment(getUserInfoForSegment())//如果您需要配置流量分组信息请参考该api
//                        .build())
                .build();
    }

    private static MediationConfigUserInfoForSegment getUserInfoForSegment(){
        MediationConfigUserInfoForSegment userInfo = new MediationConfigUserInfoForSegment();
        userInfo.setUserId("msdk-demo");
        userInfo.setGender(MediationConfigUserInfoForSegment.GENDER_MALE);
        userInfo.setChannel("msdk-channel");
        userInfo.setSubChannel("msdk-sub-channel");
        userInfo.setAge(999);
        userInfo.setUserValueGroup("msdk-demo-user-value-group");

        Map<String, String> customInfos = new HashMap<>();
        customInfos.put("aaaa", "test111");
        customInfos.put("bbbb", "test222");
        userInfo.setCustomInfos(customInfos);
        return userInfo;
    }

    /**
     * 优量汇在聚合初始化前关闭可选采集（传感器、AndroidID、安装列表等），
     * 避免 164 号文「未清晰明示 / 超范围收集」。
     */
    private static void applyGdtPrivacyBeforeInit() {
        try {
            Map<String, String> extra = new HashMap<>();
            extra.put("sensor_ts", "0");
            GlobalSetting.setExtraUserData(extra);
            GlobalSetting.setAgreeReadAndroidId(false);
            GlobalSetting.setAgreeReadDeviceId(false);
            GlobalSetting.setEnableCollectAppInstallStatus(false);

            Map<String, Boolean> privacy = new HashMap<>();
            privacy.put("android_id", false);
            privacy.put("mac_address", false);
            privacy.put("device_id", false);
            privacy.put("imsi", false);
            privacy.put("ssid", false);
            privacy.put("bssid", false);
            privacy.put("cell_id", false);
            privacy.put("wipaddr", false);
            privacy.put("sensor", false);
            GlobalSetting.setAgreeReadPrivacyInfo(privacy);
        } catch (Throwable t) {
            Log.w(TAG, "applyGdtPrivacyBeforeInit: " + t.getMessage());
        }
    }

    private static TTCustomController getTTCustomController(){
        return new TTCustomController() {

            @Override
            public boolean isCanUseLocation() {
                return false;
            }

            @Override
            public boolean alist() {
                return false;
            }

            @Override
            public boolean isCanUsePhoneState() {
                return false;
            }

            @Override
            public String getDevImei() {
                return "";
            }

            @Override
            public boolean isCanUseWifiState() {
                return false;
            }

            @Override
            public String getMacAddress() {
                return "";
            }

            @Override
            public boolean isCanUseWriteExternal() {
                return false;
            }

            @Override
            public boolean isCanUseAndroidId() {
                return false;
            }

            @Override
            public String getAndroidId() {
                return "";
            }

            @Override
            public boolean isCanUsePermissionRecordAudio() {
                return false;
            }

            @Override
            public boolean isCanUseMessage() {
                return false;
            }

            @Override
            public MediationPrivacyConfig getMediationPrivacyConfig() {
                return new MediationPrivacyConfig() {
                    @Override
                    public java.util.List<String> getCustomAppList() {
                        return Collections.emptyList();
                    }

                    @Override
                    public boolean isCanUseOaid() {
                        return true;
                    }

                    @Override
                    public boolean isLimitPersonalAds() {
                        return false;
                    }

                    @Override
                    public boolean isProgrammaticRecommend() {
                        return true;
                    }
                };
            }
        };
    }
}
