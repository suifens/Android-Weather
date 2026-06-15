package com.goodtech.tq.ad;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.goodtech.tq.BuildConfig;


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
        if (!sInit) {
            GdtPrivacyConfig.applyBeforeInit();
            //TTAdSdk.init(context, buildConfig(context));
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
                .supportMultiProcess(false)
//                .customController(new TTCustomController() {
//                    @Override
//                    public boolean isCanUseWifiState() {
//                        return false;
//                    }
//                })
                .customController(new AdPrivacyController())
//                .setMediationConfig(new MediationConfig.Builder() //可设置聚合特有参数详细设置请参考该api
//                        .setMediationConfigUserInfoForSegment(getUserInfoForSegment())//如果您需要配置流量分组信息请参考该api
//                        .build())
                .build();
    }

}
