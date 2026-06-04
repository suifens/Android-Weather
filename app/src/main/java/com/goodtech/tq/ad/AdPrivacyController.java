package com.goodtech.tq.ad;

import com.bytedance.sdk.openadsdk.TTCustomController;
import com.bytedance.sdk.openadsdk.mediation.init.MediationPrivacyConfig;

/**
 * 穿山甲/GroMore 聚合隐私开关：限制非必要采集，安装列表仅在广告展示链路按需由 SDK 处理。
 */
public class AdPrivacyController extends TTCustomController {

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
    public boolean isCanUseWifiState() {
        return false;
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
    public boolean isCanUseMessage() {
        return false;
    }

    @Override
    public MediationPrivacyConfig getMediationPrivacyConfig() {
        return new MediationPrivacyConfig() {
            @Override
            public boolean isCanUseOaid() {
                return false;
            }

            @Override
            public boolean isLimitPersonalAds() {
                return super.isLimitPersonalAds();
            }

            @Override
            public boolean isProgrammaticRecommend() {
                return super.isProgrammaticRecommend();
            }
        };
    }

    @Override
    public boolean isCanUsePermissionRecordAudio() {
        return false;
    }
}
